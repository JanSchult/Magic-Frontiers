package com.example.magicfrontiers.ai

import com.example.magicfrontiers.core.engine.catalog.BuildingCatalog
import com.example.magicfrontiers.core.engine.Simulation
import com.example.magicfrontiers.core.engine.catalog.UnitCatalog
import com.example.magicfrontiers.core.model.Command
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.core.model.CellKey
import com.example.magicfrontiers.core.state.UnitAiState

class AiController(
    private val simulation: Simulation,
    private val aiPlayerId: PlayerId,
    private val enemyPlayerId: PlayerId,
    private val decisionIntervalMs: Long = 2000L
) {
    private var timeSinceLastDecisionMs = 0L

    fun update(state: GameState, deltaMs: Long) {
        timeSinceLastDecisionMs += deltaMs
        if (timeSinceLastDecisionMs < decisionIntervalMs) return
        timeSinceLastDecisionMs = 0L

        decideEconomy(state)
        decideMilitary(state)
        decideAttack(state)
        decideControlPoints(state)
    }

    private fun decideEconomy(state: GameState) {
        val myBuildings = state.buildings.values.filter { it.ownerId == aiPlayerId }
        val resources = state.playerResources[aiPlayerId] ?: return

        // Noch keine Kaserne -> erste Priorität
        val hasBarracks = myBuildings.any { it.typeId == "barracks" }
        if (!hasBarracks) {
            val blueprint = BuildingCatalog.get("barracks")
            if ((resources[ResourceType.ENERGY] ?: 0) >= blueprint.costEnergy &&
                (resources[ResourceType.MATERIAL] ?: 0) >= blueprint.costMaterial
            ) {
                val basePosition = myBuildings.firstOrNull()?.position ?: findAiStartPosition(state)
                val placePos = Vector2(basePosition.x + 3f, basePosition.y + 1f)
                simulation.submitCommand(Command.PlaceBuilding(aiPlayerId, "barracks", placePos))
            }
        }
    }

    private fun decideMilitary(state: GameState) {
        val barracks = state.buildings.values
            .filter { it.ownerId == aiPlayerId && it.typeId == "barracks" && it.isConstructed }
            .firstOrNull() ?: return

        if (barracks.productionQueue.isNotEmpty()) return // schon am Produzieren

        val resources = state.playerResources[aiPlayerId] ?: return
        val myArmySize = state.units.values.count { it.ownerId == aiPlayerId }

        // Einfache Heuristik: solange Armee kleiner als 6, weiter produzieren
        if (myArmySize >= 6) return

        val unitTypeId = if (myArmySize % 2 == 0) "scout" else "archer"
        val blueprint = UnitCatalog.get(unitTypeId)
        if ((resources[ResourceType.ENERGY] ?: 0) >= blueprint.costEnergy &&
            (resources[ResourceType.MATERIAL] ?: 0) >= blueprint.costMaterial
        ) {
            simulation.submitCommand(Command.ProduceUnit(barracks.id, unitTypeId))
        }
    }

    private fun decideAttack(state: GameState) {
        val myUnits = state.units.values.filter { it.ownerId == aiPlayerId && it.state == UnitAiState.Idle }
        if (myUnits.size < 4) return // erst angreifen, wenn genug Einheiten idle/bereit stehen

        val myVisibility = state.visibilityByPlayer[aiPlayerId] ?: emptySet()

        val visibleEnemyUnit = state.units.values.firstOrNull {
            it.ownerId == enemyPlayerId && CellKey(it.position.x.toInt(), it.position.y.toInt()) in myVisibility
        }
        val visibleEnemyBuilding = state.buildings.values.firstOrNull {
            it.ownerId == enemyPlayerId && CellKey(it.position.x.toInt(), it.position.y.toInt()) in myVisibility
        }

        val targetPosition = visibleEnemyUnit?.position ?: visibleEnemyBuilding?.position ?: return


        simulation.submitCommand(Command.Move(myUnits.map { it.id }, targetPosition))
    }

    private fun findAiStartPosition(state: GameState): Vector2 {
        // Fallback, falls KI noch kein Gebäude besitzt (Spielstart)
        return state.units.values.firstOrNull { it.ownerId == aiPlayerId }?.position
            ?: Vector2(state.mapWidth / 2f, state.mapHeight / 2f)
    }
    private fun decideControlPoints(state: GameState) {
        val uncontrolledPoint = state.controlPoints.values
            .firstOrNull { it.controllingPlayerId != aiPlayerId }

        val myVisibility = state.visibilityByPlayer[aiPlayerId] ?: emptySet()
        val visiblePoint = uncontrolledPoint?.takeIf {
            CellKey(it.position.x.toInt(), it.position.y.toInt()) in myVisibility
        } ?: return

        val idleUnits = state.units.values.filter { it.ownerId == aiPlayerId && it.state == UnitAiState.Idle }
        if (idleUnits.size < 2) return // nicht zu viele Einheiten gleichzeitig abziehen

        simulation.submitCommand(Command.Move(idleUnits.take(2).map { it.id }, visiblePoint.position))
    }
}