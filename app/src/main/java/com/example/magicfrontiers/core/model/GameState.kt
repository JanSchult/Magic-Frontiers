package com.example.magicfrontiers.core.model
import kotlinx.serialization.Serializable

@Serializable
// core/model/GameState.kt
data class GameState(
    val tick: Long,
    val map: List<MapCell>,
    val mapWidth: Int,
    val mapHeight: Int,
    val units: Map<UnitId, GameUnit>,
    val buildings: Map<BuildingId, Building>,
    val resourceNodes: Map<String, ResourceNode>,
    val playerResources: Map<PlayerId, Map<ResourceType, Int>>,
    val factions: Map<PlayerId, FactionId>
)