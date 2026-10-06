package com.example.magicfrontiers.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ResearchOrder(
    val techId: String,
    val progress: Float = 0f,
    val durationMs: Long
)