package com.example.magicfrontiers.core.model

data class UnitBlueprint(
    val typeId: String,
    val displayName: String,
    val role: UnitRole,
    val stats: UnitStats,
    val costEnergy: Int,
    val costMaterial: Int,
    val buildDurationMs: Long
)