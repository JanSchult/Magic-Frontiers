package com.example.magicfrontiers.input

import com.example.magicfrontiers.core.engine.Simulation
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.Command
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.UnitId
import com.example.magicfrontiers.core.model.Vector2

class CommandDispatcher(
    private val simulation: Simulation,
    private val localPlayerId: PlayerId
) {
    fun moveSelected(selectedUnitIds: Set<UnitId>, target: Vector2) {
        if (selectedUnitIds.isEmpty()) return
        simulation.submitCommand(Command.Move(selectedUnitIds.toList(), target))
    }

    fun attackTarget(selectedUnitIds: Set<UnitId>, targetUnitId: UnitId) {
        if (selectedUnitIds.isEmpty()) return
        simulation.submitCommand(Command.Attack(selectedUnitIds.toList(), targetUnitId))
    }

    fun holdPosition(selectedUnitIds: Set<UnitId>) {
        if (selectedUnitIds.isEmpty()) return
        simulation.submitCommand(Command.HoldPosition(selectedUnitIds.toList()))
    }

    fun produceUnit(buildingId: BuildingId, unitTypeId: String) {
        simulation.submitCommand(Command.ProduceUnit(buildingId, unitTypeId))
    }

    fun placeBuilding(typeId: String, position: Vector2) {
        simulation.submitCommand(Command.PlaceBuilding(localPlayerId, typeId, position))
    }
    fun researchTech(buildingId: BuildingId, techId: String) {
        simulation.submitCommand(Command.ResearchTech(buildingId, techId))
    }
}