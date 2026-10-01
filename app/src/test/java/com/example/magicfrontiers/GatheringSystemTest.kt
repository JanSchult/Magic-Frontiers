package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.GatheringSystem
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceNode
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.core.state.UnitAiState
import kotlin.test.Test
import kotlin.test.assertEquals

// test/.../GatheringSystemTest.kt
class GatheringSystemTest {

    private fun worker(pos: Vector2, state: UnitAiState, nodeId: String? = null, carried: Int = 0) =
        GameUnit(
            id = UnitId("w1"), typeId = "worker", ownerId = PlayerId("p1"),
            role = UnitRole.SUPPORT, position = pos, currentHealth = 50,
            stats = UnitStats(
                maxHealth = 50, damage = 0, attackRange = 0f, attackCooldownMs = 0L,
                moveSpeed = 2f, visionRange = 4f,
                canGather = true, gatherCapacity = 10, gatherRatePerSecond = 5
            ),
            state = state, gatherTargetNodeId = nodeId, carriedResourceAmount = carried
        )

    private fun baseState(unit: GameUnit, node: ResourceNode, building: Building? = null) =
        GameState(
            tick = 0, map = emptyList(), mapWidth = 20, mapHeight = 20,
            units = mapOf(unit.id to unit),
            buildings = building?.let { mapOf(it.id to it) } ?: emptyMap(),
            resourceNodes = mapOf(node.id to node),
            playerResources = mapOf(PlayerId("p1") to mapOf(ResourceType.MATERIAL to 0)),
            factions = emptyMap()
        )

    @Test
    fun `unit starts gathering upon arrival at node`() {
        val node = ResourceNode("n1", ResourceType.MATERIAL, Vector2(5f, 5f), 100, 100)
        val unit = worker(Vector2(5.1f, 5f), UnitAiState.MovingToGather, nodeId = "n1")
        val result = GatheringSystem().update(baseState(unit, node), deltaMs = 100L)

        assertEquals(UnitAiState.Gathering, result.units[unit.id]?.state)
    }

    @Test
    fun `unit accumulates resource while gathering`() {
        val node = ResourceNode("n1", ResourceType.MATERIAL, Vector2(5f, 5f), 100, 100)
        val unit = worker(Vector2(5f, 5f), UnitAiState.Gathering, nodeId = "n1")
        val result = GatheringSystem().update(baseState(unit, node), deltaMs = 1000L) // 1s * 5/s = 5

        assertEquals(5, result.units[unit.id]?.carriedResourceAmount)
        assertEquals(95, result.resourceNodes["n1"]?.remainingAmount)
    }

    @Test
    fun `full unit returns resource to building and resets`() {
        val node = ResourceNode("n1", ResourceType.MATERIAL, Vector2(20f, 20f), 100, 100)
        val building = Building(
            id = BuildingId("b1"), typeId = "barracks", ownerId = PlayerId("p1"),
            position = Vector2(0f, 0f), currentHealth = 400, maxHealth = 400, isConstructed = true
        )
        val unit = worker(Vector2(0.2f, 0f), UnitAiState.ReturningResource, nodeId = "n1", carried = 10)
            .copy(carriedResourceType = ResourceType.MATERIAL)

        val result = GatheringSystem().update(baseState(unit, node, building), deltaMs = 100L)

        assertEquals(10, result.playerResources[PlayerId("p1")]?.get(ResourceType.MATERIAL))
        assertEquals(0, result.units[unit.id]?.carriedResourceAmount)
        assertEquals(UnitAiState.MovingToGather, result.units[unit.id]?.state) // Node noch nicht leer -> zurück
    }
}