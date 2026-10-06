package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.system.ResearchSystem
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResearchOrder
import com.example.magicfrontiers.core.model.Vector2
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class ResearchSystemTest {

    @Test
    fun `research completes and tech is added to researchedTechs`() {
        val building = Building(
            id = BuildingId("b1"), typeId = "lab", ownerId = PlayerId("p1"),
            position = Vector2(0f, 0f), currentHealth = 300, maxHealth = 300, isConstructed = true,
            researchQueue = listOf(
                ResearchOrder(
                    "efficient_gathering",
                    progress = 0.9f,
                    durationMs = 10_000L
                )
            )
        )
        val state = GameState(
            0, emptyList(), 20, 20, emptyMap(), mapOf(building.id to building),
            emptyMap(), emptyMap(), emptyMap()
        )

        val result = ResearchSystem().update(state, deltaMs = 2000L) // +20% -> fertig

        assertTrue("efficient_gathering" in (result.researchedTechs[PlayerId("p1")] ?: emptySet()))
        assertTrue(result.buildings[building.id]!!.researchQueue.isEmpty())
    }
}