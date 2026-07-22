@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.util.component

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onSurface: Boolean = true,
    content: @Composable (() -> Unit)? = null
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(AbsoluteRoundedCornerShape(50))
                    .fillMaxHeight()
                    .background(if (onSurface) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                HeadlineText(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    text = title
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (content != null) content()
        }
        Spacer(modifier = Modifier.height(5.dp))
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
    index: Int = 0,
    count: Int = 1,
    message: String,
    messageType: MessageType = MessageType.Info
) {
    CustomSegmentedListItem(
        index = index,
        count = count,
        modifier = Modifier,
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