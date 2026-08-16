@file:OptIn(ExperimentalMaterial3Api::class)

package com.a.agent.presentation.util.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun CustomUndismissableBottomSheet(
    isBottomSheetVisible: Boolean,
    title: String,
    content: LazyListScope.() -> Unit,
) {
    var isVisibleInternally by remember { mutableStateOf(isBottomSheetVisible) }
    var allowHide by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            if (allowHide) true else it != SheetValue.Hidden
        }
    )

    LaunchedEffect(isBottomSheetVisible) {
        if (isBottomSheetVisible) {
            allowHide = false
            isVisibleInternally = true
        } else {
            if (isVisibleInternally) {
                allowHide = true
                sheetState.hide()
                isVisibleInternally = false
            }
        }
    }

    if (isVisibleInternally) {
        ModalBottomSheet(
            modifier = Modifier
                .statusBarsPadding(),
            sheetState = sheetState,
            properties = ModalBottomSheetDefaults.properties(
                shouldDismissOnBackPress = false
            ),
            onDismissRequest = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleText(
                    modifier = Modifier
                        .padding(16.dp),
                    text = title,
                    isSingleLine = true
                )
                LazyColumn(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun CustomEmptyBottomSheet(
    isBottomSheetVisible: Boolean,
    title: String,
    content: LazyListScope.() -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isBottomSheetVisible) {
        ModalBottomSheet(
            modifier = Modifier
                .statusBarsPadding(),
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onCancel()
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleText(
                    modifier = Modifier
                        .padding(16.dp),
                    text = title,
                    isSingleLine = true
                )
                LazyColumn(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun CustomContentBottomSheet(
    isBottomSheetVisible: Boolean,
    title: String,
    content: LazyListScope.() -> Unit,
    isOnError: Boolean = false,
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isBottomSheetVisible) {
        ModalBottomSheet(
            modifier = Modifier
                .statusBarsPadding(),
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onCancel()
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleText(
                    modifier = Modifier
                        .padding(16.dp),
                    text = title,
                    isSingleLine = true
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, false),
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    content(this)
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        colors = if (isOnError) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onCancel()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onConfirm()
                                }
                            }
                        },
                        content = { Text(text = confirmText) }
                    )
                }
            }
        }
    }
}