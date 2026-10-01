package com.example.magicfrontiers.render

import com.example.magicfrontiers.core.model.Vector2

class Camera(
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var zoom: Float = 40f // Pixel pro Welteinheit
) {
    fun worldToScreen(world: Vector2): Vector2 =
        Vector2(world.x * zoom + offsetX, world.y * zoom + offsetY)

    fun screenToWorld(screenX: Float, screenY: Float): Vector2 =
        Vector2((screenX - offsetX) / zoom, (screenY - offsetY) / zoom)

    fun pan(dx: Float, dy: Float) {
        offsetX += dx
        offsetY += dy
    }

    fun zoomBy(factor: Float, focusScreenX: Float, focusScreenY: Float) {
        val worldFocus = screenToWorld(focusScreenX, focusScreenY)
        zoom = (zoom * factor).coerceIn(15f, 100f)
        val newScreenFocus = worldToScreen(worldFocus)
        offsetX += focusScreenX - newScreenFocus.x
        offsetY += focusScreenY - newScreenFocus.y
    }
}