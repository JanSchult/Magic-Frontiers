package com.example.magicfrontiers.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UnitStats(
    val maxHealth: Int,
    val damage: Int,
    val attackRange: Float,
    val attackCooldownMs: Long,
    val moveSpeed: Float,
    val visionRange: Float,
    val armor: Int = 0,
    val canGather: Boolean = false,
    val gatherCapacity: Int = 0,
    val gatherRatePerSecond: Int = 0
)
