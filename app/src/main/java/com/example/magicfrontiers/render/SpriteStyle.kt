package com.example.magicfrontiers.render

import androidx.compose.ui.graphics.Color
import com.example.magicfrontiers.core.model.FactionId

object SpriteStyle {
    data class Palette(val primary: Color, val outline: Color, val accent: Color)

    private val palettes = mapOf(
        "ember_dominion" to Palette(
            primary = Color(0xFFD84315), outline = Color(0xFF3E1A0A), accent = Color(0xFFFFC107)
        ),
        "verdant_concord" to Palette(
            primary = Color(0xFF558B2F), outline = Color(0xFF1B3409), accent = Color(0xFFA5D6A7)
        )
    )

    private val fallback = Palette(Color(0xFF9E9E9E), Color(0xFF424242), Color(0xFFBDBDBD))

    fun paletteFor(factionId: FactionId?): Palette = palettes[factionId?.value] ?: fallback
}