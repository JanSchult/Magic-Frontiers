package com.example.magicfrontiers.core.engine.system

import com.example.magicfrontiers.core.model.GameState

fun interface GameSystem {
    fun update(state: GameState, deltaMs: Long): GameState
}