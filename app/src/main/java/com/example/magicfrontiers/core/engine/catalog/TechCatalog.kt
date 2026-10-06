package com.example.magicfrontiers.core.engine.catalog

import com.example.magicfrontiers.core.model.TechBlueprint
import com.example.magicfrontiers.core.model.TechEffect

object TechCatalog {
    private val blueprints: Map<String, TechBlueprint> = listOf(
        TechBlueprint(
            id = "sharpened_blades",
            displayName = "Geschärfte Klingen",
            costEnergy = 60, costMaterial = 40,
            researchDurationMs = 15_000L,
            effect = TechEffect.DamageBonus(unitTypeId = "scout", flatBonus = 4)
        ),
        TechBlueprint(
            id = "reinforced_armor",
            displayName = "Verstärkte Rüstung",
            costEnergy = 80, costMaterial = 60,
            researchDurationMs = 20_000L,
            requiresTechIds = listOf("sharpened_blades"),
            effect = TechEffect.ArmorBonus(unitTypeId = "scout", flatBonus = 2)
        ),
        TechBlueprint(
            id = "efficient_gathering",
            displayName = "Effiziente Ressourcengewinnung",
            costEnergy = 50, costMaterial = 30,
            researchDurationMs = 12_000L,
            effect = TechEffect.GatherRateBonus(percentBonus = 0.25f)
        )
        // weitere Technologien ergänzbar
    ).associateBy { it.id }

    fun get(id: String): TechBlueprint = blueprints[id] ?: error("Unknown tech id: $id")
    fun getOrNull(id: String): TechBlueprint? = blueprints[id]
    fun all(): Collection<TechBlueprint> = blueprints.values
}