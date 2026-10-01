package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.CommandProcessor
import com.example.magicfrontiers.core.model.Command
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.core.state.UnitAiState
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CommandProcessorTest {

    @Test
    fun `move command sets moveTarget and state`() {
        val unit = GameUnit(
            id = UnitId("u1"), typeId = "scout", ownerId = PlayerId("p1"),
            role = UnitRole.MELEE, position = Vector2(0f, 0f), currentHealth = 100,
            stats = UnitStats(100, 10, 1f, 1000L, 2f, 5f)
        )
        val state = GameState(
            tick = 0, map = emptyList(), mapWidth = 10, mapHeight = 10,
            units = mapOf(unit.id to unit), buildings = emptyMap(),
            resourceNodes = emptyMap(), playerResources = emptyMap(),
            factions = emptyMap()
        )
        val processor = CommandProcessor()
        processor.enqueue(listOf(Command.Move(listOf(unit.id), Vector2(5f, 5f))))

        val result = processor.update(state, 50L)

        assertEquals(Vector2(5f, 5f), result.units[unit.id]?.moveTarget)
        assertEquals(UnitAiState.Moving, result.units[unit.id]?.state)
    }
}