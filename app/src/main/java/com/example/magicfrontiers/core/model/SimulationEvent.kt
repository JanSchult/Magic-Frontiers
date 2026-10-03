package com.example.magicfrontiers.core.model

// core/model/SimulationEvent.kt
sealed interface SimulationEvent {
    data class CommandRejected(val reason: String) : SimulationEvent
    data class UnitProduced(val unitId: UnitId, val ownerId: PlayerId) : SimulationEvent
    data class BuildingDestroyed(val buildingId: BuildingId) : SimulationEvent
}