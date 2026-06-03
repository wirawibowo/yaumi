package com.yaumi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.OnNightHigh
import com.yaumi.app.ui.theme.OnNightMid
import com.yaumi.app.ui.theme.RoyalPurple
import com.yaumi.app.ui.theme.SoftViolet

/**
 * Compact gradient header used by every non-home route.
 * Includes status bar inset so it draws edge-to-edge.
 */
@Composable
fun RouteHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IndigoNight, RoyalPurple, SoftViolet)
                )
            )
    ) {
        OrnamentPattern(
            modifier = Modifier.matchParentSize(),
            color = GoldCrescent,
            alpha = 0.05f
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.size(12.dp))
            }
            Column(modifier = Modifier
                .padding(start = if (onBack == null) 8.dp else 0.dp)
                .let { if (actions == null) it else it }
            ) {
                Text(
                    text = title,
                    color = OnNightHigh,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = OnNightMid,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (actions != null) {
                Spacer(modifier = Modifier
                    .weight(1f)
                )
                actions()
            }
        }
    }
}

/**
 * Convenience wrapper: pairs the gradient RouteHeader with a Lavender50
 * content area so every redesigned route reads consistently.
 */
@Composable
fun YaumiRouteScaffold(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RouteHeader(title = title, subtitle = subtitle, onBack = onBack, actions = actions)
        Box(modifier = Modifier.weight(1f, fill = true)) {
            content()
        }
    }
}
