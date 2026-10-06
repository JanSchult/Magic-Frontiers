package com.example.magicfrontiers.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TechBlueprint(
    val id: String,
    val displayName: String,
    val costEnergy: Int,
    val costMaterial: Int,
    val researchDurationMs: Long,
    val requiresTechIds: List<String> = emptyList(),
    val effect: TechEffect
)