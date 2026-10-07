package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.engine.catalog.FactionCatalog
import com.example.magicfrontiers.core.engine.catalog.TechCatalog
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.TechEffect

object TechEffects {

    fun effectiveDamage(unit: GameUnit, state: GameState): Int {
        val researched = state.researchedTechs[unit.ownerId] ?: emptySet()
        val techBonus = researched.sumOf { techId ->
            val effect = TechCatalog.getOrNull(techId)?.effect
            if (effect is TechEffect.DamageBonus && effect.unitTypeId == unit.typeId) effect.flatBonus else 0
        }
        val factionId = state.factions[unit.ownerId]
        val factionMultiplier = factionId?.let { FactionCatalog.get(it.value).traits.unitDamageMultiplier } ?: 1f
        return ((unit.stats.damage + techBonus) * factionMultiplier).toInt()
    }

    fun effectiveArmor(unit: GameUnit, state: GameState): Int {
        val researched = state.researchedTechs[unit.ownerId] ?: emptySet()
        val bonus = researched.sumOf { techId ->
            val effect = TechCatalog.getOrNull(techId)?.effect
            if (effect is TechEffect.ArmorBonus && effect.unitTypeId == unit.typeId) effect.flatBonus else 0
        }
        return unit.stats.armor + bonus
    }

    fun gatherRateMultiplier(unit: GameUnit, state: GameState): Float {
        val researched = state.researchedTechs[unit.ownerId] ?: emptySet()
        val bonus = researched.sumOf { techId ->
            val effect = TechCatalog.getOrNull(techId)?.effect
            if (effect is TechEffect.GatherRateBonus) effect.percentBonus.toDouble() else 0.0
        }
        return 1f + bonus.toFloat()
    }
}