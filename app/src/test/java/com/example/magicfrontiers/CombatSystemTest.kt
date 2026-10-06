package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.system.CombatSystem
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.core.state.UnitAiState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.test.Test
import kotlin.test.assertNull

// test/.../CombatSystemTest.kt
class CombatSystemTest {

    private fun unit(
        id: String, pos: Vector2, health: Int = 100,
        target: UnitId? = null, cooldown: Long = 0L,
        damage: Int = 10, range: Float = 1f, cooldownMs: Long = 500L, armor: Int = 0
    ) = GameUnit(
        id = UnitId(id), typeId = "soldier", ownerId = PlayerId("p1"),
        role = UnitRole.MELEE, position = pos, currentHealth = health,
        stats = UnitStats(100, damage, range, cooldownMs, 2f, 5f, armor),
        targetUnitId = target, attackCooldownRemainingMs = cooldown
    )

    private fun state(units: List<GameUnit>) = GameState(
        0, emptyList(), 20, 20,
        units.associateBy { it.id }, emptyMap(), emptyMap(), emptyMap(), emptyMap()
    )

    @Test
    fun `attacker deals damage when in range and cooldown ready`() {
        val attacker = unit("a", Vector2(0f, 0f), target = UnitId("b"))
        val defender = unit("b", Vector2(0.5f, 0f), health = 100)
        val result = CombatSystem().update(state(listOf(attacker, defender)), deltaMs = 100L)

        assertEquals(90, result.units[UnitId("b")]?.currentHealth)
        assertTrue((result.units[UnitId("a")]?.attackCooldownRemainingMs ?: 0) > 0)
    }

    @Test
    fun `attacker moves closer when target out of range`() {
        val attacker = unit("a", Vector2(0f, 0f), target = UnitId("b"))
        val defender = unit("b", Vector2(10f, 0f))
        val result = CombatSystem().update(state(listOf(attacker, defender)), deltaMs = 100L)

        assertEquals(Vector2(10f, 0f), result.units[UnitId("a")]?.moveTarget)
        assertEquals(UnitAiState.Moving, result.units[UnitId("a")]?.state)
    }

    @Test
    fun `dead unit is removed and attacker target cleared next tick`() {
        val attacker = unit("a", Vector2(0f, 0f), target = UnitId("b"), damage = 200)
        val defender = unit("b", Vector2(0.5f, 0f), health = 50)
        val system = CombatSystem()

        val afterHit = system.update(state(listOf(attacker, defender)), deltaMs = 100L)
        assertNull(afterHit.units[UnitId("b")]) // tot und entfernt

        val nextTick = system.update(afterHit, deltaMs = 100L)
        assertNull(nextTick.units[UnitId("a")]?.targetUnitId)
        assertEquals(UnitAiState.Idle, nextTick.units[UnitId("a")]?.state)
    }
}