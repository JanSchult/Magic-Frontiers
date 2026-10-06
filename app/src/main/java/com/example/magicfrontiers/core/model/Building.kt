package com.example.magicfrontiers.core.model
import kotlinx.serialization.Serializable

@Serializable
// core/model/Building.kt
data class Building(
    val id: BuildingId,
    val typeId: String,
    val ownerId: PlayerId,
    val position: Vector2,
    val currentHealth: Int,
    val maxHealth: Int,
    val productionQueue: List<ProductionOrder> = emptyList(),
    val isConstructed: Boolean = true,
    val constructionProgress: Float = 1f ,
    val researchQueue: List<ResearchOrder> = emptyList() // neu

)