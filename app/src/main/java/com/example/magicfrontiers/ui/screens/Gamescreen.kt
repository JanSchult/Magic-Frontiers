package com.example.magicfrontiers.ui.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.render.WorldRenderer
import com.example.magicfrontiers.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel = koinViewModel()) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { viewModel.onDragStart(it) },
                        onDrag = { change, _ -> viewModel.onDrag(change.position) },
                        onDragEnd = { viewModel.onDragEnd() }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { viewModel.onCommandTap(it) }
                    )
                }
        ) {
            WorldRenderer.draw(
                scope = this as DrawScope,
                state = gameState,
                camera = viewModel.camera,
                selection = selection,
                localPlayerId = PlayerId("p1")
            )
        }

        HudOverlay(
            gameState = gameState,
            selection = selection,
            commandDispatcher = viewModel.commandDispatcher,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}