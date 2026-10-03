package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.CellKey
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.Vector2
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

class VisionSystem(
    private val localPlayerId: PlayerId.Companion // für Singleplayer: nur dieser Spieler steuert Fog of War im Renderer
) : GameSystem {

    override fun update(state: GameState, deltaMs: Long): GameState {
        val allPlayerIds = (state.units.values.map { it.ownerId } + state.buildings.values.map { it.ownerId }).toSet()
        if (allPlayerIds.isEmpty()) return state

        val newVisibility = allPlayerIds.associateWith { playerId -> computeVisibleCells(state, playerId) }

        // isExplored nur für den lokalen Spieler fortschreiben (Renderer-Zweck)
        // -> wird jetzt außerhalb berechnet, siehe ExploredMapUpdater unten
        return state.copy(visibilityByPlayer = newVisibility)
    }

    private fun computeVisibleCells(state: GameState, playerId: PlayerId): Set<CellKey> {
        val sources = mutableListOf<Pair<Vector2, Float>>() // Position + Sichtradius

        state.units.values
            .filter { it.ownerId == playerId }
            .forEach { sources += it.position to it.stats.visionRange }

        state.buildings.values
            .filter { it.ownerId == playerId && it.isConstructed }
            .forEach { sources += it.position to DEFAULT_BUILDING_VISION_RANGE }

        if (sources.isEmpty()) return emptySet()

        val result = mutableSetOf<CellKey>()
        for ((position, range) in sources) {
            val minX = floor(position.x - range).toInt()
            val maxX = ceil(position.x + range).toInt()
            val minY = floor(position.y - range).toInt()
            val maxY = ceil(position.y + range).toInt()

            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    val dx = x - position.x
                    val dy = y - position.y
                    if (sqrt(dx * dx + dy * dy) <= range) {
                        result += CellKey(x, y)
                    }
                }
            }
        }
        return result
    }

    companion object {
        private const val DEFAULT_BUILDING_VISION_RANGE = 5f
    }
}