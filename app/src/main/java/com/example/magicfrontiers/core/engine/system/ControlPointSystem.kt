package com.example.magicfrontiers.core.engine.system

import com.example.magicfrontiers.core.model.ControlPoint
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType


class ControlPointSystem(
    private val captureRatePerSecond: Float = 0.1f // 10 Sekunden für volle Einnahme bei ungestörter Präsenz
) : GameSystem {

    override fun update(state: GameState, deltaMs: Long): GameState {
        if (state.controlPoints.isEmpty()) return state
        val deltaSeconds = deltaMs / 1000f

        var updatedPoints = state.controlPoints
        var updatedResources = state.playerResources

        for (point in state.controlPoints.values) {
            val presentPlayers = presentPlayersAt(point, state.units.values)

            val newPoint = when {
                presentPlayers.isEmpty() -> point // niemand da -> Status quo, kein Fortschritt/Rückgang

                presentPlayers.size == 1 -> {
                    val contestingPlayer = presentPlayers.first()
                    advanceCapture(point, contestingPlayer, deltaSeconds)
                }

                else -> {
                    // mehrere Fraktionen gleichzeitig präsent -> Patt, kein Fortschritt (umkämpfter Punkt)
                    point
                }
            }

            updatedPoints = updatedPoints + (point.id to newPoint)

            // Einkommen nur für den tatsächlichen Besitzer, unabhängig von aktuellem Gefecht am Punkt
            newPoint.controllingPlayerId?.let { ownerId ->
                val currentPool = updatedResources[ownerId] ?: emptyMap()
                val updatedPool = currentPool.toMutableMap().apply {
                    this[ResourceType.ENERGY] = (this[ResourceType.ENERGY] ?: 0) +
                            (newPoint.incomeEnergyPerSecond * deltaSeconds).toInt()
                    this[ResourceType.MATERIAL] = (this[ResourceType.MATERIAL] ?: 0) +
                            (newPoint.incomeMaterialPerSecond * deltaSeconds).toInt()
                }
                updatedResources = updatedResources + (ownerId to updatedPool)
            }
        }

        return state.copy(controlPoints = updatedPoints, playerResources = updatedResources)
    }

    private fun presentPlayersAt(point: ControlPoint, units: Collection<GameUnit>): Set<PlayerId> =
        units.filter { (it.position - point.position).length() <= point.captureRadius }
            .map { it.ownerId }
            .toSet()

    private fun advanceCapture(point: ControlPoint, contestingPlayer: PlayerId, deltaSeconds: Float): ControlPoint {
        // Fall 1: Punkt gehört bereits diesem Spieler -> nichts zu tun, bereits voll kontrolliert
        if (point.controllingPlayerId == contestingPlayer) return point

        // Fall 2: Derselbe Spieler setzt eine bereits begonnene Einnahme fort
        if (point.progressingPlayerId == contestingPlayer) {
            val newProgress = (point.captureProgress + captureRatePerSecond * deltaSeconds).coerceAtMost(1f)
            return if (newProgress >= 1f) {
                point.copy(
                    controllingPlayerId = contestingPlayer,
                    progressingPlayerId = null,
                    captureProgress = 0f
                )
            } else {
                point.copy(captureProgress = newProgress)
            }
        }

        // Fall 3: Ein anderer Spieler beginnt neu einzunehmen -> vorherigen Fortschritt zurücksetzen, neu starten
        return point.copy(
            progressingPlayerId = contestingPlayer,
            captureProgress = captureRatePerSecond * deltaSeconds
        )
    }
}