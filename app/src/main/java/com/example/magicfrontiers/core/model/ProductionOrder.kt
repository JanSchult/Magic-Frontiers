package com.example.magicfrontiers.core.model
import kotlinx.serialization.Serializable

@Serializable
data class ProductionOrder(
    val unitTypeId: String,
    val progress: Float = 0f, // 0..1
    val durationMs: Long
)