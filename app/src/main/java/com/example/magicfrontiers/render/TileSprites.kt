package com.example.magicfrontiers.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.magicfrontiers.core.model.TerrainType
import kotlin.random.Random

object TileSprites {

    fun drawTile(
        scope: DrawScope,
        terrainType: TerrainType,
        topLeft: Offset,
        tileSize: Float,
        dimmed: Boolean,
        cellSeed: Int // für konsistente, aber pro-Zelle leicht variierende Deko
    ) {
        val alpha = if (dimmed) 0.5f else 1f
        when (terrainType) {
            TerrainType.GRASS -> drawGrass(scope, topLeft, tileSize, alpha, cellSeed)
            TerrainType.ROCK -> drawRock(scope, topLeft, tileSize, alpha, cellSeed)
            TerrainType.WATER -> drawWater(scope, topLeft, tileSize, alpha)
        }
    }

    private fun drawGrass(scope: DrawScope, topLeft: Offset, size: Float, alpha: Float, seed: Int) {
        scope.drawRect(Color(0xFF2E4A2E).copy(alpha = alpha), topLeft, Size(size, size))
        // ein paar kleine "Grasbüschel"-Striche, deterministisch pro Zelle
        val rnd = Random(seed)
        repeat(3) {
            val px = topLeft.x + rnd.nextFloat() * size
            val py = topLeft.y + rnd.nextFloat() * size
            scope.drawLine(
                color = Color(0xFF4A6B3A).copy(alpha = alpha),
                start = Offset(px, py),
                end = Offset(px, py - size * 0.12f),
                strokeWidth = size * 0.04f
            )
        }
    }

    private fun drawRock(scope: DrawScope, topLeft: Offset, size: Float, alpha: Float, seed: Int) {
        scope.drawRect(Color(0xFF4A4542).copy(alpha = alpha), topLeft, Size(size, size))
        val rnd = Random(seed)
        // zwei versetzte, dunklere "Rissflächen" als Felsstruktur-Andeutung
        repeat(2) {
            val px = topLeft.x + rnd.nextFloat() * size * 0.6f
            val py = topLeft.y + rnd.nextFloat() * size * 0.6f
            val s = size * (0.25f + rnd.nextFloat() * 0.15f)
            scope.drawRect(
                color = Color(0xFF34302E).copy(alpha = alpha),
                topLeft = Offset(px, py),
                size = Size(s, s)
            )
        }
    }

    private fun drawWater(scope: DrawScope, topLeft: Offset, size: Float, alpha: Float) {
        scope.drawRect(Color(0xFF1B3A4B).copy(alpha = alpha), topLeft, Size(size, size))
        // horizontale Wellenlinie als simple Bewegungs-Andeutung (statisch, kein Animations-Overhead)
        scope.drawLine(
            color = Color(0xFF3A6B82).copy(alpha = alpha),
            start = Offset(topLeft.x, topLeft.y + size * 0.5f),
            end = Offset(topLeft.x + size, topLeft.y + size * 0.5f),
            strokeWidth = size * 0.05f
        )
    }
}