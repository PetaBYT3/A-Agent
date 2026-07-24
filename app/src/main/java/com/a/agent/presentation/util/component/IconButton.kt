package com.a.agent.presentation.util.component

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CustomSurfaceIconButton(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit),
    icon: ImageVector
) {
    IconButton(
        onClick = onClick,
        content = { Icon(icon, null) }
    )
}