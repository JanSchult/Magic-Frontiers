package com.example.magicfrontiers.core.engine.system

import com.example.magicfrontiers.core.engine.Pathfinder
import com.example.magicfrontiers.core.engine.TechEffects
import com.example.magicfrontiers.core.model.CellKey
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.state.UnitAiState

class CombatSystem : GameSystem {

    override fun update(state: GameState, deltaMs: Long): GameState {
        var units = state.units

        // 1. Cooldowns runterzählen
        units = units.mapValues { (_, unit) ->
            if (unit.attackCooldownRemainingMs > 0) {
                unit.copy(attackCooldownRemainingMs = (unit.attackCooldownRemainingMs - deltaMs).coerceAtLeast(0L))
            } else unit
        }

        val damageEvents = mutableListOf<Pair<UnitId, Int>>() // targetId -> damage
        var updatedUnits = units.toMutableMap()

        for (unit in units.values) {
            val targetId = unit.targetUnitId ?: continue
            val target = units[targetId]

            if (target == null) {
                updatedUnits[unit.id] = updatedUnits[unit.id]!!.copy(targetUnitId = null, state = UnitAiState.Idle)
                continue
            }
// Sichtbarkeitsprüfung: Ziel muss für den Angreifer-Besitzer sichtbar sein (eigene Ziele immer ok)
            val attackerVisibility = state.visibilityByPlayer[unit.ownerId] ?: emptySet()
            val targetCell = CellKey(target.position.x.toInt(), target.position.y.toInt())
            val canSeeTarget = target.ownerId == unit.ownerId || targetCell in attackerVisibility

            if (!canSeeTarget) {
                updatedUnits[unit.id] = updatedUnits[unit.id]!!.copy(
                    targetUnitId = null,
                    state = UnitAiState.Idle,
                    moveTarget = null
                )
                continue
            }


            val distance = (target.position - unit.position).length()

            if (distance > unit.stats.attackRange) {
                // Ziel außer Reichweite -> annähern lassen (MovementSystem übernimmt im nächsten Tick)
                val path = Pathfinder.findPath(state, unit.position, target.position)
                updatedUnits[unit.id] = updatedUnits[unit.id]!!.copy(
                    pathWaypoints = path,
                    moveTarget = path.firstOrNull() ?: target.position,
                    state = UnitAiState.Moving
                )
                continue
            }

            // In Reichweite: angreifen, falls Cooldown abgelaufen
            if (unit.attackCooldownRemainingMs <= 0L) {
                val baseDamage = TechEffects.effectiveDamage(unit, state)
                val targetArmor = TechEffects.effectiveArmor(target, state)
                val damage = (baseDamage - targetArmor).coerceAtLeast(1)
                damageEvents += targetId to damage

                updatedUnits[unit.id] = updatedUnits[unit.id]!!.copy(
                    attackCooldownRemainingMs = unit.stats.attackCooldownMs,
                    state = UnitAiState.Attacking,
                    moveTarget = null
                )
            }
        }

        // 2. Schaden anwenden (mehrere Angreifer können dasselbe Ziel im selben Tick treffen)
        for ((targetId, damage) in damageEvents) {
            val current = updatedUnits[targetId] ?: continue
            updatedUnits[targetId] = current.copy(
                currentHealth = (current.currentHealth - damage).coerceAtLeast(0)
            )
        }

        // 3. Tote entfernen
        val alive = updatedUnits.filterValues { it.currentHealth > 0 }

        return state.copy(units = alive)
    }
}