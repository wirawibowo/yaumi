package com.yaumi.app.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.theme.GoldDeep
import com.yaumi.app.ui.theme.Ink500
import com.yaumi.app.ui.theme.Ink700
import com.yaumi.app.ui.theme.Ink900
import com.yaumi.app.ui.theme.Lavender100
import com.yaumi.app.ui.theme.Lavender200
import com.yaumi.app.ui.theme.RoyalPurple
import com.yaumi.app.ui.theme.SurfaceWhite

@Composable
fun DateBanner(
    hijri: String,
    gregorian: String,
    weekday: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(0.5.dp, Lavender100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateColumn(
                icon = Icons.Default.NightsStay,
                iconTint = GoldDeep,
                label = "Hijriah",
                value = hijri,
                subValue = null,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(40.dp)
                    .background(Lavender200)
            )
            Spacer(Modifier.width(0.dp))
            DateColumn(
                icon = Icons.Default.CalendarMonth,
                iconTint = RoyalPurple,
                label = "Masehi",
                value = gregorian,
                subValue = weekday,
                modifier = Modifier.weight(1f).padding(start = 14.dp)
            )
        }
    }
}

@Composable
private fun DateColumn(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    subValue: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Lavender100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f, fill = true)) {
            Text(
                text = label,
                color = Ink500,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Ink900,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!subValue.isNullOrBlank()) {
                Text(
                    text = subValue,
                    color = Ink700.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
