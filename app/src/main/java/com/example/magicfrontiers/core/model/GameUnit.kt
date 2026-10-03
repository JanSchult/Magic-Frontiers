package com.example.magicfrontiers.core.model

import com.example.magicfrontiers.core.state.UnitAiState
import kotlinx.serialization.Serializable

@Serializable

// core/model/Unit.kt — Name "Unit" kollidiert mit kotlin.Unit, daher GameUnit
data class GameUnit(
    val id: UnitId,
    val typeId: String,
    val ownerId: PlayerId,
    val role: UnitRole,
    val position: Vector2,
    val currentHealth: Int,
    val stats: UnitStats,
    val state: UnitAiState = UnitAiState.Idle,
    val targetUnitId: UnitId? = null,
    val moveTarget: Vector2? = null,
    val pathWaypoints: List<Vector2> = emptyList(),
    val attackCooldownRemainingMs: Long = 0L,
    val gatherTargetNodeId: String? = null,
    val carriedResourceType: ResourceType? = null,
    val carriedResourceAmount: Int = 0
)