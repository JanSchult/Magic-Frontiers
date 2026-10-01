package com.example.magicfrontiers.core.model

data class BuildingBlueprint(
    val typeId: String,
    val displayName: String,
    val maxHealth: Int,
    val costEnergy: Int,
    val costMaterial: Int,
    val constructionDurationMs: Long,
    val producesUnitTypeIds: List<String>
)