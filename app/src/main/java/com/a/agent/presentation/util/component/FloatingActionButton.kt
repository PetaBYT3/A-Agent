@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.util.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CustomFloatingActionButton(
    modifier: Modifier = Modifier,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(FloatingActionButtonDefaults.containerColor),
    onClick: (() -> Unit),
    isLoading: Boolean = false,
    icon: ImageVector
) {
    FloatingActionButton(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        onClick = { if (!isLoading) onClick() },
        content = {
            if (isLoading) {
                ContainedLoadingIndicator(
                    modifier = Modifier
                        .size(FloatingActionButtonDefaults.LargeIconSize)
                )
            } else {
                Icon(icon, null)
            }
        }
    )
}