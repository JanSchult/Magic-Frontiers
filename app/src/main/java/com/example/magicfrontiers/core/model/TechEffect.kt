package com.example.magicfrontiers.core.model

import kotlinx.serialization.Serializable
@Serializable
sealed interface TechEffect {
    @Serializable data class DamageBonus(val unitTypeId: String, val flatBonus: Int) : TechEffect
    @Serializable data class ArmorBonus(val unitTypeId: String, val flatBonus: Int) : TechEffect
    @Serializable data class GatherRateBonus(val percentBonus: Float) : TechEffect
    @Serializable data class UnlockUnit(val unitTypeId: String) : TechEffect
}