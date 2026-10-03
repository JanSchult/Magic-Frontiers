package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.GameUnit
import com.example.magicfrontiers.core.model.Vector2
import kotlin.math.floor

class SpatialGrid(private val cellSize: Float = 2f) {

    private val buckets = HashMap<Long, MutableList<GameUnit>>()

    private fun key(x: Int, y: Int): Long = (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)

    private fun cellOf(pos: Vector2): Pair<Int, Int> =
        floor(pos.x / cellSize).toInt() to floor(pos.y / cellSize).toInt()

    fun build(units: Collection<GameUnit>) {
        buckets.clear()
        for (unit in units) {
            val (cx, cy) = cellOf(unit.position)
            buckets.getOrPut(key(cx, cy)) { mutableListOf() }.add(unit)
        }
    }

    /** Liefert alle Einheiten aus der Zelle von [position] und den 8 Nachbarzellen. */
    fun nearby(position: Vector2): List<GameUnit> {
        val (cx, cy) = cellOf(position)
        val result = mutableListOf<GameUnit>()
        for (dx in -1..1) for (dy in -1..1) {
            buckets[key(cx + dx, cy + dy)]?.let { result.addAll(it) }
        }
        return result
    }
}