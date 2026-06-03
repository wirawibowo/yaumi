package com.yaumi.app.home.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.OnNightHigh
import com.yaumi.app.ui.theme.OnNightLine
import com.yaumi.app.ui.theme.OnNightLow
import com.yaumi.app.ui.theme.OnNightMid

data class PrayerTimelineEntry(
    val label: String,
    val time: String
)

@Composable
fun PrayerTimeline(
    entries: List<PrayerTimelineEntry>,
    activeIndex: Int,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return
    val infinite = rememberInfiniteTransition(label = "active-pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .height(28.dp)
        ) {
            // Connecting hairline
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)) {
                val y = size.height / 2f
                drawLine(
                    color = OnNightLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                entries.forEachIndexed { index, _ ->
                    val isActive = index == activeIndex
                    val baseRadius = if (isActive) 7.dp else 4.dp
                    Box(
                        modifier = Modifier.height(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.height(24.dp).padding(horizontal = 6.dp)) {
                            val r = if (isActive) (baseRadius.toPx() * pulse) else baseRadius.toPx()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            if (isActive) {
                                drawCircle(
                                    color = GoldCrescent.copy(alpha = 0.25f),
                                    radius = r * 2f,
                                    center = center
                                )
                            }
                            drawCircle(
                                color = if (isActive) GoldCrescent else Color.White.copy(alpha = 0.30f),
                                radius = r,
                                center = center
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            entries.forEachIndexed { index, entry ->
                val isActive = index == activeIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = entry.label,
                        color = if (isActive) GoldCrescent else OnNightMid,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = entry.time,
                        color = if (isActive) GoldCrescent else OnNightHigh,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
