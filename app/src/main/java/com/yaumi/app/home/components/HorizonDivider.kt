package com.yaumi.app.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yaumi.app.ui.theme.GoldCrescent

/**
 * Thin horizontal gold-fade line. Used as a soft "horizon" between the dark
 * gradient hero region and the light lavender reading region below.
 */
@Composable
fun HorizonDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 10.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        GoldCrescent.copy(alpha = 0.55f),
                        Color.Transparent
                    )
                )
            )
    )
}
