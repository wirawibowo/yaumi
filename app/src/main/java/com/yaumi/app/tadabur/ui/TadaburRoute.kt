package com.yaumi.app.tadabur.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaumi.app.ui.theme.AmiriFontFamily
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.Ink500
import com.yaumi.app.ui.theme.Ink900
import com.yaumi.app.ui.theme.SurfaceWhite

@Composable
fun TadaburRoute(contentPadding: PaddingValues, onComplete: () -> Unit) {
    val vm: TadaburViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val data = state.data
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DayHeader(dayIndex = state.dayIndex)

        if (data != null) {
            AyahBlock(
                surah = data.surahName,
                ayahRange = data.ayahRange,
                arabic = data.arabicText,
                translation = data.translationId
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberedCard(
                    number = 1,
                    title = "Hikmah Ayat",
                    content = data.hikmah
                )
                NumberedCard(
                    number = 2,
                    title = "Praktik Ayat",
                    content = data.praktik.joinToString("\n\n")
                )
                AmalTrackerCard(
                    items = data.amalTracker,
                    checked = state.checklist,
                    onToggle = vm::toggleChecklist
                )
                if (data.hadithArabic.isNotBlank() || data.hadithTranslation.isNotBlank()) {
                    HadisCard(
                        arabic = data.hadithArabic,
                        translation = data.hadithTranslation,
                        reference = data.hadithReference
                    )
                }
            }

            Button(
                onClick = {
                    vm.markCompleted()
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isCompleted
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (state.isCompleted) "Hari ini sudah selesai" else "Selesai Hari Ini")
            }

            if (!state.isCompleted) {
                Button(
                    onClick = vm::skipToday,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lewati Hari Ini")
                }
            }
        }
    }
}

@Composable
private fun DayHeader(dayIndex: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Ink900,
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = "DAY $dayIndex",
            modifier = Modifier.padding(vertical = 12.dp),
            color = androidx.compose.ui.graphics.Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AyahBlock(surah: String, ayahRange: String, arabic: String, translation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                color = Ink900,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$surah : $ayahRange",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = arabic,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = AmiriFontFamily),
                textAlign = TextAlign.Center
            )
            Text(
                text = translation,
                style = MaterialTheme.typography.bodySmall,
                color = Ink500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NumberedCard(number: Int, title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitlePill(number = number, title = title)
            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = Ink500)
        }
    }
}

@Composable
private fun AmalTrackerCard(
    items: List<String>,
    checked: List<Boolean>,
    onToggle: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitlePill(number = 3, title = "Amal Tracker")
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(GoldCrescent.copy(alpha = 0.25f), GoldCrescent.copy(alpha = 0.08f))))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item, style = MaterialTheme.typography.bodySmall, color = Ink900)
                    Checkbox(
                        checked = checked.getOrNull(index) == true,
                        onCheckedChange = { onToggle(index, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HadisCard(arabic: String, translation: String, reference: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitlePill(number = 4, title = "Hadist Pilihan")
            Text(
                text = arabic,
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = AmiriFontFamily),
                textAlign = TextAlign.Center
            )
            Text(
                text = translation,
                style = MaterialTheme.typography.bodySmall,
                color = Ink500,
                textAlign = TextAlign.Center
            )
            HorizontalDivider()
            Text(
                text = reference,
                style = MaterialTheme.typography.labelMedium,
                color = Ink500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TitlePill(number: Int, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BoxCircleNumber(number)
        Spacer(modifier = Modifier.size(8.dp))
        Surface(color = Ink900, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun BoxCircleNumber(number: Int) {
    Surface(color = GoldCrescent, shape = CircleShape) {
        Text(
            text = number.toString(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Ink900,
            fontWeight = FontWeight.SemiBold
        )
    }
}
