@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.util.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dangerous
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

fun LazyListScope.message(message: String, isError: Boolean = false) {
    item {
        CustomSegmentedListItem(
            modifier = Modifier
                .animateItem(),
            leadingContent = {
                Icon(
                    tint = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current,
                    imageVector = if (isError) Icons.Rounded.Warning else Icons.Rounded.Info,
                    contentDescription = null
                )
            },
            content = {
                SupportingText(
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Unspecified,
                    text = message,
                    isSingleLine = false
                )
            }
        )
    }
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

fun LazyListScope.spacer(dp: Dp = 10.dp) {
    item { Spacer(modifier = Modifier.height(dp)) }
}