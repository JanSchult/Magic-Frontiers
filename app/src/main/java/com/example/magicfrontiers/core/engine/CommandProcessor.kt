package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.engine.catalog.BuildingCatalog
import com.example.magicfrontiers.core.engine.catalog.TechCatalog
import com.example.magicfrontiers.core.engine.catalog.UnitCatalog
import com.example.magicfrontiers.core.engine.system.GameSystem
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.Command
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.ProductionOrder
import com.example.magicfrontiers.core.model.ResearchOrder
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.SimulationEvent
import com.example.magicfrontiers.core.model.TechEffect
import com.example.magicfrontiers.core.state.UnitAiState

class CommandProcessor : GameSystem {

    private var pendingCommands: List<Command> = emptyList()
    val pendingEvents = mutableListOf<SimulationEvent>()

    fun enqueue(commands: List<Command>) {
        pendingCommands = pendingCommands + commands
    }

    override fun update(state: GameState, deltaMs: Long): GameState {
        if (pendingCommands.isEmpty()) return state
        var newState = state
        for (command in pendingCommands) {
            newState = applyCommand(newState, command)
        }
        pendingCommands = emptyList()
        return newState
    }

    private fun applyCommand(state: GameState, command: Command): GameState {
        return when (command) {
            is Command.Move -> {
                val updatedUnits = state.units.toMutableMap()
                for (unitId in command.unitIds) {
                    val unit = updatedUnits[unitId] ?: continue
                    val path = Pathfinder.findPath(state, unit.position, command.target)

                    updatedUnits[unitId] = unit.copy(
                        pathWaypoints = path,
                        moveTarget = path.firstOrNull() ?: command.target,
                        state = UnitAiState.Moving,
                        targetUnitId = null
                    )
                }
                state.copy(units = updatedUnits)
            }

            is Command.Attack -> {
                val updatedUnits = state.units.toMutableMap()
                for (unitId in command.unitIds) {
                    val unit = updatedUnits[unitId] ?: continue
                    updatedUnits[unitId] = unit.copy(
                        targetUnitId = command.targetUnitId,
                        state = UnitAiState.Attacking,
                        moveTarget = null
                    )
                }
                state.copy(units = updatedUnits)
            }

            is Command.HoldPosition -> {
                val updatedUnits = state.units.toMutableMap()
                for (unitId in command.unitIds) {
                    val unit = updatedUnits[unitId] ?: continue
                    updatedUnits[unitId] = unit.copy(
                        state = UnitAiState.HoldingPosition,
                        moveTarget = null,
                        targetUnitId = null
                    )
                }
                state.copy(units = updatedUnits)
            }

            is Command.ProduceUnit -> {
                val building = state.buildings[command.buildingId] ?: return state
                if (!building.isConstructed) return state

                val blueprint = UnitCatalog.getOrNull(command.unitTypeId) ?: return state
                // Prüfen, ob Einheit eine Freischaltung durch Tech benötigt
                val requiresUnlock = TechCatalog.all().any {
                    it.effect is TechEffect.UnlockUnit && (it.effect as TechEffect.UnlockUnit).unitTypeId == blueprint.typeId
                }
                if (requiresUnlock) {
                    val researched = state.researchedTechs[building.ownerId] ?: emptySet()
                    val isUnlocked = TechCatalog.all().any {
                        it.effect is TechEffect.UnlockUnit &&
                                (it.effect as TechEffect.UnlockUnit).unitTypeId == blueprint.typeId &&
                                it.id in researched
                    }
                    if (!isUnlocked) return state // nicht freigeschaltet -> Befehl verworfen
                }
                val resources = state.playerResources[building.ownerId] ?: return state

                val hasEnergy = (resources[ResourceType.ENERGY] ?: 0) >= blueprint.costEnergy
                val hasMaterial = (resources[ResourceType.MATERIAL] ?: 0) >= blueprint.costMaterial
                if (!hasEnergy || !hasMaterial) {
                    pendingEvents += SimulationEvent.CommandRejected("Nicht genug Ressourcen")
                    return state
                }
                val updatedResources = resources.toMutableMap().apply {
                    this[ResourceType.ENERGY] = (this[ResourceType.ENERGY] ?: 0) - blueprint.costEnergy
                    this[ResourceType.MATERIAL] = (this[ResourceType.MATERIAL] ?: 0) - blueprint.costMaterial
                }

                val order = ProductionOrder(unitTypeId = blueprint.typeId, durationMs = blueprint.buildDurationMs)
                val updatedBuilding = building.copy(productionQueue = building.productionQueue + order)

                state.copy(
                    buildings = state.buildings + (building.id to updatedBuilding),
                    playerResources = state.playerResources + (building.ownerId to updatedResources)
                )
            }

            is Command.PlaceBuilding -> {
                val blueprint = BuildingCatalog.getOrNull(command.typeId) ?: return state
                val resources = state.playerResources[command.playerId] ?: return state

                val hasEnergy = (resources[ResourceType.ENERGY] ?: 0) >= blueprint.costEnergy
                val hasMaterial = (resources[ResourceType.MATERIAL] ?: 0) >= blueprint.costMaterial
                if (!hasEnergy || !hasMaterial) return state

                // Einfache Kollisionsprüfung: kein anderes Gebäude im Umkreis von 1.5 Einheiten
                val tooClose = state.buildings.values.any { existing ->
                    (existing.position - command.position).length() < 1.5f
                }
                if (tooClose) return state

                val updatedResources = resources.toMutableMap().apply {
                    this[ResourceType.ENERGY] = (this[ResourceType.ENERGY] ?: 0) - blueprint.costEnergy
                    this[ResourceType.MATERIAL] = (this[ResourceType.MATERIAL] ?: 0) - blueprint.costMaterial
                }

                val newBuilding = Building(
                    id = BuildingId("b_${state.tick}_${state.buildings.size}"),
                    typeId = blueprint.typeId,
                    ownerId = command.playerId,
                    position = command.position,
                    currentHealth = 1, // wächst während Bauzeit
                    maxHealth = blueprint.maxHealth,
                    isConstructed = false,
                    constructionProgress = 0f
                )

                state.copy(
                    buildings = state.buildings + (newBuilding.id to newBuilding),
                    playerResources = state.playerResources + (command.playerId to updatedResources)
                )
            }
            is Command.Gather -> {
                val node = state.resourceNodes[command.nodeId] ?: return state
                val updatedUnits = state.units.toMutableMap()
                for (unitId in command.unitIds) {
                    val unit = updatedUnits[unitId] ?: continue
                    if (!unit.stats.canGather) continue // Einheit kann grundsätzlich nicht sammeln
                    updatedUnits[unitId] = unit.copy(
                        gatherTargetNodeId = node.id,
                        moveTarget = node.position,
                        state = UnitAiState.MovingToGather,
                        targetUnitId = null
                    )
                }
                state.copy(units = updatedUnits)
            }
            is Command.ResearchTech -> {
                val building = state.buildings[command.buildingId] ?: return state
                if (!building.isConstructed) return state
                if (building.researchQueue.isNotEmpty()) return state // nur eine Forschung gleichzeitig pro Gebäude

                val blueprint = TechCatalog.getOrNull(command.techId) ?: return state
                val alreadyResearched = state.researchedTechs[building.ownerId] ?: emptySet()
                if (blueprint.id in alreadyResearched) return state // schon erforscht

                val missingPrereqs = blueprint.requiresTechIds.any { it !in alreadyResearched }
                if (missingPrereqs) return state

                val resources = state.playerResources[building.ownerId] ?: return state
                val hasEnergy = (resources[ResourceType.ENERGY] ?: 0) >= blueprint.costEnergy
                val hasMaterial = (resources[ResourceType.MATERIAL] ?: 0) >= blueprint.costMaterial
                if (!hasEnergy || !hasMaterial) return state

                val updatedResources = resources.toMutableMap().apply {
                    this[ResourceType.ENERGY] = (this[ResourceType.ENERGY] ?: 0) - blueprint.costEnergy
                    this[ResourceType.MATERIAL] = (this[ResourceType.MATERIAL] ?: 0) - blueprint.costMaterial
                }

                val order =
                    ResearchOrder(techId = blueprint.id, durationMs = blueprint.researchDurationMs)
                val updatedBuilding = building.copy(researchQueue = listOf(order))

                state.copy(
                    buildings = state.buildings + (building.id to updatedBuilding),
                    playerResources = state.playerResources + (building.ownerId to updatedResources)
                )
            }
        }
    }
}