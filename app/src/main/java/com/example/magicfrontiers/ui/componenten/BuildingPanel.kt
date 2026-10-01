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
import com.example.magicfrontiers.core.engine.BuildingCatalog
import com.example.magicfrontiers.core.engine.UnitCatalog
import com.example.magicfrontiers.core.model.Building
import com.example.magicfrontiers.input.CommandDispatcher

@Composable
 fun BuildingPanel(building: Building, commandDispatcher: CommandDispatcher) {
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
            Text("Produziert: ${order.unitTypeId} (${(order.progress * 100).toInt()}%)", color = Color.White)
        }
    }
}