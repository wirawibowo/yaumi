package com.yaumi.app.tasbih.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaumi.app.ui.theme.AmiriFontFamily
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.Ink500
import com.yaumi.app.ui.theme.Ink700
import com.yaumi.app.ui.theme.Ink900
import com.yaumi.app.ui.theme.Lavender100
import com.yaumi.app.ui.theme.RoyalPurple
import com.yaumi.app.ui.theme.SoftViolet

private data class Zikir(
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val target: Int
)

private val ZikirList = listOf(
    Zikir("سُبْحَانَ اللّٰه", "Subhanallah", "Maha Suci Allah", 33),
    Zikir("اَلْحَمْدُ لِلّٰه", "Alhamdulillah", "Segala puji bagi Allah", 33),
    Zikir("اللّٰهُ أَكْبَر", "Allahu Akbar", "Allah Maha Besar", 33),
    Zikir("أَسْتَغْفِرُ اللّٰه", "Astaghfirullah", "Aku memohon ampun kepada Allah", 100),
    Zikir("لَا إِلٰهَ إِلَّا اللّٰه", "La ilaha illallah", "Tiada Tuhan selain Allah", 100)
)

@Composable
fun TasbihRoute(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var zikirIndex by rememberSaveable { mutableIntStateOf(0) }
    var count by rememberSaveable { mutableIntStateOf(0) }
    var totalSession by rememberSaveable { mutableIntStateOf(0) }
    val zikir = ZikirList[zikirIndex]
    val target = zikir.target
    val progress = (count.coerceAtMost(target).toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, animationSpec = tween(220), label = "progress")

    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(if (pressed) 0.96f else 1f, animationSpec = tween(120), label = "press")

    fun increment() {
        count += 1
        totalSession += 1
        triggerHaptic(context)
        if (count == target) {
            zikirIndex = (zikirIndex + 1) % ZikirList.size
            count = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Lavender100),
            border = BorderStroke(0.5.dp, Lavender100)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = zikir.arabic,
                    fontFamily = AmiriFontFamily,
                    fontSize = 32.sp,
                    color = Ink900,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = zikir.transliteration,
                    style = MaterialTheme.typography.titleMedium,
                    color = RoyalPurple,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = zikir.translation,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink500,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(pressScale)
                .clickable {
                    pressed = true
                    increment()
                    pressed = false
                },
            contentAlignment = Alignment.Center
        ) {
            // Progress ring
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 14.dp.toPx()
                val diameter = size.minDimension - stroke
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                drawArc(
                    color = Lavender100,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = androidx.compose.ui.geometry.Size(diameter, diameter),
                    style = Stroke(width = stroke)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(SoftViolet, RoyalPurple, GoldCrescent, GoldCrescent)),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = androidx.compose.ui.geometry.Size(diameter, diameter),
                    style = Stroke(width = stroke)
                )
            }
            // Inner circle button
            Box(
                modifier = Modifier
                    .size(196.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SoftViolet, RoyalPurple, IndigoNight)
                        )
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = count.toString(),
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ $target",
                        color = GoldCrescent,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Tap lingkaran untuk menghitung",
            color = Ink500,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Refresh,
                label = "Reset",
                modifier = Modifier.weight(1f)
            ) {
                count = 0
            }
            ActionButton(
                icon = Icons.Default.SkipNext,
                label = "Zikir Berikutnya",
                modifier = Modifier.weight(1f)
            ) {
                zikirIndex = (zikirIndex + 1) % ZikirList.size
                count = 0
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Lavender100),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total sesi",
                        color = Ink500,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = totalSession.toString(),
                        color = Ink900,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Lavender100)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${ZikirList.size} jenis zikir",
                        color = Ink700,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Lavender100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalPurple,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = Ink700,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun triggerHaptic(context: Context) {
    runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
