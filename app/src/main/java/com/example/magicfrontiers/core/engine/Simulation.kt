package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.Command
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.SimulationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

class Simulation(initialState: GameState) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<GameState> = _state

    private val commandQueue = CommandQueue()
    private val commandProcessor = CommandProcessor()

    // Reihenfolge ist bewusst: Commands zuerst, dann Bewegung, dann Kampf, ...
    // Movement/Combat/Production-Systeme werden in den nächsten Schritten ergänzt.
    private val systems: List<GameSystem> = listOf(
        commandProcessor,
        MovementSystem(),
        CombatSystem(),
        ProductionSystem(),
        GatheringSystem(),
        VisionSystem(localPlayerId = PlayerId),
        ExploredMapSystem(localPlayerId = PlayerId("p1"))

    )
    private val _events = MutableSharedFlow<SimulationEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

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
        commandProcessor.pendingEvents.forEach { _events.tryEmit(it) }
        commandProcessor.pendingEvents.clear()
    }
}