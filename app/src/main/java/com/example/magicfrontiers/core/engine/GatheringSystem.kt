package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.state.UnitAiState

class GatheringSystem(
    private val arrivalThreshold: Float = 0.3f
) : GameSystem {

    override fun update(state: GameState, deltaMs: Long): GameState {
        var units = state.units
        var nodes = state.resourceNodes
        var playerResources = state.playerResources

        for (unit in state.units.values) {
            when (unit.state) {
                UnitAiState.MovingToGather -> {
                    val nodeId = unit.gatherTargetNodeId
                    val node = nodeId?.let { nodes[it] }
                    if (node == null) {
                        units = units + (unit.id to unit.copy(state = UnitAiState.Idle, gatherTargetNodeId = null))
                        continue
                    }
                    val distance = (node.position - unit.position).length()
                    if (distance <= arrivalThreshold) {
                        units = units + (unit.id to unit.copy(state = UnitAiState.Gathering, moveTarget = null))
                    }
                }

                UnitAiState.Gathering -> {
                    val node = nodes[unit.gatherTargetNodeId ?: continue]
                    if (node == null || node.remainingAmount <= 0) {
                        // Node erschöpft -> Einheit gibt auf, geht in Idle zurück (kein Ziel mehr)
                        units = units + (unit.id to unit.copy(
                            state = UnitAiState.Idle,
                            gatherTargetNodeId = null
                        ))
                        continue
                    }

                    val gatherAmount = ((unit.stats.gatherRatePerSecond * deltaMs) / 1000f).toInt().coerceAtLeast(1)
                    val spaceLeft = unit.stats.gatherCapacity - unit.carriedResourceAmount
                    val actualGathered = minOf(gatherAmount, spaceLeft, node.remainingAmount)

                    if (actualGathered <= 0 || spaceLeft <= 0) {
                        // Ladung voll -> Rückweg zum nächsten Gebäude antreten
                        val homeBuilding = findNearestOwnBuilding(unit, state)
                        if (homeBuilding == null) {
                            units = units + (unit.id to unit.copy(state = UnitAiState.Idle))
                            continue
                        }
                        units = units + (unit.id to unit.copy(
                            state = UnitAiState.ReturningResource,
                            moveTarget = homeBuilding.position,
                            carriedResourceType = node.type
                        ))
                        continue
                    }

                    nodes = nodes + (node.id to node.copy(remainingAmount = node.remainingAmount - actualGathered))
                    units = units + (unit.id to unit.copy(
                        carriedResourceAmount = unit.carriedResourceAmount + actualGathered,
                        carriedResourceType = node.type
                    ))
                }

                UnitAiState.ReturningResource -> {
                    val homeBuilding = findNearestOwnBuilding(unit, state) ?: continue
                    val distance = (homeBuilding.position - unit.position).length()
                    if (distance <= arrivalThreshold + 1.0f) { // Gebäude sind größer -> großzügigere Schwelle
                        val resourceType = unit.carriedResourceType ?: continue
                        val currentPool = playerResources[unit.ownerId] ?: emptyMap()
                        val updatedPool = currentPool.toMutableMap().apply {
                            this[resourceType] = (this[resourceType] ?: 0) + unit.carriedResourceAmount
                        }
                        playerResources = playerResources + (unit.ownerId to updatedPool)

                        // Zurück zum Node, um weiterzusammeln
                        val node = nodes[unit.gatherTargetNodeId ?: ""]
                        units = units + (unit.id to unit.copy(
                            carriedResourceAmount = 0,
                            carriedResourceType = null,
                            state = if (node != null && node.remainingAmount > 0) UnitAiState.MovingToGather else UnitAiState.Idle,
                            moveTarget = node?.position
                        ))
                    }
                }

                else -> { /* nichts zu tun für andere Zustände */ }
            }
        }

        return state.copy(units = units, resourceNodes = nodes, playerResources = playerResources)
    }

    private fun findNearestOwnBuilding(unit: GameUnit, state: GameState): Building? =
        state.buildings.values
            .filter { it.ownerId == unit.ownerId && it.isConstructed }
            .minByOrNull { (it.position - unit.position).length() }
}