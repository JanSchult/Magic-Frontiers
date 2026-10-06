package com.example.magicfrontiers.core.model

// core/model/Command.kt
sealed interface Command {
    data class Move(val unitIds: List<UnitId>, val target: Vector2) : Command
    data class Attack(val unitIds: List<UnitId>, val targetUnitId: UnitId) : Command
    data class HoldPosition(val unitIds: List<UnitId>) : Command
    data class ProduceUnit(val buildingId: BuildingId, val unitTypeId: String) : Command
    data class PlaceBuilding(val playerId: PlayerId, val typeId: String, val position: Vector2) : Command
    data class Gather(val unitIds: List<UnitId>, val nodeId: String) : Command
    data class ResearchTech(val buildingId: BuildingId, val techId: String) : Command

}
