package com.example.magicfrontiers.persistence

import com.example.magicfrontiers.core.model.GameState
import kotlinx.serialization.Serializable

// persistence/SaveFile.kt
@Serializable
data class SaveFile(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val state: GameState
) {
    companion object { const val CURRENT_SCHEMA_VERSION = 1 }
}