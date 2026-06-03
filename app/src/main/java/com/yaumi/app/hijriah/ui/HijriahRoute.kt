package com.yaumi.app.hijriah.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaumi.app.azan.data.AzanRepository
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.Ink500
import com.yaumi.app.ui.theme.Ink700
import com.yaumi.app.ui.theme.Ink900
import com.yaumi.app.ui.theme.Lavender100
import com.yaumi.app.ui.theme.RoyalPurple
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class HijriToday(
    val day: String,
    val monthName: String,
    val year: String
)

private val ImportantDates = listOf(
    "1 Muharam" to "Tahun Baru Hijriah",
    "10 Muharam" to "Hari Asyura",
    "12 Rabiulawal" to "Maulid Nabi Muhammad ﷺ",
    "27 Rajab" to "Isra' Mi'raj",
    "15 Syakban" to "Nisfu Sya'ban",
    "1 Ramadan" to "Awal Puasa Ramadan",
    "17 Ramadan" to "Nuzulul Qur'an",
    "1 Syawal" to "Idul Fitri",
    "10 Zulhijah" to "Idul Adha",
    "8-13 Zulhijah" to "Hari Tasyrik & Haji"
)

@Composable
fun HijriahRoute(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var hijri by remember { mutableStateOf<HijriToday?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        runCatching {
            val data = AzanRepository(context).loadAzanData()
            val parts = data.hijriReadable.trim().split(" ")
            if (parts.size >= 3) {
                HijriToday(
                    day = parts[0],
                    monthName = parts.subList(1, parts.size - 1).joinToString(" "),
                    year = parts.last()
                )
            } else null
        }.onSuccess {
            hijri = it
            loading = false
        }.onFailure {
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TodayHeroCard(hijri = hijri, loading = loading) }
        item { GregorianCard() }
        item { ImportantDatesCard() }
    }
}

@Composable
private fun TodayHeroCard(hijri: HijriToday?, loading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(0.5.dp, Lavender100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(colors = listOf(IndigoNight, RoyalPurple))
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldCrescent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = GoldCrescent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Tanggal Hijriah",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.size(20.dp))
                if (loading) {
                    Text(
                        text = "Memuat...",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.titleLarge
                    )
                } else if (hijri == null) {
                    Text(
                        text = "Data tidak tersedia",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = hijri.day,
                            color = GoldCrescent,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Text(
                                text = hijri.monthName,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${hijri.year} H",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GregorianCard() {
    val today = LocalDate.now()
    val locale = Locale.forLanguageTag("id-ID")
    val weekday = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, locale)
        .replaceFirstChar { it.titlecase(locale) }
    val gregorian = today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", locale))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Lavender100)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tanggal Masehi",
                color = Ink500,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = gregorian,
                color = Ink900,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = weekday,
                color = Ink500,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ImportantDatesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Lavender100)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tanggal Penting",
                color = Ink900,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.size(12.dp))
            ImportantDates.forEach { (date, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Lavender100)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = date,
                            color = RoyalPurple,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = name,
                        color = Ink700,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
