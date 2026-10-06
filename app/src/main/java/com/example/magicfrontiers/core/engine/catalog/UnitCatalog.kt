package com.example.magicfrontiers.core.engine.catalog

import com.example.magicfrontiers.core.model.UnitBlueprint
import com.example.magicfrontiers.core.model.UnitRole
import com.example.magicfrontiers.core.model.UnitStats

object UnitCatalog {
    private val blueprints: Map<String, UnitBlueprint> = listOf(
        UnitBlueprint(
            typeId = "scout",
            displayName = "Kundschafter",
            role = UnitRole.MELEE,
            stats = UnitStats(
                maxHealth = 60,
                damage = 8,
                attackRange = 1f,
                attackCooldownMs = 800L,
                moveSpeed = 3.2f,
                visionRange = 6f
            ),
            costEnergy = 30, costMaterial = 10,
            buildDurationMs = 4000L
        ),
        UnitBlueprint(
            typeId = "archer",
            displayName = "Bogenschütze",
            role = UnitRole.RANGED,
            stats = UnitStats(
                maxHealth = 45,
                damage = 14,
                attackRange = 4f,
                attackCooldownMs = 1200L,
                moveSpeed = 2.4f,
                visionRange = 7f
            ),
            costEnergy = 40, costMaterial = 25,
            buildDurationMs = 6000L
        )
        // weitere Einheitentypen ergänzbar
    ).associateBy { it.typeId }

    fun get(typeId: String): UnitBlueprint =
        blueprints[typeId] ?: error("Unknown unit typeId: $typeId")

    fun getOrNull(typeId: String): UnitBlueprint? = blueprints[typeId]
}