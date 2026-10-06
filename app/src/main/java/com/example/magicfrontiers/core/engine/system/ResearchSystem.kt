package com.example.magicfrontiers.core.engine.system

import com.example.magicfrontiers.core.model.GameState

class ResearchSystem : GameSystem {

    override fun update(state: GameState, deltaMs: Long): GameState {
        var buildings = state.buildings
        var researchedTechs = state.researchedTechs

        for (building in state.buildings.values) {
            if (building.researchQueue.isEmpty()) continue
            val order = building.researchQueue.first()
            val progressDelta = deltaMs.toFloat() / order.durationMs
            val newProgress = order.progress + progressDelta

            if (newProgress >= 1f) {
                val currentSet = researchedTechs[building.ownerId] ?: emptySet()
                researchedTechs = researchedTechs + (building.ownerId to (currentSet + order.techId))
                buildings = buildings + (building.id to building.copy(researchQueue = emptyList()))
            } else {
                val updatedOrder = order.copy(progress = newProgress)
                buildings = buildings + (building.id to building.copy(researchQueue = listOf(updatedOrder)))
            }
        }

        return state.copy(buildings = buildings, researchedTechs = researchedTechs)
    }
}