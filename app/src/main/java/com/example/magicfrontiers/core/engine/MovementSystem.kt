package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.core.state.UnitAiState
import kotlin.math.max

class MovementSystem(
    private val arrivalThreshold: Float = 0.15f,
    private val separationRadius: Float = 0.6f,
    private val separationStrength: Float = 0.4f
) : GameSystem {

    private val grid = SpatialGrid(cellSize = separationRadius * 2f)


    override fun update(state: GameState, deltaMs: Long): GameState {
        val deltaSeconds = deltaMs / 1000f
        val unitsList = state.units.values.toList()
        if (unitsList.isEmpty()) return state

        grid.build(unitsList) // einmal pro Tick aufbauen statt pro Einheit zu iterieren

        val updatedUnits = state.units.toMutableMap()
        for (unit in unitsList) {
            val target = unit.moveTarget ?: continue
            if (unit.state != UnitAiState.Moving) continue

            val toTarget = target - unit.position
            val distance = toTarget.length()

            if (distance <= arrivalThreshold) {
                updatedUnits[unit.id] = unit.copy(moveTarget = null, state = UnitAiState.Idle)
                continue
            }


            val direction = Vector2(toTarget.x / distance, toTarget.y / distance)
            val separation = computeSeparation(unit, unitsList)

            val combinedX = direction.x + separation.x * separationStrength
            val combinedY = direction.y + separation.y * separationStrength
            val combinedLength = max(Vector2(combinedX, combinedY).length(), 0.0001f)

            val moveX = (combinedX / combinedLength) * unit.stats.moveSpeed * deltaSeconds
            val moveY = (combinedY / combinedLength) * unit.stats.moveSpeed * deltaSeconds

            val newPosition = Vector2(unit.position.x + moveX, unit.position.y + moveY)

            updatedUnits[unit.id] = unit.copy(position = newPosition)
        }

        return state.copy(units = updatedUnits)
    }

    /**
     * Einfache Abstoßung von nahen, befreundeten wie auch fremden Einheiten,
     * damit sich Gruppen nicht überlappen. O(n^2) - für MVP-Einheitenzahlen
     * (< 100 gleichzeitig) unproblematisch, siehe Risiko-Hinweis unten.
     */
    private fun computeSeparation(unit: GameUnit, allUnits: List<GameUnit>): Vector2 {
        var pushX = 0f
        var pushY = 0f

        // Nutzt das Grid für die Performanz, greift nur auf nahegelegene Einheiten zu
        for (other in grid.nearby(unit.position)) {
            if (other.id == unit.id) continue
            val diff = unit.position - other.position
            val dist = diff.length()
            if (dist in 0.0001f..separationRadius) {
                val strength = (separationRadius - dist) / separationRadius
                pushX += (diff.x / dist) * strength
                pushY += (diff.y / dist) * strength
            }
        }

        return Vector2(pushX, pushY)
    }
}