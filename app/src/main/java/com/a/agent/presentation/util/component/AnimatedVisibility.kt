package com.a.agent.presentation.util.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween()
        ) + fadeIn(tween()),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween()
        ) + fadeOut(tween())
    ) {
        content()
    }
}

@Composable
fun CustomSlideLeftAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween()
        ) + fadeIn(tween()),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween()
        ) + fadeOut(tween())
    ) {
        content()
    }
}

@Composable
fun CustomSlideRightAnimatedVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable (() -> Unit)
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween()
        ) + fadeIn(tween()),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween()
        ) + fadeOut(tween())
    ) {
        content()
    }
}