package com.yaumi.app.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.theme.OnNightHigh
import com.yaumi.app.ui.theme.OnNightLine

/**
 * Single-line location pill. Caps at maxWidth so long city names ellipsize
 * instead of wrapping vertically (the bug from the original SholatCard).
 */
@Composable
fun LocationPill(
    city: String,
    modifier: Modifier = Modifier,
    onDark: Boolean = true
) {
    val tint = if (onDark) OnNightHigh else MaterialTheme.colorScheme.onSurface
    val containerBorder = if (onDark) OnNightLine else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bg = if (onDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier
            .widthIn(max = 180.dp)
            .background(bg, RoundedCornerShape(50))
            .border(0.5.dp, containerBorder, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = city,
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
