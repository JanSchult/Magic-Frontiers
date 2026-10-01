package com.example.magicfrontiers.ui.componenten

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.magicfrontiers.core.model.GameState
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.core.model.ResourceType

@Composable
 fun ResourceBar(gameState: GameState, localPlayerId: PlayerId) {
    val resources = gameState.playerResources[localPlayerId] ?: emptyMap()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚡ ${resources[ResourceType.ENERGY] ?: 0}", color = Color.White)
        Text("🪨 ${resources[ResourceType.MATERIAL] ?: 0}", color = Color.White)
        Text("💎 ${resources[ResourceType.RARE] ?: 0}", color = Color.White)
    }
}