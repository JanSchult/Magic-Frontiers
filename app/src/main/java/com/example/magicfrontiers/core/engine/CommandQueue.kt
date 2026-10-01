package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.Command
import kotlinx.coroutines.channels.Channel

class CommandQueue {
    private val channel = Channel<Command>(capacity = Channel.UNLIMITED)

    fun submit(command: Command) {
        channel.trySend(command)
    }

    /** Holt alle aktuell wartenden Commands, ohne zu blockieren. */
    fun drainAll(): List<Command> {
        val result = mutableListOf<Command>()
        while (true) {
            val r = channel.tryReceive()
            if (r.isSuccess) {
                result += r.getOrThrow()
            } else break
        }
        return result
    }
}