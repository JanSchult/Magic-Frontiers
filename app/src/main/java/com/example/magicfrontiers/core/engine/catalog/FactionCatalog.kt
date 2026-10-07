package com.example.magicfrontiers.core.engine.catalog

import com.example.magicfrontiers.core.model.Faction
import com.example.magicfrontiers.core.model.FactionId
import com.example.magicfrontiers.core.model.FactionTraits

object FactionCatalog {
    private val factions: Map<String, Faction> = listOf(
        Faction(
            id = FactionId("ember_dominion"),
            name = "Ember-Dominion",
            description = "Aggressive Kriegertruppen mit erhöhtem Schaden, dafür zerbrechlicher. Favorisiert schnelle Angriffe vor Wirtschaftsaufbau.",
            unitTypeIds = listOf("scout", "archer"),
            buildingTypeIds = listOf("barracks", "lab"),
            techTreeId = "default",
            traits = FactionTraits(
                startEnergy = 140, startMaterial = 90,
                unitDamageMultiplier = 1.15f,
                unitHealthMultiplier = 0.9f
            )
        ),
        Faction(
            id = FactionId("verdant_concord"),
            name = "Verdant-Konkordat",
            description = "Wirtschaftlich orientierte Fraktion mit robusteren Einheiten und günstigeren Baukosten. Favorisiert längere Partien.",
            unitTypeIds = listOf("scout", "archer"),
            buildingTypeIds = listOf("barracks", "lab"),
            techTreeId = "default",
            traits = FactionTraits(
                startEnergy = 160, startMaterial = 110,
                resourceCostMultiplier = 0.9f,
                unitHealthMultiplier = 1.15f
            )
        )
    ).associateBy { it.id.value }

    fun get(id: String): Faction = factions[id] ?: error("Unknown faction id: $id")
    fun all(): List<Faction> = factions.values.toList()
}