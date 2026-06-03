package com.yaumi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tile-able 8-point Islamic star pattern. Drawn at low alpha for use as a
 * background ornament on dark surfaces (hero header, prayer card).
 */
@Composable
fun OrnamentPattern(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    alpha: Float = 0.06f,
    tileSize: Float = 84f,
    starRadiusFraction: Float = 0.34f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val tilesX = (w / tileSize).toInt() + 2
        val tilesY = (h / tileSize).toInt() + 2
        val starRadius = tileSize * starRadiusFraction
        val tinted = color.copy(alpha = alpha)

        for (ix in 0..tilesX) {
            for (iy in 0..tilesY) {
                val cx = ix * tileSize
                val cy = iy * tileSize
                drawEightPointStar(
                    centerX = cx,
                    centerY = cy,
                    outer = starRadius,
                    inner = starRadius * 0.42f,
                    strokeColor = tinted,
                    strokeWidth = 1.1f
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEightPointStar(
    centerX: Float,
    centerY: Float,
    outer: Float,
    inner: Float,
    strokeColor: Color,
    strokeWidth: Float
) {
    val points = 8
    val total = points * 2
    val path = Path()
    for (i in 0 until total) {
        val r = if (i % 2 == 0) outer else inner
        val angle = Math.PI * i / points
        val x = centerX + (r * cos(angle)).toFloat()
        val y = centerY + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    rotate(degrees = 22.5f, pivot = Offset(centerX, centerY)) {
        drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth))
    }
}
