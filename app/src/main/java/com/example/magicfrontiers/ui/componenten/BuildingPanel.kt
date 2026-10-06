package com.example.magicfrontiers.ui.componenten

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.magicfrontiers.core.engine.catalog.BuildingCatalog
import com.example.magicfrontiers.core.engine.catalog.TechCatalog
import com.example.magicfrontiers.core.engine.catalog.UnitCatalog
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.input.CommandDispatcher

@Composable
 fun BuildingPanel(building: Building,gameState: GameState, commandDispatcher: CommandDispatcher) {
    val blueprint = BuildingCatalog.getOrNull(building.typeId) ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        Text(blueprint.displayName, color = Color.White)

        if (!building.isConstructed) {
            Text("Im Bau: ${(building.constructionProgress * 100).toInt()}%", color = Color.White)
            return@Column
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            blueprint.producesUnitTypeIds.forEach { unitTypeId ->
                val unitBlueprint = UnitCatalog.getOrNull(unitTypeId)
                Button(onClick = { commandDispatcher.produceUnit(building.id, unitTypeId) }) {
                    Text(unitBlueprint?.displayName ?: unitTypeId)
                }
            }
        }

        if (building.productionQueue.isNotEmpty()) {
            val order = building.productionQueue.first()
            Text(
                "Produziert: ${order.unitTypeId} (${(order.progress * 100).toInt()}%)",
                color = Color.White
            )
        }
        if (building.typeId == "lab") {
            val researched = gameState.researchedTechs[building.ownerId] ?: emptySet()
            Text("Forschung", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TechCatalog.all()
                    .filter { it.id !in researched && it.requiresTechIds.all { req -> req in researched } }
                    .forEach { tech ->
                        Button(onClick = { commandDispatcher.researchTech(building.id, tech.id) }) {
                            Text("${tech.displayName} (${tech.costEnergy}⚡ ${tech.costMaterial}🪨)")
                        }
                    }
            }
            if (building.researchQueue.isNotEmpty()) {
                val order = building.researchQueue.first()
                Text(
                    "Erforscht: ${order.techId} (${(order.progress * 100).toInt()}%)",
                    color = Color.White
                )
            }
        }
    }
}