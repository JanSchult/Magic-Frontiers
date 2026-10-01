package com.example.magicfrontiers.input

import com.example.magicfrontiers.core.model.UnitId

data class SelectionState(
    val selectedUnitIds: Set<UnitId> = emptySet(),
    val dragStart: androidx.compose.ui.geometry.Offset? = null,
    val dragCurrent: androidx.compose.ui.geometry.Offset? = null
) {
    val isDragging: Boolean get() = dragStart != null
}