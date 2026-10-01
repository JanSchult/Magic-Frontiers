package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.CommandProcessor
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.Command
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.Vector2
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test

class CommandProcessorProductionTest {

    @Test
    fun `produce unit rejected when not enough resources`() {
        val building = Building(
            id = BuildingId("b1"), typeId = "barracks", ownerId = PlayerId("p1"),
            position = Vector2(0f, 0f), currentHealth = 400, maxHealth = 400, isConstructed = true
        )
        val state = GameState(
            tick = 0, map = emptyList(), mapWidth = 20, mapHeight = 20,
            units = emptyMap(), buildings = mapOf(building.id to building),
            resourceNodes = emptyMap(),
            playerResources = mapOf(
                PlayerId("p1") to mapOf(
                    ResourceType.ENERGY to 5,
                    ResourceType.MATERIAL to 0
                )
            ),
            factions = emptyMap()
        )
        val processor = CommandProcessor()
        processor.enqueue(listOf(Command.ProduceUnit(building.id, "scout"))) // kostet 30 Energie

        val result = processor.update(state, deltaMs = 100L)

        assertTrue(result.buildings[building.id]!!.productionQueue.isEmpty()) // Befehl verworfen
        assertEquals(5, result.playerResources[PlayerId("p1")]?.get(ResourceType.ENERGY)) // unverändert
    }
}