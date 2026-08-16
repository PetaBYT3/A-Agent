@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.util.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomSegmentedListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    index: Int = 0,
    count: Int = 1,
    leadingContent: @Composable (() -> Unit)? = null,
    overline: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit),
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    additionalContent: @Composable (() -> Unit)? = null,
) {
    SegmentedListItem(
        modifier = modifier,
        onClick = { onClick?.invoke() },
        colors = colors,
        shapes = if (count <= 1) {
            ListItemDefaults.shapes(
                shape = AbsoluteRoundedCornerShape(15.dp)
            )
        } else {
            ListItemDefaults.segmentedShapes(
                index = index,
                count = count,
            )
        },
        leadingContent = leadingContent,
        overlineContent = overline,
        content = content,
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (supportingContent != null) supportingContent()
                if (additionalContent != null) additionalContent()
            }
        },
        trailingContent = trailingContent,
    )
}


fun LazyListScope.listTitle(
    title: String,
    content: @Composable (() -> Unit)? = null
) {
    item {
        Row(
            modifier = Modifier
                .padding(bottom = 5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            if (content != null) content()
        }
    }
}

fun LazyListScope.loadingIndicator() {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator(
                modifier = Modifier
                    .animateItem()
            )
        }
    }
}

enum class MessageType {
    Info, Warning, Error
}

fun LazyListScope.message(message: String, messageType: MessageType = MessageType.Info) {
    item {
        CustomSegmentedListItem(
            modifier = Modifier
                .animateItem(),
            leadingContent = {
                Icon(
                    tint = when (messageType) {
                        MessageType.Info -> LocalContentColor.current
                        MessageType.Warning -> Color.Yellow
                        MessageType.Error -> MaterialTheme.colorScheme.error
                    },
                    imageVector = when (messageType) {
                        MessageType.Info -> Icons.Rounded.Info
                        MessageType.Warning -> Icons.Rounded.Warning
                        MessageType.Error -> Icons.Rounded.Error
                    },
                    contentDescription = null
                )
            },
            content = {
                Text(
                    text = when (messageType) {
                        MessageType.Info -> "Info"
                        MessageType.Warning -> "Warning"
                        MessageType.Error -> "Error"
                    }
                )
            },
            supportingContent = { Text(text = message) }
        )
    }
}

@Composable
fun Message(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    index: Int = 0,
    count: Int = 1,
    message: String,
    messageType: MessageType = MessageType.Info
) {
    val containerColor = when (messageType) {
        MessageType.Info -> MaterialTheme.colorScheme.surfaceContainer
        MessageType.Warning -> Color.Yellow
        MessageType.Error -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (messageType) {
        MessageType.Info -> Color.Unspecified
        MessageType.Warning -> Color.Black
        MessageType.Error -> MaterialTheme.colorScheme.onErrorContainer
    }
    CustomSegmentedListItem(
        modifier = modifier
            .height(50.dp),
        onClick = onClick,
        colors = ListItemDefaults.colors(
            containerColor = containerColor,
            leadingContentColor = contentColor,
            contentColor = contentColor
        ),
        index = index,
        count = count,
        leadingContent = {
            Icon(
                imageVector = when (messageType) {
                    MessageType.Info -> Icons.Rounded.Info
                    MessageType.Warning -> Icons.Rounded.Warning
                    MessageType.Error -> Icons.Rounded.Error
                },
                contentDescription = null
            )
        },
        content = {
            SupportingText(
                text = message,
                isSingleLine = false
            )
        }
    )
}

fun LazyListScope.itemColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable (() -> Unit)
) {
    item {
        Column(
            modifier = modifier
                .animateItem(),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            content()
        }
    }
}

fun LazyListScope.itemRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable (RowScope.() -> Unit)
) {
    item {
        Row(
            modifier = modifier
                .animateItem(),
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement
        ) {
            content()
        }
    }
}

fun LazyListScope.spacer(dp: Dp = 10.dp) {
    item { Spacer(modifier = Modifier.height(dp)) }
}