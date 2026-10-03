package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.MapCell

object MapGenerator {
    fun generateInitialMap(width: Int, height: Int): List<MapCell> =
        (0 until width).flatMap { x ->
            (0 until height).map { y ->
                MapCell(x = x, y = y, isWalkable = true, isExplored = false)
            }
        }
}