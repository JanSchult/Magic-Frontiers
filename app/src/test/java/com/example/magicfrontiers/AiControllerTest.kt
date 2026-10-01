package com.example.magicfrontiers

import com.example.magicfrontiers.ai.AiController
import com.example.magicfrontiers.core.engine.Simulation
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import junit.framework.TestCase.assertTrue
import kotlin.collections.emptyList
import kotlin.test.Test

class AiControllerTest {

    @Test
    fun `ai builds barracks when affordable and none exists`() {
        val simulation = Simulation(
            GameState(
                tick = 0, map = emptyList(), mapWidth = 20, mapHeight = 20,
                units = mapOf(
                    UnitId("ai1") to GameUnit(
                        UnitId("ai1"), "scout", PlayerId("p2"), UnitRole.MELEE,
                        Vector2(10f, 10f), 60,
                        UnitStats(60, 8, 1f, 800L, 3f, 6f)
                    )
                ),
                buildings = emptyMap(), resourceNodes = emptyMap(),
                playerResources = mapOf(
                    PlayerId("p2") to mapOf(
                        ResourceType.ENERGY to 200,
                        ResourceType.MATERIAL to 200
                    )
                ),
                factions = emptyMap()
            )
        )
        val ai = AiController(simulation, PlayerId("p2"), PlayerId("p1"), decisionIntervalMs = 0L)

        ai.update(simulation.state.value, deltaMs = 100L)
        simulation.step(deltaMs = 100L) // Command verarbeiten

        assertTrue(simulation.state.value.buildings.values.any { it.typeId == "barracks" })
    }
}