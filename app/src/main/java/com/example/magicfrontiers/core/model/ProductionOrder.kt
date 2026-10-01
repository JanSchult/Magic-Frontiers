package com.example.magicfrontiers.core.model

data class ProductionOrder(
    val unitTypeId: String,
    val progress: Float = 0f, // 0..1
    val durationMs: Long
)