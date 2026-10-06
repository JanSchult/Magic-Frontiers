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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CombatSystemVisionTest {

    @Test
    fun `attacker loses target when target leaves attacker visibility`() {
        val attacker = GameUnit(
            UnitId("a"), "soldier", PlayerId("p1"), UnitRole.MELEE, Vector2(0f, 0f), 100,
            UnitStats(100, 10, 1f, 500L, 2f, 5f), targetUnitId = UnitId("b")
        )
        val defender = GameUnit(
            UnitId("b"), "soldier", PlayerId("p2"), UnitRole.MELEE, Vector2(0.5f, 0f), 100,
            UnitStats(100, 10, 1f, 500L, 2f, 5f)
        )
        val state = GameState(
            0,
            emptyList(),
            20,
            20,
            units = mapOf(attacker.id to attacker, defender.id to defender),
            buildings = emptyMap(),
            resourceNodes = emptyMap(),
            playerResources = emptyMap(),
            factions = emptyMap(),
            visibilityByPlayer = mapOf(PlayerId("p1") to emptySet()) // p1 sieht nichts -> Ziel außer Sicht
        )

        val result = CombatSystem().update(state, deltaMs = 100L)

        assertNull(result.units[UnitId("a")]?.targetUnitId)
        assertEquals(UnitAiState.Idle, result.units[UnitId("a")]?.state)
    }
}