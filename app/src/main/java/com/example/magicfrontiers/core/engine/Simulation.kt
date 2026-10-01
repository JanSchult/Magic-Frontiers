package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.Command
import com.example.magicfrontiers.core.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Simulation(initialState: GameState) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<GameState> = _state

    private val commandQueue = CommandQueue()
    private val commandProcessor = CommandProcessor()

    // Reihenfolge ist bewusst: Commands zuerst, dann Bewegung, dann Kampf, ...
    // Movement/Combat/Production-Systeme werden in den nächsten Schritten ergänzt.
    private val systems: List<GameSystem> = listOf(
        commandProcessor,
         MovementSystem(),  // -> Schritt 2
         CombatSystem(),     // -> Schritt 3
         ProductionSystem(), // -> Schritt 4
        GatheringSystem(),

        // VisionSystem(),     // -> Schritt 8
    )

    fun submitCommand(command: Command) {
        commandQueue.submit(command)
    }

    fun step(deltaMs: Long) {
        commandProcessor.enqueue(commandQueue.drainAll())

        var newState = _state.value.copy(tick = _state.value.tick + 1)
        for (system in systems) {
            newState = system.update(newState, deltaMs)
        }
        _state.value = newState
    }
}