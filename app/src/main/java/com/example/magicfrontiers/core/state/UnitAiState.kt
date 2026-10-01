package com.example.magicfrontiers.core.state

sealed interface UnitAiState {
    data object Idle : UnitAiState
    data object Moving : UnitAiState
    data object Attacking : UnitAiState
    data object HoldingPosition : UnitAiState
    data object MovingToGather : UnitAiState
    data object Gathering : UnitAiState
    data object ReturningResource : UnitAiState
}