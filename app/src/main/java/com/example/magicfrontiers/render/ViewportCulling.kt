package com.example.magicfrontiers.render

import androidx.compose.ui.geometry.Size


data class WorldBounds(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int) {
     fun contains(x: Int, y: Int): Boolean = x in minX..maxX && y in minY..maxY
}

object ViewportCulling {
    /** Berechnet den sichtbaren Weltbereich plus kleinem Rand (Objekte an Kanten nicht abrupt abschneiden). */
    fun visibleWorldBounds(camera: Camera, canvasSize: Size, mapWidth: Int, mapHeight: Int, margin: Int = 1): WorldBounds {
        val topLeft = camera.screenToWorld(0f, 0f)
        val bottomRight = camera.screenToWorld(canvasSize.width, canvasSize.height)
        return WorldBounds(
            minX = (topLeft.x.toInt() - margin).coerceAtLeast(0),
            maxX = (bottomRight.x.toInt() + margin).coerceAtMost(mapWidth),
            minY = (topLeft.y.toInt() - margin).coerceAtLeast(0),
            maxY = (bottomRight.y.toInt() + margin).coerceAtMost(mapHeight)
        )
    }
}