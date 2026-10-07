package com.example.magicfrontiers.core.model

import kotlinx.serialization.Serializable

@Serializable
data class FactionTraits(
    val startEnergy: Int,
    val startMaterial: Int,
    val resourceCostMultiplier: Float = 1f, // <1 = günstiger bauen, >1 = teurer
    val unitDamageMultiplier: Float = 1f,
    val unitHealthMultiplier: Float = 1f
)