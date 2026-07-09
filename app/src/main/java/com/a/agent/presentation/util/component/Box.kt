package com.a.agent.presentation.util.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomFadeBox(
    modifier: Modifier = Modifier,
    fadeTop: Dp = 50.dp,
    fadeBottom: Dp = 50.dp,
    content: @Composable (BoxScope.() -> Unit)
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val smoothColorsTop = listOf(
        backgroundColor,
        backgroundColor.copy(alpha = 0.90f),
        backgroundColor.copy(alpha = 0.85f),
        backgroundColor.copy(alpha = 0.80f),
        backgroundColor.copy(alpha = 0.75f),
        backgroundColor.copy(alpha = 0.70f),
        backgroundColor.copy(alpha = 0.46f),
        backgroundColor.copy(alpha = 0.23f),
        Color.Transparent
    )
    val smoothColorsBottom = smoothColorsTop.reversed()

    Box(
        modifier = modifier
    ) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(fadeTop)
                .background(
                    brush = Brush.verticalGradient(
                        colors = smoothColorsTop
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(fadeBottom)
                .background(
                    brush = Brush.verticalGradient(
                        colors = smoothColorsBottom
                    )
                )
        )
    }
}