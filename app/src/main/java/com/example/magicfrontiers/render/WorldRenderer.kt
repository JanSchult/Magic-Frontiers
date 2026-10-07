package com.example.magicfrontiers.render

import android.graphics.Color.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.magicfrontiers.core.model.CellKey
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
        localPlayerId: PlayerId,
        buildModeTypeId: String? = null,
        dragPreviewScreenPos: Offset? = null // letzte bekannte Touch-Position
    ) {
        val visibleCells = state.visibilityByPlayer[localPlayerId] ?: emptySet()
        val canvasSize = scope.size
        drawMapGrid(scope, state, camera, visibleCells,canvasSize)
        drawResourceNodes(scope, state, camera, canvasSize)
        drawBuildings(scope, state, camera, localPlayerId, visibleCells,canvasSize)
        drawUnits(scope, state, camera, selection, localPlayerId, visibleCells,canvasSize)
        drawControlPoints(scope, state, camera, localPlayerId, canvasSize)
        if (selection.isDragging) drawSelectionRect(scope, selection)
        if (buildModeTypeId != null && dragPreviewScreenPos != null) {
            drawBuildPreview(scope, camera.zoom, dragPreviewScreenPos)
        }
    }

    private fun drawMapGrid(scope: DrawScope, state: GameState, camera: Camera, visibleCells: Set<CellKey>,canvasSize: Size) {
        val topLeftWorld = camera.screenToWorld(0f, 0f)
        val bottomRightWorld = camera.screenToWorld(canvasSize.width, canvasSize.height)
        val minX = topLeftWorld.x.toInt().coerceAtLeast(0)
        val maxX = bottomRightWorld.x.toInt().coerceAtMost(state.mapWidth)
        val minY = topLeftWorld.y.toInt().coerceAtLeast(0)
        val maxY = bottomRightWorld.y.toInt().coerceAtMost(state.mapHeight)
        val bounds = ViewportCulling.visibleWorldBounds(camera, canvasSize, state.mapWidth, state.mapHeight)

        for (cell in state.map) {
            if (cell.x !in bounds.minX..bounds.maxX || cell.y !in bounds.minY..bounds.maxY) continue
            if (cell.x !in minX..maxX || cell.y !in minY..maxY) continue
            if (!cell.isExplored) continue
            val isVisible = CellKey(cell.x, cell.y) in visibleCells
            val screenPos = camera.worldToScreen(Vector2(cell.x.toFloat(), cell.y.toFloat()))

            TileSprites.drawTile(
                scope = scope,
                terrainType = cell.terrainType,
                topLeft = Offset(screenPos.x, screenPos.y),
                tileSize = camera.zoom,
                dimmed = !isVisible,
                cellSeed = cell.x * 10000 + cell.y // deterministisch, kollisionsfrei für Kartengrößen < 10000
            )
        }
    }

    private fun drawResourceNodes(scope: DrawScope, state: GameState, camera: Camera, canvasSize: Size) {

        val bounds = ViewportCulling.visibleWorldBounds(camera, canvasSize, state.mapWidth, state.mapHeight)

        for (node in state.resourceNodes.values) {
            val nx = node.position.x.toInt()
            val ny = node.position.y.toInt()
            if (nx !in bounds.minX..bounds.maxX || ny !in bounds.minY..bounds.maxY) continue

            val screenPos = camera.worldToScreen(node.position)
            val color = when (node.type) {
                ResourceType.ENERGY -> Color(0xFF4FC3F7)
                ResourceType.MATERIAL -> Color(0xFFA1887F)
                ResourceType.RARE -> Color(0xFFCE93D8)
            }
            scope.drawCircle(color = color, radius = camera.zoom * 0.35f, center = Offset(screenPos.x, screenPos.y))
        }
    }

    private fun drawBuildings(
        scope: DrawScope,
        state: GameState,
        camera: Camera,
        localPlayerId: PlayerId,
        visibleCells: Set<CellKey>,
        canvasSize: Size
    ) {
        val bounds = ViewportCulling.visibleWorldBounds(camera, canvasSize, state.mapWidth, state.mapHeight)

        for (building in state.buildings.values) {
            val bx = building.position.x.toInt()
            val by = building.position.y.toInt()
            if (bx !in bounds.minX..bounds.maxX || by !in bounds.minY..bounds.maxY) continue

            val cellKey = CellKey(building.position.x.toInt(), building.position.y.toInt())

            // Gebaeude ist sichtbar, wenn es dem eigenen Spieler gehoert oder auf einem sichtbaren Feld steht
            val isVisibleToPlayer = building.ownerId == localPlayerId || cellKey in visibleCells
            if (!isVisibleToPlayer) continue

            val screenPos = camera.worldToScreen(building.position)
            val size = camera.zoom * 1.2f
            val factionId = state.factions[building.ownerId]
            val baseColor = if (building.isConstructed) Color(0xFF616161) else Color(0xFF616161).copy(alpha = 0.5f)
            val alpha = if (building.isConstructed) 1f else 0.5f

            BuildingSprites.drawBuilding(scope, building.typeId, Offset(screenPos.x, screenPos.y), size, factionId, alpha)

            scope.drawRect(
                color = baseColor,
                topLeft = Offset(screenPos.x - size / 2, screenPos.y - size / 2),
                size = androidx.compose.ui.geometry.Size(size, size)
            )

            // Baufortschritt-Balken
            if (!building.isConstructed) {
                drawProgressBar(scope, screenPos, size, building.constructionProgress, Color(0xFFFFC107))
            }
            // Health-Bar nur wenn beschaedigt
            else if (building.currentHealth < building.maxHealth) {
                drawProgressBar(
                    scope,
                    screenPos,
                    size,
                    building.currentHealth.toFloat() / building.maxHealth,
                    Color(0xFF4CAF50)
                )
            }
        }
    }

    private fun drawUnits(
        scope: DrawScope,
        state: GameState,
        camera: Camera,
        selection: SelectionState,
        localPlayerId: PlayerId,
        visibleCells: Set<CellKey>,
        canvasSize: Size
    ) {
        val bounds = ViewportCulling.visibleWorldBounds(camera, canvasSize, state.mapWidth, state.mapHeight)

        for (unit in state.units.values) {
            val ux = unit.position.x.toInt()
            val uy = unit.position.y.toInt()

            // 2. Performance-Check: Einheiten außerhalb des Bildschirms überspringen
            if (ux !in bounds.minX..bounds.maxX || uy !in bounds.minY..bounds.maxY) continue

            // 3. Sichtbarkeits-Check (Fog of War)
            val cellKey = CellKey(ux, uy)
            val isVisibleToPlayer = unit.ownerId == localPlayerId || cellKey in visibleCells
            if (!isVisibleToPlayer) continue

            // 4. Rendering-Berechnungen
            val screenPos = camera.worldToScreen(unit.position)
            val radius = camera.zoom * 0.25f
            val factionId = state.factions[unit.ownerId]

            // Sprite zeichnen
            UnitSprites.drawUnit(scope, unit.typeId, Offset(screenPos.x, screenPos.y), radius, factionId)

            // Team-Farbe bestimmen und Basis-Kreis zeichnen
            val color = when {
                unit.ownerId == localPlayerId -> Color(0xFF66BB6A)
                else -> Color(0xFFEF5350)
            }

            scope.drawCircle(
                color = color,
                radius = radius,
                center = Offset(screenPos.x, screenPos.y)
            )

            // Selektionsring (falls ausgewählt)
            if (unit.id in selection.selectedUnitIds) {
                scope.drawCircle(
                    color = Color.White,
                    radius = radius + 4f,
                    center = Offset(screenPos.x, screenPos.y),
                    style = Stroke(width = 2f)
                )
            }

            // Lebensbalken (falls beschädigt)
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

    private fun drawBuildPreview(scope: DrawScope, zoom: Float, screenPos: Offset) {
        val size = zoom * 1.2f
        scope.drawRect(
            color = Color(0xFFFFC107).copy(alpha = 0.4f),
            topLeft = Offset(screenPos.x - size / 2, screenPos.y - size / 2),
            size = androidx.compose.ui.geometry.Size(size, size)
        )
    }
    private fun drawControlPoints(
        scope: DrawScope,
        state: GameState,
        camera: Camera,
        localPlayerId: PlayerId,
        canvasSize: androidx.compose.ui.geometry.Size
    ) {
        val bounds = ViewportCulling.visibleWorldBounds(camera, canvasSize, state.mapWidth, state.mapHeight)

        for (point in state.controlPoints.values) {
            val px = point.position.x.toInt()
            val py = point.position.y.toInt()
            if (px !in bounds.minX..bounds.maxX || py !in bounds.minY..bounds.maxY) continue

            val screenPos = camera.worldToScreen(point.position)
            val radiusPx = point.captureRadius * camera.zoom

            val ringColor = when (point.controllingPlayerId) {
                localPlayerId -> Color(0xFF66BB6A)
                null -> Color(0xFF9E9E9E)
                else -> Color(0xFFEF5350)
            }

            scope.drawCircle(
                color = ringColor.copy(alpha = 0.25f),
                radius = radiusPx,
                center = Offset(screenPos.x, screenPos.y)
            )
            scope.drawCircle(
                color = ringColor,
                radius = radiusPx,
                center = Offset(screenPos.x, screenPos.y),
                style = Stroke(width = 3f)
            )

            // Flaggen-Symbol in der Mitte
            drawFlagIcon(scope, Offset(screenPos.x, screenPos.y), camera.zoom * 0.4f, ringColor)

            if (point.progressingPlayerId != null) {
                val progressColor = if (point.progressingPlayerId == localPlayerId) Color(0xFF66BB6A) else Color(0xFFEF5350)
                drawProgressBar(
                    scope,
                    Vector2(screenPos.x, screenPos.y - radiusPx - 14f),
                    radiusPx * 0.8f,
                    point.captureProgress,
                    progressColor
                )
            }
        }
    }

    private fun drawFlagIcon(scope: DrawScope, center: Offset, size: Float, color: Color) {
        scope.drawLine(
            color = color,
            start = Offset(center.x, center.y - size),
            end = Offset(center.x, center.y + size * 0.3f),
            strokeWidth = size * 0.12f
        )
        val flagPath = Path().apply {
            moveTo(center.x, center.y - size)
            lineTo(center.x + size * 0.7f, center.y - size * 0.7f)
            lineTo(center.x, center.y - size * 0.4f)
            close()
        }
        scope.drawPath(flagPath, color = color, style = Fill)
    }
}