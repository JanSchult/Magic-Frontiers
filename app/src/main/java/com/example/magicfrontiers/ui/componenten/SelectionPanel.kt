package com.example.magicfrontiers.ui.componenten

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.input.CommandDispatcher
import com.example.magicfrontiers.input.SelectionState

@Composable
 fun SelectionPanel(
    gameState: GameState,
    selection: SelectionState,
    commandDispatcher: CommandDispatcher
) {
    if (selection.selectedUnitIds.isEmpty()) return

    val selectedUnits = selection.selectedUnitIds.mapNotNull { gameState.units[it] }
    val selectedBuilding = selection.selectedUnitIds.firstOrNull()
        ?.let { null } // Platzhalter: Gebäude-Auswahl kommt mit Bau-UI, hier nur Einheiten-Fokus

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("${selectedUnits.size} Einheit(en) ausgewählt", color = Color.White, modifier = Modifier.weight(1f))

        Button(onClick = { commandDispatcher.holdPosition(selection.selectedUnitIds) }) {
            Text("Halten")
        }
    }
}