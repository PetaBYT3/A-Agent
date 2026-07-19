@file:OptIn(ExperimentalMaterial3Api::class)

package com.a.agent.presentation.util.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun CustomEmptyBottomSheet(
    isBottomSheetVisible: Boolean,
    content: LazyListScope.() -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isBottomSheetVisible) {
        ModalBottomSheet(
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
            LazyColumn(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(2.5.dp),
                contentPadding = PaddingValues(15.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun CustomComposableBottomSheet(
    isBottomSheetVisible: Boolean,
    content: LazyListScope.() -> Unit,
    additionalContent: @Composable () -> Unit = {},
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isBottomSheetVisible) {
        ModalBottomSheet(
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
                modifier = Modifier.fillMaxWidth().padding(top = 15.dp, bottom = 15.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, false),
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    contentPadding = PaddingValues(start = 15.dp, end = 15.dp)
                ) {
                    content(this)
                }
                Spacer(modifier = Modifier.height(15.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    additionalContent()
                    Button(
                        modifier = Modifier.fillMaxWidth(),
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

data class SegmentedItemData(
    val onClick: () -> Unit,
    val leadingContent: (@Composable () -> Unit)? = null,
    val content: @Composable () -> Unit,
    val supportingContent: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null
)

@Composable
fun <T> SelectItemBottomSheet(
    isBottomSheetVisible: Boolean,
    itemList: List<T>,
    additionalContent: (@Composable () -> Unit)? = null,
    itemContent: @Composable (onDismiss: () -> Unit, index: Int, item: T) -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val closeSheet: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onCancel()
            }
        }
    }

    if (isBottomSheetVisible) {
        ModalBottomSheet(
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, false),
                verticalArrangement = Arrangement.spacedBy(2.5.dp),
                contentPadding = PaddingValues(start = 15.dp, end = 15.dp, top = 15.dp, bottom = 15.dp)
            ) {
                if (additionalContent != null) {
                    item { additionalContent() }
                }
                itemsIndexed(itemList) { index, item ->
                    itemContent(closeSheet, index, item)
                }
            }
        }
    }
}