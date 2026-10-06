package com.example.magicfrontiers.core.engine.catalog

import com.example.magicfrontiers.core.model.BuildingBlueprint

object BuildingCatalog {
    private val blueprints: Map<String, BuildingBlueprint> = listOf(
        BuildingBlueprint(
            typeId = "barracks",
            displayName = "Kaserne",
            maxHealth = 400,
            costEnergy = 80, costMaterial = 60,
            constructionDurationMs = 10_000L,
            producesUnitTypeIds = listOf("scout", "archer")
        ),

        BuildingBlueprint(
            typeId = "lab",
            displayName = "Forschungslabor",
            maxHealth = 300,
            costEnergy = 100, costMaterial = 80,
            constructionDurationMs = 12_000L,
            producesUnitTypeIds = emptyList() // Labor produziert keine Einheiten, nur Forschung
    )
        // weitere Gebäudetypen ergänzbar

    ).associateBy { it.typeId }

    fun get(typeId: String): BuildingBlueprint =
        blueprints[typeId] ?: error("Unknown building typeId: $typeId")

    fun getOrNull(typeId: String): BuildingBlueprint? = blueprints[typeId]
}