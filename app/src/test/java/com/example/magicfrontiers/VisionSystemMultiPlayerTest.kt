package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.VisionSystem
import com.example.magicfrontiers.core.model.CellKey
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class VisionSystemMultiPlayerTest {

    @Test
    fun `each player gets independent visibility`() {
        val p1Unit = GameUnit(
            UnitId("u1"), "scout", PlayerId("p1"), UnitRole.MELEE, Vector2(2f, 2f), 60,
            UnitStats(60, 8, 1f, 800L, 3f, visionRange = 2f)
        )
        val p2Unit = GameUnit(
            UnitId("u2"), "scout", PlayerId("p2"), UnitRole.MELEE,
            Vector2(20f, 20f), 60,
            UnitStats(60, 8, 1f, 800L, 3f, visionRange = 2f)
        )

        val state = GameState(
            0,
            emptyList(),
            30,
            30,
            units = mapOf(p1Unit.id to p1Unit, p2Unit.id to p2Unit),
            buildings = emptyMap(),
            resourceNodes = emptyMap(),
            playerResources = emptyMap(),
            factions = emptyMap()
        )

        val result = VisionSystem(
            localPlayerId = PlayerId
        ).update(state, deltaMs = 100L)

        assertTrue(CellKey(2, 2) in (result.visibilityByPlayer[PlayerId("p1")] ?: emptySet()))
        assertFalse(CellKey(20, 20) in (result.visibilityByPlayer[PlayerId("p1")] ?: emptySet()))
        assertTrue(CellKey(20, 20) in (result.visibilityByPlayer[PlayerId("p2")] ?: emptySet()))
    }
}