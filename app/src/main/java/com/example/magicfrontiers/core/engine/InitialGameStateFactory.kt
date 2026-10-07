package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.engine.catalog.FactionCatalog
import com.example.magicfrontiers.core.model.ControlPoint
import com.example.magicfrontiers.core.model.FactionId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType
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
            units = emptyMap(),
            buildings = emptyMap(),
            resourceNodes = emptyMap(),
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
}