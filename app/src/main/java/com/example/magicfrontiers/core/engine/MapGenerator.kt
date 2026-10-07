package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.MapCell
import com.example.magicfrontiers.core.model.TerrainType
import kotlin.random.Random

object MapGenerator {
    fun generateInitialMap(width: Int, height: Int, seed: Long = 42L): List<MapCell> {
        val random = Random(seed)
        val rockClusters = (0 until (width * height / 60)).map {
            Triple(random.nextInt(width), random.nextInt(height), random.nextInt(2, 4))
        }
        val waterCenterX = width / 2 + random.nextInt(-3, 3)
        val waterCenterY = height / 2 + random.nextInt(-3, 3)

        return (0 until width).flatMap { x ->
            (0 until height).map { y ->
                val terrain = when {
                    isNearWaterCenter(x, y, waterCenterX, waterCenterY) -> TerrainType.WATER
                    rockClusters.any { (cx, cy, r) -> distance(x, y, cx, cy) <= r } -> TerrainType.ROCK
                    else -> TerrainType.GRASS
                }
                MapCell(x = x, y = y, terrainType = terrain)
            }
        }
    }

    private fun isNearWaterCenter(x: Int, y: Int, cx: Int, cy: Int): Boolean =
        distance(x, y, cx, cy) <= 2.5

    private fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Double =
        kotlin.math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble())
}
