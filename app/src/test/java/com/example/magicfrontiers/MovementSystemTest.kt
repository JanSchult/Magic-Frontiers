package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.system.MovementSystem
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.core.state.UnitAiState
import junit.framework.TestCase.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MovementSystemTest {

    private fun testUnit(pos: Vector2, target: Vector2?) = GameUnit(
        id = UnitId("u1"), typeId = "scout", ownerId = PlayerId("p1"),
        role = UnitRole.MELEE, position = pos, currentHealth = 100,
        stats = UnitStats(
            maxHealth = 100,
            damage = 10,
            attackRange = 1f,
            attackCooldownMs = 1000L,
            moveSpeed = 2f,
            visionRange = 5f
        ),
        state = if (target != null) UnitAiState.Moving else UnitAiState.Idle,
        moveTarget = target
    )

    @Test
    fun `unit moves toward target`() {
        val unit = testUnit(Vector2(0f, 0f), Vector2(10f, 0f))
        val state = GameState(
            0,
            emptyList(),
            20,
            20,
            mapOf(unit.id to unit),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            emptyMap()
        )

        val result = MovementSystem().update(state, deltaMs = 1000L) // 1 Sekunde

        val moved = result.units[unit.id]!!
        assertTrue(moved.position.x > 0f && moved.position.x <= 2f) // moveSpeed = 2f/s
        assertEquals(UnitAiState.Moving, moved.state)
    }

    @Test
    fun `unit arrives and stops`() {
        val unit = testUnit(Vector2(9.9f, 0f), Vector2(10f, 0f))
        val state = GameState(0, emptyList(), 20, 20, mapOf(unit.id to unit), emptyMap(), emptyMap(), emptyMap(), emptyMap())

        val result = MovementSystem().update(state, deltaMs = 1000L)

        val arrived = result.units[unit.id]!!
        assertEquals(UnitAiState.Idle, arrived.state)
        assertNull(arrived.moveTarget)
    }
}