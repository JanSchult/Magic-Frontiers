package com.example.magicfrontiers.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ControlPoint(
    val id: String,
    val position: Vector2,
    val captureRadius: Float = 3f,
    val controllingPlayerId: PlayerId? = null, // null = neutral/unbesetzt
    val captureProgress: Float = 0f, // 0..1, bezogen auf den jeweils fortschreitenden Spieler
    val progressingPlayerId: PlayerId? = null, // wer aktuell einnimmt (kann von controllingPlayerId abweichen bei Rückeroberung)
    val incomeEnergyPerSecond: Int = 2,
    val incomeMaterialPerSecond: Int = 1
)