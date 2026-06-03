package com.yaumi.app.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.GoldSoft
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.OnNightHigh
import com.yaumi.app.ui.theme.OnNightMid
import com.yaumi.app.ui.theme.RoyalPurple

@Composable
fun HeroHeaderContent(
    greeting: String,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Crescent moon + star icon (same motif as splash screen)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(colors = listOf(RoyalPurple, IndigoNight))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        drawMiniCrescentStar(goldColor = GoldCrescent, starColor = GoldSoft, bgColor = IndigoNight)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Yaumi",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "يَوْمِي • Teman ibadah harian",
                        color = OnNightMid,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .clickable { onBellClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan",
                    tint = Color.White
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Selamat $greeting,",
            color = OnNightHigh,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Sahabat",
            color = GoldCrescent,
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Semoga hari ini penuh keberkahan ✦",
            color = OnNightMid,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun DrawScope.drawMiniCrescentStar(goldColor: Color, starColor: Color, bgColor: Color) {
    val w = size.width
    val h = size.height
    val cx = w * 0.42f
    val cy = h / 2f
    val r = minOf(w, h) * 0.40f

    // Gold crescent body
    drawCircle(color = goldColor, radius = r, center = Offset(cx, cy))
    // Carve out crescent
    drawCircle(color = bgColor, radius = r * 0.80f, center = Offset(cx + r * 0.28f, cy - r * 0.08f))

    // 5-point star
    val starCx = w * 0.82f
    val starCy = h * 0.28f
    val outerR = r * 0.28f
    val innerR = outerR * 0.45f
    val path = Path()
    for (i in 0 until 10) {
        val angle = Math.PI / 5.0 * i - Math.PI / 2.0
        val radius = if (i % 2 == 0) outerR else innerR
        val px = (starCx + radius * Math.cos(angle)).toFloat()
        val py = (starCy + radius * Math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path = path, color = starColor)
}
