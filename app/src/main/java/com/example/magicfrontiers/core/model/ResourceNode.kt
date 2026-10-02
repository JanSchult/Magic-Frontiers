package com.example.magicfrontiers.core.model
import kotlinx.serialization.Serializable

@Serializable
data class ResourceNode(
    val id: String,
    val type: ResourceType,
    val position: Vector2,
    val remainingAmount: Int,
    val maxAmount: Int
)