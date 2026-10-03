package com.example.magicfrontiers.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magicfrontiers.ai.AiController
import com.example.magicfrontiers.core.engine.GameLoop
import com.example.magicfrontiers.core.engine.InitialGameStateFactory
import com.example.magicfrontiers.core.engine.SaveStatus
import com.example.magicfrontiers.core.engine.Simulation
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.input.CommandDispatcher
import com.example.magicfrontiers.input.SelectionController
import com.example.magicfrontiers.input.SelectionState
import com.example.magicfrontiers.persistence.SaveGameRepository
import com.example.magicfrontiers.render.Camera
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class GameViewModel(
    initialState: GameState,
    private val saveGameRepository: SaveGameRepository,
    private val localPlayerId: PlayerId = PlayerId("p1")
) : ViewModel() {

    private val simulation = Simulation(InitialGameStateFactory.create())
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
    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    private val _lastPointerScreenPos = MutableStateFlow<Offset?>(null)


    private val aiController = AiController(
        simulation = simulation,
        aiPlayerId = PlayerId("p2"),
        enemyPlayerId = localPlayerId
    )
    init {
        viewModelScope.launch {
            while (isActive) {
                delay(60_000L.milliseconds)
                saveGame("autosave")
            }
        }    }

    fun onDragStart(position: Offset) {
        _selection.value = _selection.value.copy(dragStart = position, dragCurrent = position)
    }
    fun clearDragState() {
        _selection.value = _selection.value.copy(dragStart = null, dragCurrent = null)
    }
    fun onPointerMove(position: Offset) {
        if (_buildMode.value != null) _lastPointerScreenPos.value = position
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
    fun saveGame(slot: String = "autosave") {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            val result = saveGameRepository.save(gameState.value, slot)
            _saveStatus.value = if (result.isSuccess) SaveStatus.Saved else SaveStatus.Error
        }
    }
    suspend fun loadGame(slot: String = "autosave"): GameState? =
        saveGameRepository.load(slot).getOrNull()

    override fun onCleared() {
        gameLoop.stop()
    }
    fun pauseLoop() = gameLoop.stop()
    fun resumeLoop() = gameLoop.start()
}