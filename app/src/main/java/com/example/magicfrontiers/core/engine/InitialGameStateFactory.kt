package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.engine.catalog.FactionCatalog
import com.example.magicfrontiers.core.engine.catalog.UnitCatalog
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.ControlPoint
import com.example.magicfrontiers.core.model.FactionId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceNode
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.Vector2

object InitialGameStateFactory {
    fun create(
        mapWidth: Int = 20,
        mapHeight: Int = 20,
        playerFactionId: String,
        aiFactionId: String
    ): GameState {
        val playerFaction = FactionCatalog.get(playerFactionId)
        val aiFaction = FactionCatalog.get(aiFactionId)

        val playerStart = Vector2(3f, mapHeight - 3f)
        val aiStart = Vector2(mapWidth - 3f, 3f)

        val startingUnits = buildMap {
            put(UnitId("p1_scout_1"), startingScout(PlayerId("p1"), playerStart))
            put(UnitId("p1_scout_2"), startingScout(PlayerId("p1"), playerStart + Vector2(1f, 0f)))
            put(UnitId("p2_scout_1"), startingScout(PlayerId("p2"), aiStart))
            put(UnitId("p2_scout_2"), startingScout(PlayerId("p2"), aiStart + Vector2(1f, 0f)))
        }
        val resourceNodes = mapOf(
            "node_p1" to ResourceNode(
                "node_p1",
                ResourceType.MATERIAL,
                playerStart + Vector2(3f, 0f),
                500,
                500
            ),
            "node_p2" to ResourceNode("node_p2", ResourceType.MATERIAL, aiStart + Vector2(-3f, 0f), 500, 500)
        )
        val startingBuildings = buildMap {
            put(BuildingId("p1_barracks"), startingBarracks(PlayerId("p1"), playerStart + Vector2(-1.5f, -1.5f)))
            put(BuildingId("p2_barracks"), startingBarracks(PlayerId("p2"), aiStart + Vector2(1.5f, 1.5f)))
        }

        val controlPoints = listOf(
            ControlPoint(id = "cp_center", position = Vector2(mapWidth / 2f, mapHeight / 2f)),
            ControlPoint(id = "cp_north", position = Vector2(mapWidth / 2f, mapHeight * 0.2f)),
            ControlPoint(id = "cp_south", position = Vector2(mapWidth / 2f, mapHeight * 0.8f))
        ).associateBy { it.id }

        return GameState(
            tick = 0,
            map = MapGenerator.generateInitialMap(mapWidth, mapHeight),
            mapWidth = mapWidth,
            mapHeight = mapHeight,
            units = startingUnits,
            buildings = startingBuildings,
            resourceNodes = resourceNodes,
            playerResources = mapOf(
                PlayerId("p1") to mapOf(
                    ResourceType.ENERGY to playerFaction.traits.startEnergy,
                    ResourceType.MATERIAL to playerFaction.traits.startMaterial
                ),
                PlayerId("p2") to mapOf(
                    ResourceType.ENERGY to aiFaction.traits.startEnergy,
                    ResourceType.MATERIAL to aiFaction.traits.startMaterial
                )
            ),
            factions = mapOf(
                PlayerId("p1") to playerFaction.id,
                PlayerId("p2") to aiFaction.id
            ),
            controlPoints = controlPoints
        )
    }

    private fun startingScout(owner: PlayerId, pos: Vector2) = GameUnit(
        id = UnitId("scout_${owner.value}_${pos.x}_${pos.y}"),
        typeId = "scout",
        ownerId = owner,
        role = UnitRole.MELEE,
        position = pos,
        currentHealth = 60,
        stats = UnitCatalog.get("scout").stats
    )

    private fun startingBarracks(owner: PlayerId, pos: Vector2) = Building(
        id = BuildingId("barracks_${owner.value}"),
        typeId = "barracks",
        ownerId = owner,
        position = pos,
        currentHealth = 400,
        maxHealth = 400,
        isConstructed = true
    )
}