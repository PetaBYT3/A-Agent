package com.a.agent.presentation.util.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach

@Composable
fun CustomPopupMenu(
    content: @Composable ((expand: () -> Unit) -> Unit),
    items: List<Triple<() -> Unit, ImageVector, String>>
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }

    val expand = {
        isExpanded = true
    }
    Box {
        content(expand)
        DropdownMenu(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = AbsoluteRoundedCornerShape(15.dp),
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            items.fastForEach { triple ->
                DropdownMenuItem(
                    onClick = {
                        triple.first()
                        isExpanded = false
                    },
                    leadingIcon = { Icon(triple.second, null) },
                    text = { Text(text = triple.third) }
                )
            }
        }
    }
}