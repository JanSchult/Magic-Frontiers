package com.example.magicfrontiers.core.engine.system

import com.example.magicfrontiers.core.model.CellKey
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.state.UnitAiState

class AutoAggroSystem : GameSystem {
    override fun update(state: GameState, deltaMs: Long): GameState {
        var units = state.units
        for (unit in state.units.values) {
            if (unit.state != UnitAiState.Idle || unit.stats.attackRange <= 0f) continue
            val visibility = state.visibilityByPlayer[unit.ownerId] ?: emptySet()
            val nearestEnemy = state.units.values
                .filter { it.ownerId != unit.ownerId }
                .filter { CellKey(it.position.x.toInt(), it.position.y.toInt()) in visibility }
                .filter { (it.position - unit.position).length() <= unit.stats.visionRange }
                .minByOrNull { (it.position - unit.position).length() }
            if (nearestEnemy != null) {
                units = units + (unit.id to unit.copy(targetUnitId = nearestEnemy.id, state = UnitAiState.Attacking))
            }
        }
        return state.copy(units = units)
    }
}