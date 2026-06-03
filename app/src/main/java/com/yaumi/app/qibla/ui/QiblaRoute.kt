package com.yaumi.app.qibla.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaumi.app.ui.components.OrnamentPattern
import com.yaumi.app.ui.theme.GoldCrescent
import com.yaumi.app.ui.theme.IndigoNight
import com.yaumi.app.ui.theme.Ink500
import com.yaumi.app.ui.theme.Ink700
import com.yaumi.app.ui.theme.Ink900
import com.yaumi.app.ui.theme.Lavender100
import com.yaumi.app.ui.theme.RoyalPurple
import com.yaumi.app.ui.theme.SoftViolet
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaRoute(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val vm: QiblaViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val animatedHeading by animateFloatAsState(
        targetValue = state.heading.toFloat(),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 240f),
        label = "qibla_heading"
    )
    val animatedBearing by animateFloatAsState(
        targetValue = state.qiblaBearing.toFloat(),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 240f),
        label = "qibla_bearing"
    )
    var locationPermissionDenied by remember { mutableStateOf(false) }

    val finePermission = Manifest.permission.ACCESS_FINE_LOCATION
    val coarsePermission = Manifest.permission.ACCESS_COARSE_LOCATION
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[finePermission] == true || result[coarsePermission] == true
        locationPermissionDenied = !granted
        vm.refresh()
    }
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, finePermission) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, coarsePermission) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            locationLauncher.launch(arrayOf(finePermission, coarsePermission))
        } else {
            locationPermissionDenied = false
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
        item {
            LocationCard(
                locationName = state.locationName,
                distanceKm = state.distanceKm,
                bearing = state.qiblaBearing,
                onRefresh = vm::refresh,
                isLoading = state.isLoading
            )
        }

        item {
            CompassFace(
                heading = animatedHeading,
                qiblaBearing = animatedBearing
            )
        }

        item {
            GuidanceCard(
                turn = state.turnDegrees,
                accuracy = state.sensorAccuracy
            )
        }

        if (locationPermissionDenied) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, Lavender100)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Izin lokasi ditolak",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Arah Kiblat dihitung dari lokasi default (Jakarta).",
                            color = Ink700,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { err ->
            item {
                Text(err, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun LocationCard(
    locationName: String,
    distanceKm: Double,
    bearing: Double,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
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
                    Brush.verticalGradient(
                        colors = listOf(IndigoNight, RoyalPurple)
                    )
                )
        ) {
            OrnamentPattern(
                modifier = Modifier.matchParentSize(),
                color = GoldCrescent,
                alpha = 0.05f
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = GoldCrescent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = locationName.ifBlank { "—" },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    AssistChip(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        label = { Text("Refresh", color = Color.White) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            labelColor = Color.White
                        ),
                        border = null
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column {
                        Text(
                            text = "Jarak ke Ka'bah",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${"%,.0f".format(distanceKm)} km",
                            color = GoldCrescent,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = "Bearing",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${"%.1f".format(bearing)}°",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompassFace(heading: Float, qiblaBearing: Float) {
    // The dial rotates so that Kaaba's bearing aligns with where it should be
    // relative to current device heading. Pointer at top is fixed.
    val dialRotation = -heading
    val kaabaAngleOnDial = qiblaBearing - heading

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Lavender100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer dial canvas (fixed size)
            Box(
                modifier = Modifier
                    .size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rotating dial
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(dialRotation)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerR = size.minDimension / 2f - 4f
                    val midR = outerR - 18f
                    val innerR = outerR * 0.62f

                    // Outer ring fill
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SoftViolet.copy(alpha = 0.15f),
                                RoyalPurple.copy(alpha = 0.20f)
                            )
                        ),
                        radius = outerR,
                        center = center
                    )
                    // Outer ring border
                    drawCircle(
                        color = RoyalPurple.copy(alpha = 0.30f),
                        radius = outerR,
                        center = center,
                        style = Stroke(width = 1.4f)
                    )

                    // Tick marks every 6° (every 30° = major)
                    for (deg in 0 until 360 step 6) {
                        val rad = Math.toRadians(deg.toDouble() - 90.0)
                        val major = deg % 30 == 0
                        val tickStart = if (major) midR - 4f else midR + 4f
                        val tickEnd = midR + 12f
                        val sx = center.x + cos(rad).toFloat() * tickStart
                        val sy = center.y + sin(rad).toFloat() * tickStart
                        val ex = center.x + cos(rad).toFloat() * tickEnd
                        val ey = center.y + sin(rad).toFloat() * tickEnd
                        drawLine(
                            color = if (major) RoyalPurple else RoyalPurple.copy(alpha = 0.35f),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = if (major) 2.2f else 1f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Inner disc (lavender tinted)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Lavender100
                            )
                        ),
                        radius = innerR,
                        center = center
                    )
                    drawCircle(
                        color = RoyalPurple.copy(alpha = 0.25f),
                        radius = innerR,
                        center = center,
                        style = Stroke(width = 1f)
                    )

                    // Kaaba indicator on dial at qiblaAngleOnDial degrees relative to dial north
                    // Kaaba angle relative to dial-north is just qiblaBearing (because dial is rotated)
                    val kaabaRad = Math.toRadians(qiblaBearing.toDouble() - 90.0)
                    val kaabaDistance = midR - 30f
                    val kx = center.x + cos(kaabaRad).toFloat() * kaabaDistance
                    val ky = center.y + sin(kaabaRad).toFloat() * kaabaDistance

                    // Glow behind Kaaba marker
                    drawCircle(
                        color = GoldCrescent.copy(alpha = 0.35f),
                        radius = 22f,
                        center = Offset(kx, ky)
                    )
                    // Kaaba square marker
                    val markerSize = 22f
                    drawRect(
                        color = IndigoNight,
                        topLeft = Offset(kx - markerSize / 2f, ky - markerSize / 2f),
                        size = Size(markerSize, markerSize)
                    )
                    // Inner gold line on Kaaba (kiswah suggestion)
                    drawLine(
                        color = GoldCrescent,
                        start = Offset(kx - markerSize / 2f + 2f, ky),
                        end = Offset(kx + markerSize / 2f - 2f, ky),
                        strokeWidth = 2f
                    )
                }

                // Cardinal letters drawn upright (not rotated): we apply rotation manually
                // so the letters still rotate with the dial visually.
                CardinalLabels(rotation = dialRotation)

                // Fixed top pointer (gold arrow at top of bezel)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val outerR = size.minDimension / 2f - 4f
                    val tipY = (size.height / 2f) - outerR + 8f
                    val baseY = tipY + 28f
                    val halfW = 9f
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx, tipY)
                        lineTo(cx + halfW, baseY)
                        lineTo(cx - halfW, baseY)
                        close()
                    }
                    drawPath(path = path, color = GoldCrescent)
                }

                // Center dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(IndigoNight)
                )
            }
        }
    }
}

@Composable
private fun CardinalLabels(rotation: Float) {
    Box(modifier = Modifier
        .size(280.dp)
        .rotate(rotation)
    ) {
        val pad = 26.dp
        Text(
            text = "N",
            color = GoldCrescent,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = pad)
        )
        Text(
            text = "E",
            color = Ink900,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = pad)
        )
        Text(
            text = "S",
            color = Ink900,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = pad)
        )
        Text(
            text = "W",
            color = Ink900,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = pad)
        )
    }
}

@Composable
private fun GuidanceCard(turn: Double, accuracy: Int) {
    val absTurn = abs(turn)
    val isAligned = absTurn <= 5
    val direction = when {
        isAligned -> "Arah sudah tepat"
        turn > 0 -> "Putar perangkat ke kanan"
        else -> "Putar perangkat ke kiri"
    }
    val containerColor = if (isAligned) {
        GoldCrescent.copy(alpha = 0.15f)
    } else {
        Lavender100
    }
    val textColor = if (isAligned) IndigoNight else Ink900

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(0.5.dp, if (isAligned) GoldCrescent else Lavender100)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = if (isAligned) GoldCrescent else RoyalPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = direction,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                if (!isAligned) {
                    Text(
                        text = "${"%.0f".format(absTurn)}°",
                        color = RoyalPurple,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = accuracyText(accuracy),
                color = Ink500,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun accuracyText(accuracy: Int): String = when (accuracy) {
    3 -> "Akurasi sensor: tinggi"
    2 -> "Akurasi sensor: sedang"
    1 -> "Akurasi sensor: rendah — gerakkan perangkat dengan pola angka 8 untuk kalibrasi"
    0 -> "Sensor tidak dapat diandalkan — kalibrasi dengan menggerakkan perangkat dalam pola angka 8"
    else -> "Akurasi sensor belum diketahui"
}
