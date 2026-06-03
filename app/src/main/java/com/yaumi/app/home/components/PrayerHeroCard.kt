package com.yaumi.app.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.components.OrnamentPattern
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.OnNightHigh
import com.yaumi.app.ui.theme.OnNightLine
import com.yaumi.app.ui.theme.OnNightMid
import com.yaumi.app.ui.theme.RoyalPurple
import java.time.Duration

data class PrayerHeroState(
    val activeLabel: String,
    val activeArabic: String,
    val activeTime: String,
    val remaining: Duration,
    val timeline: List<PrayerTimelineEntry>,
    val activeTimelineIndex: Int,
    val city: String
)

@Composable
fun PrayerHeroCard(
    state: PrayerHeroState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RoyalPurple,
                        IndigoNight
                    )
                )
            )
            .border(0.5.dp, OnNightLine, RoundedCornerShape(24.dp))
    ) {
        OrnamentPattern(
            modifier = Modifier.matchParentSize(),
            color = GoldCrescent,
            alpha = 0.05f,
            tileSize = 96f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sholat berikutnya",
                        color = OnNightMid,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(0.dp))
                    if (state.activeArabic.isNotBlank()) {
                        Text(
                            text = "  •  ${state.activeArabic}",
                            color = GoldCrescent.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                LocationPill(city = state.city, onDark = true)
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.activeLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.activeTime,
                    color = GoldCrescent,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            CountdownBadge(
                remaining = state.remaining,
                modifier = Modifier
            )

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = OnNightLine, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))

            PrayerTimeline(
                entries = state.timeline,
                activeIndex = state.activeTimelineIndex
            )
        }
    }
}
