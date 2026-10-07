package com.example.magicfrontiers.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.magicfrontiers.core.model.FactionId

object BuildingSprites {

    fun drawBuilding(
        scope: DrawScope,
        typeId: String,
        center: Offset,
        size: Float,
        factionId: FactionId?,
        alpha: Float = 1f
    ) {
        val palette = SpriteStyle.paletteFor(factionId)
        when (typeId) {
            "barracks" -> drawBarracks(scope, center, size, palette, alpha)
            "lab" -> drawLab(scope, center, size, palette, alpha)
            else -> scope.drawRect(
                color = palette.primary.copy(alpha = alpha),
                topLeft = Offset(center.x - size / 2, center.y - size / 2),
                size = Size(size, size)
            )
        }
    }

    /** Kaserne: breiter Sockel + spitzes Dach, wirkt wehrhaft/militärisch. */
    private fun drawBarracks(scope: DrawScope, center: Offset, size: Float, palette: SpriteStyle.Palette, alpha: Float) {
        val half = size / 2
        // Sockel
        scope.drawRect(
            color = palette.primary.copy(alpha = alpha),
            topLeft = Offset(center.x - half, center.y),
            size = Size(size, half)
        )
        // Dach (Dreieck)
        val roof = Path().apply {
            moveTo(center.x - half * 1.1f, center.y)
            lineTo(center.x, center.y - half)
            lineTo(center.x + half * 1.1f, center.y)
            close()
        }
        scope.drawPath(roof, color = palette.outline.copy(alpha = alpha), style = Fill)
        scope.drawRect(
            color = palette.outline.copy(alpha = alpha),
            topLeft = Offset(center.x - half, center.y),
            size = Size(size, half),
            style = Stroke(width = size * 0.05f)
        )
    }

    /** Labor: Turm mit Kristall-Spitze, wirkt magisch/forschend statt militärisch. */
    private fun drawLab(scope: DrawScope, center: Offset, size: Float, palette: SpriteStyle.Palette, alpha: Float) {
        val half = size / 2
        // Turmschaft
        scope.drawRect(
            color = palette.primary.copy(alpha = alpha),
            topLeft = Offset(center.x - half * 0.5f, center.y - half * 0.3f),
            size = Size(size * 0.5f, size * 0.8f)
        )
        // Kristall-Spitze (Raute)
        val crystal = Path().apply {
            moveTo(center.x, center.y - half * 1.3f)
            lineTo(center.x + half * 0.4f, center.y - half * 0.3f)
            lineTo(center.x, center.y + half * 0.1f)
            lineTo(center.x - half * 0.4f, center.y - half * 0.3f)
            close()
        }
        scope.drawPath(crystal, color = palette.accent.copy(alpha = alpha), style = Fill)
        scope.drawPath(crystal, color = palette.outline.copy(alpha = alpha), style = Stroke(width = size * 0.04f))
    }
}