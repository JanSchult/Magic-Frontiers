package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.FactionId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType

object InitialGameStateFactory {
    fun create(mapWidth: Int = 20, mapHeight: Int = 20): GameState {
        return GameState(
            tick = 0,
            map = MapGenerator.generateInitialMap(mapWidth, mapHeight),
            mapWidth = mapWidth,
            mapHeight = mapHeight,
            units = emptyMap(),
            buildings = emptyMap(),
            resourceNodes = emptyMap(),
            playerResources = mapOf(
                PlayerId("p1") to mapOf(ResourceType.ENERGY to 150, ResourceType.MATERIAL to 100),
                PlayerId("p2") to mapOf(ResourceType.ENERGY to 150, ResourceType.MATERIAL to 100)
            ),
            factions = mapOf(
                PlayerId("p1") to FactionId("human"),
                PlayerId("p2") to FactionId("ai")
            )
        )
    }
}