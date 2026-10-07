package com.example.magicfrontiers.core.model
import kotlinx.serialization.Serializable

@Serializable
// core/model/Faction.kt
data class Faction(
    val id: FactionId,
    val name: String,
    val description: String,
    val unitTypeIds: List<String>,
    val buildingTypeIds: List<String>,
    val techTreeId: String,
    val traits: FactionTraits
)