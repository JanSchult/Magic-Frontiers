package com.example.magicfrontiers.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.magicfrontiers.core.model.PlayerId
import com.example.magicfrontiers.render.WorldRenderer
import com.example.magicfrontiers.ui.componenten.HudOverlay
import com.example.magicfrontiers.viewmodel.GameViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameScreen(viewModel: GameViewModel = koinViewModel()) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()

    val buildMode by viewModel.buildMode.collectAsStateWithLifecycle()
    val selectedBuildingId by viewModel.selectedBuildingId.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pauseLoop()
                Lifecycle.Event.ON_RESUME -> viewModel.resumeLoop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        var isDrag = false
                        viewModel.onDragStart(down.position)

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            val dragDistance = (change.position - down.position).getDistance()
                            if (dragDistance > 12f) isDrag = true
                            if (isDrag) viewModel.onDrag(change.position)
                            change.consume()
                        } while (event.changes.any { it.pressed })

                        if (isDrag) {
                            viewModel.onDragEnd()
                        } else {
                            viewModel.onCommandTap(down.position)
                            viewModel.clearDragState()
                        }
                    }
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
            buildMode = buildMode,
            selectedBuildingId = selectedBuildingId,
            commandDispatcher = viewModel.commandDispatcher,
            onEnterBuildMode = { viewModel.enterBuildMode(it) },
            onCancelBuildMode = { viewModel.cancelBuildMode() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}