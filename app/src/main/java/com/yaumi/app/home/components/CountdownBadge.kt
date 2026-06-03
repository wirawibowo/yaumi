package com.yaumi.app.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Duration

/**
 * Pretty countdown pill. Renders as `tersisa 9j 08m 16d` (or `5m 12d`,
 * or `45d`) depending on remaining magnitude — friendlier than `09:08:16`.
 */
@Composable
fun CountdownBadge(
    remaining: Duration,
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFFF5C518),
    container: Color = Color.White.copy(alpha = 0.10f),
    label: String = "tersisa"
) {
    val safe = if (remaining.isNegative) Duration.ZERO else remaining
    val totalSeconds = safe.seconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val display = when {
        hours > 0 -> "${hours}j ${minutes.toString().padStart(2, '0')}m"
        minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}d"
        else -> "${seconds}d"
    }

    Row(
        modifier = modifier
            .background(container, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.HourglassBottom,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.width(6.dp))
        AnimatedContent(
            targetState = display,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "countdown"
        ) { value ->
            Text(
                text = value,
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
