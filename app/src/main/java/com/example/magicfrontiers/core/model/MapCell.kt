package com.example.magicfrontiers.core.model

// core/model/MapCell.kt
data class MapCell(
    val x: Int,
    val y: Int,
    val isWalkable: Boolean,
    val isVisible: Boolean = false,   // aktuell sichtbar
    val isExplored: Boolean = false,  // Fog of War: bereits aufgedeckt
    val occupantUnitId: UnitId? = null
)
