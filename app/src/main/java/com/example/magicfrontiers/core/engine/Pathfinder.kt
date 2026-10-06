package com.example.magicfrontiers.core.engine

import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.Vector2
import java.util.PriorityQueue
import kotlin.math.abs

object Pathfinder {

    private data class Node(val x: Int, val y: Int)

    /**
     * Findet einen Pfad von start zu goal über begehbare Zellen.
     * Gibt eine Liste von Weltkoordinaten zurück (Zellenmittelpunkte), ohne den Startpunkt.
     * Leere Liste = kein Pfad gefunden (z. B. Ziel unerreichbar).
     */
    fun findPath(state: GameState, start: Vector2, goal: Vector2): List<Vector2> {
        val startNode = Node(start.x.toInt(), start.y.toInt())
        val goalNode = Node(goal.x.toInt(), goal.y.toInt())

        val walkable = state.map.associateBy { Node(it.x, it.y) }
        if (walkable[goalNode]?.isWalkable != true) return emptyList()
        if (startNode == goalNode) return emptyList()

        val openSet = PriorityQueue<Pair<Node, Float>>(compareBy { it.second })
        openSet.add(startNode to 0f)

        val cameFrom = HashMap<Node, Node>()
        val gScore = HashMap<Node, Float>().apply { put(startNode, 0f) }
        val visited = HashSet<Node>()

        while (openSet.isNotEmpty()) {
            val (current, _) = openSet.poll()
            if (current in visited) continue
            visited += current

            if (current == goalNode) {
                return reconstructPath(cameFrom, current).map {
                    Vector2(it.x + 0.5f, it.y + 0.5f) // Zellenmittelpunkt
                }
            }

            for (neighbor in neighbors(current)) {
                val cell = walkable[neighbor] ?: continue
                if (!cell.isWalkable) continue

                val isDiagonal = neighbor.x != current.x && neighbor.y != current.y
                val moveCost = if (isDiagonal) 1.4142f else 1f
                val tentativeG = (gScore[current] ?: Float.MAX_VALUE) + moveCost

                if (tentativeG < (gScore[neighbor] ?: Float.MAX_VALUE)) {
                    cameFrom[neighbor] = current
                    gScore[neighbor] = tentativeG
                    val f = tentativeG + heuristic(neighbor, goalNode)
                    openSet.add(neighbor to f)
                }
            }
        }
        return emptyList() // kein Pfad gefunden
    }

    private fun neighbors(node: Node): List<Node> = listOf(
        Node(node.x + 1, node.y), Node(node.x - 1, node.y),
        Node(node.x, node.y + 1), Node(node.x, node.y - 1),
        Node(node.x + 1, node.y + 1), Node(node.x - 1, node.y - 1),
        Node(node.x + 1, node.y - 1), Node(node.x - 1, node.y + 1)
    )

    private fun heuristic(a: Node, b: Node): Float {
        val dx = abs(a.x - b.x)
        val dy = abs(a.y - b.y)
        return (dx + dy) + (1.4142f - 2f) * minOf(dx, dy) // Octile-Distanz, passt zu 8-Richtungs-Bewegung
    }

    private fun reconstructPath(cameFrom: Map<Node, Node>, end: Node): List<Node> {
        val path = mutableListOf(end)
        var current = end
        while (cameFrom.containsKey(current)) {
            current = cameFrom.getValue(current)
            path.add(0, current)
        }
        return path.drop(1) // Startzelle nicht mit einschließen
    }
}