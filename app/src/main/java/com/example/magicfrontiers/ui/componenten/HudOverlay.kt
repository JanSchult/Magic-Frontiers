package com.example.magicfrontiers.ui.componenten

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.magicfrontiers.core.model.BuildingId
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.input.CommandDispatcher
import com.example.magicfrontiers.input.SelectionState

@Composable
fun HudOverlay(
    gameState: GameState,
    selection: SelectionState,
    buildMode: String?,
    selectedBuildingId: BuildingId?,
    commandDispatcher: CommandDispatcher,
    onEnterBuildMode: (String) -> Unit,
    onCancelBuildMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ResourceBar(gameState = gameState, localPlayerId = PlayerId("p1"))
        Spacer(Modifier.height(8.dp))

        when {
            buildMode != null -> BuildModeBar(buildMode, onCancelBuildMode)
            selectedBuildingId != null -> {
                val building = gameState.buildings[selectedBuildingId]
                if (building != null) BuildingPanel(building, commandDispatcher)
            }
            selection.selectedUnitIds.isNotEmpty() -> SelectionPanel(gameState, selection, commandDispatcher)
            else -> BuildMenuBar(onEnterBuildMode)
        }
    }
}