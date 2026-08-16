package com.a.agent.presentation.util.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomTopAppBar(
    modifier: Modifier = Modifier,
    onNavigationClick: (() -> Unit)? = null,
    title: String,
    action: @Composable (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(
                    onClick = onNavigationClick,
                    content = { Icon(Icons.Rounded.ArrowBack, null) }
                )
            }
        },
        title = { Text(text = title) },
        actions = {
            action()
            Spacer(modifier = Modifier.width(7.dp))
        }
    )
}