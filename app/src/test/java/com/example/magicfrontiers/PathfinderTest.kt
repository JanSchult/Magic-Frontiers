package com.example.magicfrontiers

import com.example.magicfrontiers.core.engine.Pathfinder
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.MapCell
import com.example.magicfrontiers.core.model.TerrainType
import com.example.magicfrontiers.core.model.Vector2
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class PathfinderTest {

    private fun openMap(width: Int, height: Int) =
        (0 until width).flatMap { x -> (0 until height).map { y ->
            MapCell(
                x,
                y,
                terrainType = TerrainType.GRASS
            )
        } }

    @Test
    fun `finds direct path on open map`() {
        val map = openMap(10, 10)
        val state =
            GameState(0, map, 10, 10, emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap())

        val path = Pathfinder.findPath(state, Vector2(0.5f, 0.5f), Vector2(5.5f, 0.5f))

        assertTrue(path.isNotEmpty())
        assertEquals(5.5f, path.last().x, 0.01f)
    }

    @Test
    fun `routes around obstacle`() {
        val map = openMap(10, 10).map { cell ->
            if (cell.x == 5 && cell.y in 0..5) cell.copy() else cell
        }
        val state = GameState(0, map, 10, 10, emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap())

        val path = Pathfinder.findPath(state, Vector2(0.5f, 2.5f), Vector2(9.5f, 2.5f))

        assertTrue(path.isNotEmpty())
        // Pfad muss die blockierte Spalte x=5 im Bereich y 0..5 meiden
        assertTrue(path.none { it.x.toInt() == 5 && it.y.toInt() in 0..5 })
    }

    @Test
    fun `returns empty list when goal unreachable`() {
        val map = openMap(5, 5).map { cell -> cell.copy() } // ganze Spalte x=4 blockiert
        val state = GameState(0, map, 5, 5, emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap())

        val path = Pathfinder.findPath(state, Vector2(0.5f, 0.5f), Vector2(4.5f, 0.5f))

        assertTrue(path.isEmpty())
    }
}