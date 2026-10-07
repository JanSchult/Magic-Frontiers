package com.example.magicfrontiers.core.model
import kotlinx.serialization.Serializable

@Serializable
// core/model/MapCell.kt
data class MapCell(
    val x: Int,
    val y: Int,
    val terrainType: TerrainType,
    val isExplored: Boolean = false,  // Fog of War: bereits aufgedeckt
    val occupantUnitId: UnitId? = null
){
    val isWalkable: Boolean get() = terrainType != TerrainType.WATER

}
