package com.example.magicfrontiers.core.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameLoop(
    private val simulation: Simulation,
    private val scope: CoroutineScope,
    private val tickRateHz: Int = 20,
    private val onTick: ((deltaMs: Long) -> Unit)? = null
) {
    private val tickDurationMs = 1000L / tickRateHz
    private var job: Job? = null

    fun start() {
        job = scope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()
            var accumulator = 0L
            while (isActive) {
                val now = System.nanoTime()
                accumulator += (now - lastTime) / 1_000_000
                lastTime = now

                while (accumulator >= tickDurationMs) {
                    simulation.step(deltaMs = tickDurationMs)
                    onTick?.invoke(tickDurationMs)
                    accumulator -= tickDurationMs
                }
                delay(4) // CPU schonen, kein Busy-Loop
            }
        }
    }

    fun stop() = job?.cancel()
}