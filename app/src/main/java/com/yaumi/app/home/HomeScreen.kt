package com.yaumi.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Brush
import com.yaumi.app.ui.components.OrnamentPattern
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.RoyalPurple
import com.yaumi.app.ui.theme.SoftViolet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.Application
import com.yaumi.app.azan.domain.model.AzanUiData
import com.yaumi.app.azan.domain.model.PrayerTiming
import com.yaumi.app.azan.ui.AzanViewModel
import com.yaumi.app.home.components.AyatCard
import com.yaumi.app.home.data.DailyAyatProvider
import com.yaumi.app.home.components.DateBanner
import com.yaumi.app.tadabur.data.TadaburProgressStore
import com.yaumi.app.tadabur.data.TadaburRepository
import com.yaumi.app.home.components.ExploreGrid
import com.yaumi.app.home.components.HeroHeaderContent
import com.yaumi.app.home.components.HorizonDivider
import com.yaumi.app.home.components.PrayerHeroCard
import com.yaumi.app.home.components.PrayerHeroState
import com.yaumi.app.home.components.PrayerTimelineEntry
import com.yaumi.app.ui.theme.Lavender50
import com.yaumi.app.home.components.TadaburHeroCard
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

enum class FeatureKey(val label: String) {
    QURAN("Al-Qur'an"),
    HADIS("Hadis"),
    DOA("Doa Harian"),
    AZAN("Azan"),
    QIBLA("Kiblat"),
    TASBIH("Tasbih"),
    HIJRIAH("Hijriah"),
    TADABUR("Tadabur"),
    SETTINGS("Atur")
}

@Composable
fun HomeScreen(contentPadding: PaddingValues, onFeatureClick: (FeatureKey) -> Unit) {
    val vm: AzanViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1.seconds)
        }
    }
    LaunchedEffect(Unit) { vm.refresh() }

    val data = state.data
    val heroState = remember(data, now) { computePrayerHeroState(data, now) }
    val greeting = remember(now) { greetingText(now.hour) }
    val weekday = remember(now) { weekdayLabel(now.toLocalDate()) }
    var tadaburDay by remember { mutableStateOf(1) }
    val appContext = vm.getApplication<Application>().applicationContext

    LaunchedEffect(Unit) {
        val repo = TadaburRepository(appContext)
        val total = repo.getAll().size
        val progress = TadaburProgressStore(appContext)
        tadaburDay = progress.getCurrentDayIndex(total)
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IndigoNight, RoyalPurple, SoftViolet, SoftViolet)
                )
            ),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = contentPadding.calculateBottomPadding() + navBarPadding + 24.dp
        )
    ) {
        item {
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    HeroHeaderContent(
                        greeting = greeting,
                        onBellClick = { onFeatureClick(FeatureKey.SETTINGS) }
                    )
                    PrayerHeroCard(
                        state = heroState,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }

        item { HorizonDivider() }

        item {
            DateBanner(
                hijri = data?.hijriReadable?.takeIf { it.isNotBlank() && it != "-" }
                    ?: "—",
                gregorian = data?.dateReadable?.takeIf { it.isNotBlank() && it != "-" }
                    ?: defaultGregorian(now.toLocalDate()),
                weekday = weekday,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ExploreGrid(
                onFeatureClick = onFeatureClick,
                onSeeAllClick = {},
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            TadaburHeroCard(
                dayIndex = tadaburDay,
                onClick = { onFeatureClick(FeatureKey.TADABUR) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            AyatCard(
                data = DailyAyatProvider.forDate(now.toLocalDate()),
                onShareClick = {},
                onBookmarkClick = {},
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private fun greetingText(hour: Int): String = when {
    hour < 11 -> "pagi"
    hour < 15 -> "siang"
    hour < 18 -> "sore"
    else -> "malam"
}

private fun weekdayLabel(date: LocalDate): String {
    val day = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("id-ID"))
    return day.replaceFirstChar { it.titlecase(Locale.forLanguageTag("id-ID")) }
}

private fun defaultGregorian(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
    return date.format(formatter)
}

private fun computePrayerHeroState(data: AzanUiData?, now: LocalDateTime): PrayerHeroState {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val orderedNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    val timings = (data?.timings?.filter { it.name in orderedNames }
        ?: listOf(
            PrayerTiming("Fajr", "04:35"),
            PrayerTiming("Dhuhr", "12:03"),
            PrayerTiming("Asr", "15:24"),
            PrayerTiming("Maghrib", "18:07"),
            PrayerTiming("Isha", "19:18")
        )).sortedBy { orderedNames.indexOf(it.name) }

    val parsedTimes = timings.map { it to LocalTime.parse(it.time, formatter) }
    val today = now.toLocalDate()
    val nextEntry = parsedTimes.firstOrNull { today.atTime(it.second).isAfter(now) }
    val active = nextEntry ?: parsedTimes.first()
    val activeIndex = timings.indexOf(active.first)

    val nextDateTime = today.atTime(active.second)
    val resolvedNext = if (nextDateTime.isAfter(now)) nextDateTime else nextDateTime.plusDays(1)
    val remaining = Duration.between(now, resolvedNext)

    val label = active.first.localizedLabel()
    val arabic = active.first.arabicLabel()

    return PrayerHeroState(
        activeLabel = label,
        activeArabic = arabic,
        activeTime = active.first.time,
        remaining = remaining,
        timeline = timings.map {
            PrayerTimelineEntry(label = it.localizedLabel(), time = it.time)
        },
        activeTimelineIndex = activeIndex,
        city = data?.cityName?.takeIf { it.isNotBlank() } ?: "Jakarta Selatan"
    )
}

private fun PrayerTiming.localizedLabel(): String = when (name) {
    "Fajr" -> "Subuh"
    "Dhuhr" -> "Dzuhur"
    "Asr" -> "Ashar"
    "Maghrib" -> "Maghrib"
    "Isha" -> "Isya"
    else -> name
}

private fun PrayerTiming.arabicLabel(): String = when (name) {
    "Fajr" -> "الفجر"
    "Dhuhr" -> "الظهر"
    "Asr" -> "العصر"
    "Maghrib" -> "المغرب"
    "Isha" -> "العشاء"
    else -> ""
}
