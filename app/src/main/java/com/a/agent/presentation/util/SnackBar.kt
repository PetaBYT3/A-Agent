package com.a.agent.presentation.util

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomSnackBar(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState
) {
    SnackbarHost(
        modifier = modifier,
        hostState = snackBarHostState,
    )
}