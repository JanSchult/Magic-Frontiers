package com.example.magicfrontiers.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.magicfrontiers.core.engine.catalog.FactionCatalog
import com.example.magicfrontiers.core.model.Faction
import com.example.magicfrontiers.ui.componenten.FactionCard

@Composable
fun FactionSelectScreen(onFactionChosen: (String) -> Unit) {
    var selected by remember { mutableStateOf<Faction?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Fraktion wählen", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        FactionCatalog.all().forEach { faction ->
            FactionCard(
                faction = faction,
                isSelected = faction.id == selected?.id,
                onClick = { selected = faction }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { selected?.let { onFactionChosen(it.id.value) } },
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Spiel starten")
        }
    }
}