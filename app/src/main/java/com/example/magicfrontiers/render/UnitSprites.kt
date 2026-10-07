package com.example.magicfrontiers.render

import android.system.Os.close
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.magicfrontiers.core.model.FactionId
import androidx.compose.ui.graphics.Path

object UnitSprites {

    fun drawUnit(
        scope: DrawScope,
        typeId: String,
        center: Offset,
        radius: Float,
        factionId: FactionId?
    ) {
        val palette = SpriteStyle.paletteFor(factionId)
        when (typeId) {
            "scout" -> drawScout(scope, center, radius, palette)
            "archer" -> drawArcher(scope, center, radius, palette)
            else -> scope.drawCircle(color = palette.primary, radius = radius, center = center) // Fallback
        }
    }

    /** Scout: kompakter Keil, deutet schnelle Nahkampf-Bewegung an. */
    private fun drawScout(scope: DrawScope, center: Offset, radius: Float, palette: SpriteStyle.Palette) {
        val path = Path().apply {
            moveTo(center.x, center.y - radius)
            lineTo(center.x + radius * 0.85f, center.y + radius * 0.7f)
            lineTo(center.x, center.y + radius * 0.3f)
            lineTo(center.x - radius * 0.85f, center.y + radius * 0.7f)
            close()
        }
        scope.drawPath(path, color = palette.primary, style = Fill)
        scope.drawPath(path, color = palette.outline, style = Stroke(width = radius * 0.12f))
    }

    /** Archer: Raute mit kleinem "Bogen"-Strich, deutet Fernkampf an. */
    private fun drawArcher(scope: DrawScope, center: Offset, radius: Float, palette: SpriteStyle.Palette) {
        val path = Path().apply {
            moveTo(center.x, center.y - radius)
            lineTo(center.x + radius * 0.7f, center.y)
            lineTo(center.x, center.y + radius)
            lineTo(center.x - radius * 0.7f, center.y)
            close()
        }
        scope.drawPath(path, color = palette.primary, style = Fill)
        scope.drawPath(path, color = palette.outline, style = Stroke(width = radius * 0.1f))

        // kleiner Akzent-Punkt oben = "Pfeilspitze"-Andeutung
        scope.drawCircle(color = palette.accent, radius = radius * 0.15f, center = Offset(center.x, center.y - radius * 0.75f))
    }
}