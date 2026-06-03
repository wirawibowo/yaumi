package com.yaumi.app.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yaumi.app.home.FeatureKey
import com.yaumi.app.ui.theme.Ink500
import com.yaumi.app.ui.theme.Ink900
import com.yaumi.app.ui.theme.Lavender100

private data class FeatureSpec(
    val key: FeatureKey,
    val icon: ImageVector,
    val tint: Color
)

private val ExploreFeatures = listOf(
    FeatureSpec(FeatureKey.QURAN, Icons.Default.Book, Lavender100),
    FeatureSpec(FeatureKey.HADIS, Icons.Default.Book, Lavender100),
    FeatureSpec(FeatureKey.DOA, Icons.Default.FavoriteBorder, Lavender100),
    FeatureSpec(FeatureKey.TADABUR, Icons.Default.Book, Lavender100),
    FeatureSpec(FeatureKey.QIBLA, Icons.Default.Explore, Lavender100),
    FeatureSpec(FeatureKey.TASBIH, Icons.Default.SelfImprovement, Lavender100),
    FeatureSpec(FeatureKey.HIJRIAH, Icons.Default.CalendarMonth, Lavender100)
)

@Composable
fun ExploreGrid(
    onFeatureClick: (FeatureKey) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jelajahi",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Lihat semua  ›",
                color = com.yaumi.app.ui.theme.GoldCrescent,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        val rows = ExploreFeatures.chunked(3)
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { spec ->
                    FeatureTile(
                        label = spec.key.label,
                        icon = spec.icon,
                        tint = spec.tint,
                        onClick = { onFeatureClick(spec.key) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index < rows.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}
