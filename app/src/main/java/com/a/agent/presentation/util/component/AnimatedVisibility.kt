package com.a.agent.presentation.util.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomShrinkDownAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = expandVertically(
            expandFrom = Alignment.Top
        ) + fadeIn(tween()),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top
        ) + fadeOut(tween())
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            content()
        }
    }
}

@Composable
fun CustomShrinkUpAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = expandVertically(
            expandFrom = Alignment.Bottom
        ) + fadeIn(tween()),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Bottom
        ) + fadeOut(tween())
    ) {
        content()
    }
}

@Composable
fun CustomShrinkLeftAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = expandHorizontally(
            expandFrom = Alignment.End
        ) + fadeIn(tween()),
        exit = shrinkHorizontally(
            shrinkTowards = Alignment.End
        ) + fadeOut(tween())
    ) {
        content()
    }
}

@Composable
fun CustomShrinkRightAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = expandHorizontally(
            expandFrom = Alignment.Start
        ) + fadeIn(tween()),
        exit = shrinkHorizontally(
            shrinkTowards = Alignment.Start
        ) + fadeOut(tween())
    ) {
        content()
    }
}

@Composable
fun CustomSlideUpAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it }
        ) + fadeOut()
    ) {
        content()
    }
}

@Composable
fun CustomFadeAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(tween()),
        exit = fadeOut(tween())
    ) {
        content()
    }
}