package com.example.magicfrontiers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magicfrontiers.core.engine.GameLoop
import com.example.magicfrontiers.core.engine.Simulation
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.input.CommandDispatcher
import com.example.magicfrontiers.input.SelectionController
import com.example.magicfrontiers.input.SelectionState
import com.example.magicfrontiers.render.Camera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    initialState: GameState,
    private val localPlayerId: PlayerId = PlayerId("p1")
) : ViewModel() {

    private val simulation = Simulation(initialState)
    private val gameLoop = GameLoop(simulation, viewModelScope)

    val camera = Camera()
    val gameState: StateFlow<GameState> = simulation.state

    private val _selection = MutableStateFlow(SelectionState())
    val selection: StateFlow<SelectionState> = _selection.asStateFlow()

    private val selectionController = SelectionController(localPlayerId)
    val commandDispatcher = CommandDispatcher(simulation, localPlayerId)

    init {
        gameLoop.start()
    }

    fun onDragStart(position: androidx.compose.ui.geometry.Offset) {
        _selection.value = _selection.value.copy(dragStart = position, dragCurrent = position)
    }

    fun onDrag(position: androidx.compose.ui.geometry.Offset) {
        _selection.value = _selection.value.copy(dragCurrent = position)
    }

    fun onDragEnd() {
        val current = _selection.value
        val start = current.dragStart ?: return
        val end = current.dragCurrent ?: return

        val dragDistance = kotlin.math.hypot(end.x - start.x, end.y - start.y)
        val newSelection = if (dragDistance < 12f) {
            // Zu kleine Bewegung -> als Tap behandeln
            val tapped = selectionController.selectUnitAtTap(gameState.value, camera, end)
            tapped?.let { setOf(it) } ?: emptySet()
        } else {
            selectionController.selectUnitsInDragRect(gameState.value, camera, start, end)
        }

        _selection.value = SelectionState(selectedUnitIds = newSelection)
    }

    /** Tap auf Karte während Einheiten ausgewählt sind -> Move oder Attack, je nach Ziel. */
    fun onCommandTap(position: androidx.compose.ui.geometry.Offset) {
        val selected = _selection.value.selectedUnitIds
        if (selected.isEmpty()) return

        val targetUnit = selectionController.findAnyUnitAtTap(gameState.value, camera, position)
        if (targetUnit != null) {
            val isEnemy = gameState.value.units[targetUnit]?.ownerId != localPlayerId
            if (isEnemy) {
                commandDispatcher.attackTarget(selected, targetUnit)
                return
            }
        }

        val worldPos = camera.screenToWorld(position.x, position.y)
        commandDispatcher.moveSelected(selected, worldPos)
    }

    override fun onCleared() {
        gameLoop.stop()
    }
}