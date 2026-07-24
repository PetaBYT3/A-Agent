package com.a.agent.presentation.util.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun CustomBannerHolder(
    modifier: Modifier = Modifier,
    content: @Composable (height: Dp) -> Unit
) {
    val density = LocalDensity.current
    var bannerHeight by remember {
        mutableStateOf(0.dp)
    }
    Column(
        modifier = modifier
            .zIndex(1f)
            .padding(horizontal = 10.dp)
            .animateContentSize()
            .wrapContentHeight()
            .onGloballyPositioned { coordinates ->
                val heightInDp = with(density) {
                    coordinates.size.height.toDp()
                }
                bannerHeight = if (heightInDp > 0.dp) heightInDp + 10.dp else 0.dp
            },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content(bannerHeight)
    }
}

enum class BannerType {
    Info, Warning, Error
}

@Composable
fun BannerItem(
    modifier: Modifier = Modifier,
    message: String,
    bannerType: BannerType = BannerType.Info
) {
    val containerColor = when (bannerType) {
        BannerType.Info -> MaterialTheme.colorScheme.surfaceContainer
        BannerType.Warning -> Color.Yellow
        BannerType.Error -> MaterialTheme.colorScheme.error
    }
    val contentColor = when (bannerType) {
        BannerType.Info -> LocalContentColor.current
        BannerType.Warning -> Color.Black
        BannerType.Error -> MaterialTheme.colorScheme.error
    }
    val icon = when (bannerType) {
        BannerType.Info -> Icons.Rounded.Info
        BannerType.Warning -> Icons.Rounded.Warning
        BannerType.Error -> Icons.Rounded.Error
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AbsoluteRoundedCornerShape(10.dp))
            .background(containerColor),
    ) {
        Row(
            modifier = modifier
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(20.dp),
                tint = contentColor,
                imageVector = icon,
                contentDescription = null
            )
            SupportingText(
                color = contentColor,
                text = message,
                isSingleLine = true
            )
        }
    }
}