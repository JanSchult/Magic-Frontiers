package com.example.magicfrontiers.core.engine.system

import com.example.magicfrontiers.core.model.CellKey
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId

class ExploredMapSystem(private val localPlayerId: PlayerId) : GameSystem {

    override fun update(state: GameState, deltaMs: Long): GameState {
        val visibleNow = state.visibilityByPlayer[localPlayerId] ?: return state
        val updatedMap = state.map.map { cell ->
            if (!cell.isExplored && CellKey(cell.x, cell.y) in visibleNow) {
                cell.copy(isExplored = true)
            } else cell
        }
        return state.copy(map = updatedMap)
    }
}