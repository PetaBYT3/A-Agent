package com.a.agent.presentation.util.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

enum class AnimatedContentState {
    IsLoading, IsError, IsEmpty, Success
}

@Composable
fun CustomAnimatedContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isError: Boolean,
    isEmpty: Boolean,
    content: @Composable ((AnimatedContentState) -> Unit)
) {
    AnimatedContent(
        modifier = modifier
            .animateContentSize(),
        targetState = when {
            isLoading -> AnimatedContentState.IsLoading
            isEmpty -> AnimatedContentState.IsEmpty
            isError -> AnimatedContentState.IsError
            else -> AnimatedContentState.Success
        },
        transitionSpec = { fadeIn(tween()) togetherWith fadeOut(tween()) }
    ) { state ->
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content(state)
        }
    }
}

@Composable
fun <S> CustomFadeAnimatedContent(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.CenterStart,
    targetState: S,
    content: @Composable ((animatedContentState: S) -> Unit)
) {
    AnimatedContent(
        modifier = modifier
            .animateContentSize(tween()),
        targetState = targetState,
        transitionSpec = { fadeIn(tween()) togetherWith fadeOut(tween()) }
    ) { animatedContentState ->
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = alignment
        ) {
            content(animatedContentState)
        }
    }
}