package com.example.magicfrontiers.ui.componenten

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.magicfrontiers.core.model.ResourceType
import java.nio.file.Files.size

@Composable
fun ResourceIcon(type: ResourceType, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.85f

        when (type) {
            ResourceType.ENERGY -> drawEnergyIcon(center, radius)
            ResourceType.MATERIAL -> drawMaterialIcon(center, radius)
            ResourceType.RARE -> drawRareIcon(center, radius)
        }
    }
}
/** Energie: Blitz-Silhouette. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEnergyIcon(center: Offset, radius: Float) {
    val color = Color(0xFF4FC3F7)
    val path = Path().apply {
        moveTo(center.x + radius * 0.15f, center.y - radius)
        lineTo(center.x - radius * 0.5f, center.y + radius * 0.15f)
        lineTo(center.x - radius * 0.05f, center.y + radius * 0.15f)
        lineTo(center.x - radius * 0.15f, center.y + radius)
        lineTo(center.x + radius * 0.5f, center.y - radius * 0.15f)
        lineTo(center.x + radius * 0.05f, center.y - radius * 0.15f)
        close()
    }
    drawPath(path, color = color, style = Fill)
}

/** Material: sechseckiger Stein. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMaterialIcon(center: Offset, radius: Float) {
    val color = Color(0xFFA1887F)
    val outline = Color(0xFF5D4037)
    val path = Path().apply {
        for (i in 0..5) {
            val angle = Math.toRadians((60 * i - 90).toDouble())
            val x = center.x + radius * kotlin.math.cos(angle).toFloat()
            val y = center.y + radius * kotlin.math.sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path, color = color, style = Fill)
    drawPath(path, color = outline, style = Stroke(width = radius * 0.15f))
}

/** Rare: Diamant/Kristall. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRareIcon(center: Offset, radius: Float) {
    val color = Color(0xFFCE93D8)
    val accent = Color(0xFFF3E5F5)
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.7f, center.y - radius * 0.1f)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.7f, center.y - radius * 0.1f)
        close()
    }
    drawPath(path, color = color, style = Fill)
    // Facetten-Linie für Glitzer-Effekt
    drawLine(accent, Offset(center.x, center.y - radius), Offset(center.x, center.y + radius * 0.3f), strokeWidth = radius * 0.08f)
}