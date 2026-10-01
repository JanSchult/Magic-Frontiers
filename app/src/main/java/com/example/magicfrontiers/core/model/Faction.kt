package com.example.magicfrontiers.core.model

// core/model/Faction.kt
data class Faction(
    val id: FactionId,
    val name: String,
    val description: String,
    val unitTypeIds: List<String>,
    val buildingTypeIds: List<String>,
    val techTreeId: String
)