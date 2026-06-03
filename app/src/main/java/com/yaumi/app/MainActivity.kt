package com.yaumi.app

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.components.OrnamentPattern
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.GoldSoft
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.RoyalPurple
import com.yaumi.app.ui.theme.SoftViolet
import com.yaumi.app.azan.alarm.AzanAlarmScheduler
import com.yaumi.app.azan.data.AzanSettingsStore
import com.yaumi.app.azan.notifications.AzanNotificationHelper
import com.yaumi.app.navigation.AppNavigation
import com.yaumi.app.tadabur.data.TadaburSettingsStore
import com.yaumi.app.tadabur.worker.TadaburAlarmScheduler
import com.yaumi.app.ui.theme.YaumiTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AzanNotificationHelper.ensureChannel(applicationContext)

        val settings = AzanSettingsStore(applicationContext).load()
        if (settings.notificationEnabled) {
            AzanAlarmScheduler.rescheduleFromRepository(applicationContext)
            requestBatteryOptimizationExemption()
            requestExactAlarmPermission()
        }

        val tadaburSettings = TadaburSettingsStore(applicationContext).load()
        if (tadaburSettings.notificationEnabled) {
            TadaburAlarmScheduler.scheduleFromAzan(
                context = applicationContext,
                data = null,
                offsetMinutes = tadaburSettings.offsetMinutesAfterFajr
            )
        }

        setContent {
            YaumiApp()
        }
    }

    private fun requestBatteryOptimizationExemption() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            runCatching {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                runCatching {
                    startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun YaumiApp() {
    YaumiTheme {
        var showSplash by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(1500)
            showSplash = false
        }

        if (showSplash) {
            SplashScreen()
        } else {
            AppNavigation()
        }
    }
}

@Composable
private fun SplashScreen() {
    val infinite = rememberInfiniteTransition(label = "splash-pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(IndigoNight, RoyalPurple, SoftViolet))
            ),
        contentAlignment = Alignment.Center
    ) {
        OrnamentPattern(
            modifier = Modifier.matchParentSize(),
            color = GoldCrescent,
            alpha = 0.06f,
            tileSize = 96f
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(colors = listOf(Color(0xFF2A1B6E), IndigoNight))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(88.dp)) {
                    drawCrescentAndStar(goldColor = GoldCrescent, innerColor = IndigoNight, starColor = GoldSoft)
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Yaumi",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "يَوْمِي • Teman Ibadah Harian",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(40.dp))
            Text(
                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                color = GoldCrescent.copy(alpha = 0.85f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

private fun DrawScope.drawCrescentAndStar(goldColor: Color, innerColor: Color, starColor: Color) {
    val w = size.width
    val h = size.height
    val cx = w * 0.44f
    val cy = h / 2f
    val r = minOf(w, h) * 0.38f

    // Gold crescent body
    drawCircle(color = goldColor, radius = r, center = Offset(cx, cy))
    // Carve crescent (offset right + slightly up)
    drawCircle(color = innerColor, radius = r * 0.82f, center = Offset(cx + r * 0.26f, cy - r * 0.06f))

    // 5-point star beside crescent
    val starCx = cx + r * 1.08f
    val starCy = cy - r * 0.58f
    val outerR = r * 0.30f
    val innerR = outerR * 0.42f

    val starPath = Path()
    for (i in 0 until 10) {
        val angle = Math.PI / 5.0 * i - Math.PI / 2.0
        val radius = if (i % 2 == 0) outerR else innerR
        val px = (starCx + radius * Math.cos(angle)).toFloat()
        val py = (starCy + radius * Math.sin(angle)).toFloat()
        if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
    }
    starPath.close()
    drawPath(path = starPath, color = starColor)
}
