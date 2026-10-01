package com.example.magicfrontiers.input

import androidx.compose.ui.geometry.Offset
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.render.Camera
import kotlin.math.max
import kotlin.math.min

class SelectionController(
    private val localPlayerId: PlayerId
) {

    fun selectUnitsInDragRect(
        state: GameState,
        camera: Camera,
        dragStart: Offset,
        dragEnd: Offset
    ): Set<UnitId> {
        val minX = min(dragStart.x, dragEnd.x)
        val maxX = max(dragStart.x, dragEnd.x)
        val minY = min(dragStart.y, dragEnd.y)
        val maxY = max(dragStart.y, dragEnd.y)

        return state.units.values
            .filter { it.ownerId == localPlayerId } // nur eigene Einheiten auswählbar
            .filter { unit ->
                val screenPos = camera.worldToScreen(unit.position)
                screenPos.x in minX..maxX && screenPos.y in minY..maxY
            }
            .map { it.id }
            .toSet()
    }

    /** Tap-Selektion: findet die nächste Einheit im Tap-Radius (mit vergrößerter Hitbox für Touch). */
    fun selectUnitAtTap(
        state: GameState,
        camera: Camera,
        tapPosition: Offset,
        tapRadiusPx: Float = 48f // großzügige Touch-Hitbox, siehe Risiko-Hinweis aus Schritt 1
    ): UnitId? {
        return state.units.values
            .filter { it.ownerId == localPlayerId }
            .map { it to camera.worldToScreen(it.position) }
            .filter { (_, screenPos) ->
                val dx = screenPos.x - tapPosition.x
                val dy = screenPos.y - tapPosition.y
                (dx * dx + dy * dy) <= tapRadiusPx * tapRadiusPx
            }
            .minByOrNull { (_, screenPos) ->
                val dx = screenPos.x - tapPosition.x
                val dy = screenPos.y - tapPosition.y
                dx * dx + dy * dy
            }
            ?.first?.id
    }

    /** Findet ein beliebiges Ziel (auch gegnerisch) an Tap-Position — für Attack-Befehle. */
    fun findAnyUnitAtTap(
        state: GameState,
        camera: Camera,
        tapPosition: Offset,
        tapRadiusPx: Float = 48f
    ): UnitId? {
        return state.units.values
            .map { it to camera.worldToScreen(it.position) }
            .filter { (_, screenPos) ->
                val dx = screenPos.x - tapPosition.x
                val dy = screenPos.y - tapPosition.y
                (dx * dx + dy * dy) <= tapRadiusPx * tapRadiusPx
            }
            .minByOrNull { (_, screenPos) ->
                val dx = screenPos.x - tapPosition.x
                val dy = screenPos.y - tapPosition.y
                dx * dx + dy * dy
            }
            ?.first?.id
    }
}
