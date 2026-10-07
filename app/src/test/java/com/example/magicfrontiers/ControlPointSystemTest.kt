package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.system.ControlPointSystem
import com.example.magicfrontiers.core.model.ControlPoint
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import junit.framework.TestCase.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class ControlPointSystemTest {

    private fun unit(owner: String, pos: Vector2) = GameUnit(
        id = UnitId("u_$owner"), typeId = "scout", ownerId = PlayerId(owner),
        role = UnitRole.MELEE, position = pos, currentHealth = 60,
        stats = UnitStats(60, 8, 1f, 800L, 3f, 6f)
    )

    @Test
    fun `single player presence advances capture progress`() {
        val point = ControlPoint(id = "cp1", position = Vector2(5f, 5f), captureRadius = 3f)
        val state = GameState(
            0,
            emptyList(),
            20,
            20,
            units = mapOf(unit("p1", Vector2(5f, 5f)).let { it.id to it }),
            buildings = emptyMap(),
            resourceNodes = emptyMap(),
            playerResources = emptyMap(),
            factions = emptyMap(),
            controlPoints = mapOf(point.id to point)
        )

        val result = ControlPointSystem(captureRatePerSecond = 0.1f).update(state, deltaMs = 5000L) // 5s

        val updated = result.controlPoints["cp1"]!!
        assertEquals(PlayerId("p1"), updated.progressingPlayerId)
        assertTrue(updated.captureProgress in 0.4f..0.6f) // 5s * 0.1/s = 0.5
    }

    @Test
    fun `contested point makes no progress when two players present`() {
        val point = ControlPoint(id = "cp1", position = Vector2(5f, 5f), captureRadius = 3f)
        val state = GameState(
            0, emptyList(), 20, 20,
            units = mapOf(
                unit("p1", Vector2(5f, 5f)).let { it.id to it },
                unit("p2", Vector2(5.5f, 5f)).let { it.id to it }
            ),
            buildings = emptyMap(), resourceNodes = emptyMap(), playerResources = emptyMap(), factions = emptyMap(),
            controlPoints = mapOf(point.id to point)
        )

        val result = ControlPointSystem().update(state, deltaMs = 5000L)

        assertEquals(0f, result.controlPoints["cp1"]!!.captureProgress)
    }

    @Test
    fun `controlled point generates passive income`() {
        val point = ControlPoint(
            id = "cp1", position = Vector2(5f, 5f),
            controllingPlayerId = PlayerId("p1"),
            incomeEnergyPerSecond = 2, incomeMaterialPerSecond = 1
        )
        val state = GameState(
            0, emptyList(), 20, 20, emptyMap(), emptyMap(), emptyMap(),
            playerResources = mapOf(PlayerId("p1") to mapOf(ResourceType.ENERGY to 0, ResourceType.MATERIAL to 0)),
            factions = emptyMap(),
            controlPoints = mapOf(point.id to point)
        )

        val result = ControlPointSystem().update(state, deltaMs = 1000L) // 1s

        assertEquals(2, result.playerResources[PlayerId("p1")]?.get(ResourceType.ENERGY))
        assertEquals(1, result.playerResources[PlayerId("p1")]?.get(ResourceType.MATERIAL))
    }
}