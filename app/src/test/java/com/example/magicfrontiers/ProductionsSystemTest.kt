package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.system.ProductionSystem
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ProductionOrder
import com.example.magicfrontiers.core.model.Vector2
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class ProductionSystemTest {

    private fun baseState(building: Building) = GameState(
        tick = 0, map = emptyList(), mapWidth = 20, mapHeight = 20,
        units = emptyMap(), buildings = mapOf(building.id to building),
        resourceNodes = emptyMap(), playerResources = emptyMap(), factions = emptyMap()
    )

    @Test
    fun `building under construction progresses and does not produce`() {
        val building = Building(
            id = BuildingId("b1"), typeId = "barracks", ownerId = PlayerId("p1"),
            position = Vector2(0f, 0f), currentHealth = 1, maxHealth = 400,
            isConstructed = false, constructionProgress = 0f,
            productionQueue = listOf(ProductionOrder("scout", durationMs = 4000L))
        )
        val result = ProductionSystem().update(baseState(building), deltaMs = 5000L) // 5s > 10s Bauzeit? nein, teilweise

        val updated = result.buildings[building.id]!!
        assertTrue(updated.constructionProgress > 0f && updated.constructionProgress < 1f)
        assertTrue(updated.productionQueue.first().progress == 0f) // Queue läuft erst nach Fertigstellung
        assertTrue(result.units.isEmpty())
    }

    @Test
    fun `finished building spawns unit when order completes`() {
        val building = Building(
            id = BuildingId("b1"), typeId = "barracks", ownerId = PlayerId("p1"),
            position = Vector2(5f, 5f), currentHealth = 400, maxHealth = 400,
            isConstructed = true,
            productionQueue = listOf(ProductionOrder("scout", progress = 0.9f, durationMs = 4000L))
        )
        val result = ProductionSystem().update(baseState(building), deltaMs = 1000L) // +25% -> fertig

        assertEquals(1, result.units.size)
        val spawned = result.units.values.first()
        assertEquals("scout", spawned.typeId)
        assertEquals(PlayerId("p1"), spawned.ownerId)
        assertTrue(result.buildings[building.id]!!.productionQueue.isEmpty())
    }
}