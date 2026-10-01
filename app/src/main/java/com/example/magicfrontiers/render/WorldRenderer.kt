package com.example.magicfrontiers.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType
import com.example.magicfrontiers.core.model.Vector2
import com.example.magicfrontiers.input.SelectionState

object WorldRenderer {

    fun draw(
        scope: DrawScope,
        state: GameState,
        camera: Camera,
        selection: SelectionState,
        localPlayerId: PlayerId
    ) {
        drawMapGrid(scope, state, camera)
        drawResourceNodes(scope, state, camera)
        drawBuildings(scope, state, camera)
        drawUnits(scope, state, camera, selection, localPlayerId)
        if (selection.isDragging) {
            drawSelectionRect(scope, selection)
        }
    }

    private fun drawMapGrid(scope: DrawScope, state: GameState, camera: Camera) {
        for (cell in state.map) {
            if (!cell.isExplored) continue // Fog of War kommt in Schritt 8, hier schon vorbereitet
            val screenPos = camera.worldToScreen(Vector2(cell.x.toFloat(), cell.y.toFloat()))
            val color = if (cell.isWalkable) Color(0xFF2A3B2A) else Color(0xFF3B2A2A)
            scope.drawRect(
                color = color.copy(alpha = if (cell.isVisible) 1f else 0.5f),
                topLeft = Offset(screenPos.x, screenPos.y),
                size = androidx.compose.ui.geometry.Size(camera.zoom, camera.zoom)
            )
        }
    }

    private fun drawResourceNodes(scope: DrawScope, state: GameState, camera: Camera) {
        for (node in state.resourceNodes.values) {
            val screenPos = camera.worldToScreen(node.position)
            val color = when (node.type) {
                ResourceType.ENERGY -> Color(0xFF4FC3F7)
                ResourceType.MATERIAL -> Color(0xFFA1887F)
                ResourceType.RARE -> Color(0xFFCE93D8)
            }
            scope.drawCircle(color = color, radius = camera.zoom * 0.35f, center = Offset(screenPos.x, screenPos.y))
        }
    }

    private fun drawBuildings(scope: DrawScope, state: GameState, camera: Camera) {
        for (building in state.buildings.values) {
            val screenPos = camera.worldToScreen(building.position)
            val size = camera.zoom * 1.2f
            val baseColor = if (building.isConstructed) Color(0xFF616161) else Color(0xFF616161).copy(alpha = 0.5f)

            scope.drawRect(
                color = baseColor,
                topLeft = Offset(screenPos.x - size / 2, screenPos.y - size / 2),
                size = androidx.compose.ui.geometry.Size(size, size)
            )

            // Baufortschritt-Balken
            if (!building.isConstructed) {
                drawProgressBar(scope, screenPos, size, building.constructionProgress, Color(0xFFFFC107))
            }
            // Health-Bar nur wenn beschädigt
            else if (building.currentHealth < building.maxHealth) {
                drawProgressBar(scope, screenPos, size, building.currentHealth.toFloat() / building.maxHealth, Color(0xFF4CAF50))
            }
        }
    }

    private fun drawUnits(
        scope: DrawScope,
        state: GameState,
        camera: Camera,
        selection: SelectionState,
        localPlayerId: PlayerId
    ) {
        for (unit in state.units.values) {
            val screenPos = camera.worldToScreen(unit.position)
            val radius = camera.zoom * 0.25f

            val color = when {
                unit.ownerId == localPlayerId -> Color(0xFF66BB6A)
                else -> Color(0xFFEF5350)
            }

            scope.drawCircle(color = color, radius = radius, center = Offset(screenPos.x, screenPos.y))

            if (unit.id in selection.selectedUnitIds) {
                scope.drawCircle(
                    color = Color.White,
                    radius = radius + 4f,
                    center = Offset(screenPos.x, screenPos.y),
                    style = Stroke(width = 2f)
                )
            }

            if (unit.currentHealth < unit.stats.maxHealth) {
                drawProgressBar(
                    scope,
                    Vector2(screenPos.x, screenPos.y - radius - 10f),
                    radius * 2.2f,
                    unit.currentHealth.toFloat() / unit.stats.maxHealth,
                    Color(0xFF4CAF50)
                )
            }
        }
    }

    private fun drawProgressBar(scope: DrawScope, screenPos: Vector2, width: Float, progress: Float, color: Color) {
        val height = 5f
        val topLeft = Offset(screenPos.x - width / 2, screenPos.y)
        scope.drawRect(color = Color.Black.copy(alpha = 0.5f), topLeft = topLeft, size = androidx.compose.ui.geometry.Size(width, height))
        scope.drawRect(color = color, topLeft = topLeft, size = androidx.compose.ui.geometry.Size(width * progress.coerceIn(0f, 1f), height))
    }

    private fun drawSelectionRect(scope: DrawScope, selection: SelectionState) {
        val start = selection.dragStart ?: return
        val current = selection.dragCurrent ?: return
        val topLeft = Offset(minOf(start.x, current.x), minOf(start.y, current.y))
        val size = androidx.compose.ui.geometry.Size(
            kotlin.math.abs(current.x - start.x),
            kotlin.math.abs(current.y - start.y)
        )
        scope.drawRect(color = Color.White.copy(alpha = 0.15f), topLeft = topLeft, size = size)
        scope.drawRect(color = Color.White, topLeft = topLeft, size = size, style = Stroke(width = 1.5f))
    }
}