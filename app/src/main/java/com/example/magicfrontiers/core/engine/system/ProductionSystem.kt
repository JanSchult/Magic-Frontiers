package com.example.magicfrontiers.core.engine.system

import com.example.magicfrontiers.core.engine.catalog.BuildingCatalog
import com.example.magicfrontiers.core.engine.catalog.UnitCatalog
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.core.engine.catalog.FactionCatalog

class ProductionSystem(
    private val spawnOffset: Float = 1.5f
) : GameSystem {

    override fun update(state: GameState, deltaMs: Long): GameState {
        var units = state.units
        var buildings = state.buildings
        var nextUnitCounter = state.units.size

        for (building in state.buildings.values) {

            // 1. Gebäude selbst im Bau -> Baufortschritt vorantreiben
            if (!building.isConstructed) {
                val blueprint = BuildingCatalog.getOrNull(building.typeId) ?: continue
                val progressDelta = deltaMs.toFloat() / blueprint.constructionDurationMs
                val newProgress = (building.constructionProgress + progressDelta).coerceAtMost(1f)
                val newHealth = (blueprint.maxHealth * newProgress).toInt().coerceAtLeast(1)

                buildings = buildings + (building.id to building.copy(
                    constructionProgress = newProgress,
                    currentHealth = newHealth,
                    isConstructed = newProgress >= 1f
                ))
                continue // fertiges Gebäude produziert erst ab nächstem Tick
            }

            // 2. Produktionsqueue abarbeiten
            if (building.productionQueue.isEmpty()) continue
            val currentOrder = building.productionQueue.first()
            val progressDelta = deltaMs.toFloat() / currentOrder.durationMs
            val newProgress = currentOrder.progress + progressDelta

            if (newProgress >= 1f) {
                // Einheit fertig -> spawnen, aus Queue entfernen
                val blueprint = UnitCatalog.getOrNull(currentOrder.unitTypeId) ?: continue
                val factionId = state.factions[building.ownerId]
                val healthMultiplier = factionId?.let { FactionCatalog.get(it.value).traits.unitHealthMultiplier } ?: 1f
                val scaledMaxHealth = (blueprint.stats.maxHealth * healthMultiplier).toInt()


                val spawnPosition = Vector2(
                    building.position.x + spawnOffset,
                    building.position.y
                )
                val newUnit = GameUnit(
                    id = UnitId("u_${state.tick}_${nextUnitCounter++}"),
                    typeId = blueprint.typeId,
                    ownerId = building.ownerId,
                    role = blueprint.role,
                    position = spawnPosition,
                    currentHealth = blueprint.stats.maxHealth,
                    stats = blueprint.stats.copy(maxHealth = scaledMaxHealth)
                )
                units = units + (newUnit.id to newUnit)

                buildings = buildings + (building.id to building.copy(
                    productionQueue = building.productionQueue.drop(1)
                ))
            } else {
                val updatedOrder = currentOrder.copy(progress = newProgress)
                buildings = buildings + (building.id to building.copy(
                    productionQueue = listOf(updatedOrder) + building.productionQueue.drop(1)
                ))
            }
        }

        return state.copy(units = units, buildings = buildings)
    }
}