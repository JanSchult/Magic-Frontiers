package com.example.magicfrontiers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magicfrontiers.ai.AiController
import com.example.magicfrontiers.core.engine.GameLoop
import com.example.magicfrontiers.core.engine.Simulation
import com.example.magicfrontiers.core.model.BuildingId
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
    // GameViewModel.kt — init anpassen
    private val gameLoop = GameLoop(
        simulation = simulation,
        scope = viewModelScope,
        onTick = { deltaMs -> aiController.update(simulation.state.value, deltaMs) }
    )
    val camera = Camera()
    val gameState: StateFlow<GameState> = simulation.state

    private val _selection = MutableStateFlow(SelectionState())
    val selection: StateFlow<SelectionState> = _selection.asStateFlow()

    private val selectionController = SelectionController(localPlayerId)
    val commandDispatcher = CommandDispatcher(simulation, localPlayerId)
    private val _buildMode = MutableStateFlow<String?>(null) // typeId des zu bauenden Gebäudes, null = aus
    val buildMode: StateFlow<String?> = _buildMode.asStateFlow()

    private val _selectedBuildingId = MutableStateFlow<BuildingId?>(null)
    val selectedBuildingId: StateFlow<BuildingId?> = _selectedBuildingId.asStateFlow()

    // ui/GameViewModel.kt — Ergänzung
    private val aiController = AiController(
        simulation = simulation,
        aiPlayerId = PlayerId("p2"),
        enemyPlayerId = localPlayerId
    )
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
        val buildType = _buildMode.value
        if (buildType != null) {
            val worldPos = camera.screenToWorld(position.x, position.y)
            commandDispatcher.placeBuilding(buildType, worldPos)
            _buildMode.value = null
            return
        }

        val selected = _selection.value.selectedUnitIds
        if (selected.isEmpty()) {
            selectBuildingAt(position) // kein Unit-Befehl aktiv -> evtl. Gebäude anklicken
            return
        }

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
    fun enterBuildMode(typeId: String) {
        _buildMode.value = typeId
        _selection.value = SelectionState() // Einheitenauswahl aufheben, Baumodus hat Vorrang
    }

    fun cancelBuildMode() {
        _buildMode.value = null
    }

    fun selectBuildingAt(position: androidx.compose.ui.geometry.Offset) {
        val worldPos = camera.screenToWorld(position.x, position.y)
        val hit = gameState.value.buildings.values
            .filter { it.ownerId == localPlayerId }
            .minByOrNull { (it.position - worldPos).length() }
            ?.takeIf { (it.position - worldPos).length() < 1.0f }

        _selectedBuildingId.value = hit?.id
        if (hit != null) _selection.value = SelectionState() // Gebäude- und Einheitenauswahl schließen sich aus
    }
    override fun onCleared() {
        gameLoop.stop()
    }
}