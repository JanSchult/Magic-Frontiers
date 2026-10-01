package com.example.magicfrontiers.core.model

// core/model/Vector2.kt
data class Vector2(val x: Float, val y: Float) {
    operator fun minus(o: Vector2) = Vector2(x - o.x, y - o.y)
    operator fun plus(o: Vector2) = Vector2(x + o.x, y + o.y)
    fun length(): Float = kotlin.math.sqrt(x * x + y * y)
}
