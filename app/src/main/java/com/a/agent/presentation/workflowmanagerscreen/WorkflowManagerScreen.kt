package com.a.agent.presentation.workflowmanagerscreen

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomFadeBox
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTopAppBar
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun WorkflowManagerScreen(
    navBackStack: NavBackStack<NavKey>
) {
    Screen(
        navBackStack = navBackStack
    )
}

@Composable
private fun Screen(
    navBackStack: NavBackStack<NavKey>
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "Workflow",
            )
        },
        content = { innerPadding ->
            Content(
                navBackStack = navBackStack,
                innerPadding = innerPadding
            )
        }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues
) {
    var models by remember {
        mutableStateOf(
            listOf("Gemma", "Qwen", "Llama", "Grok")
        )
    }
    var currentDraggingIndex by remember {
        mutableStateOf<Int?>(null)
    }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        models = models.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    CustomFadeBox(

    ) {

    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = lazyListState,
        contentPadding = innerPadding + PaddingValues(start = 15.dp, end = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        itemsIndexed(
            items = models,
            key = { index, model -> model }
        ) { index, model ->
            ReorderableItem(
                state = reorderableLazyListState,
                key = model
            ) {
                CustomSegmentedListItem(
                    index = index,
                    count = models.size,
                    content = { Text(text = model) },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(8.dp),
                                imageVector = Icons.Rounded.ArrowDownward,
                                contentDescription = null
                            )
                            Icon(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .draggableHandle(),
                                imageVector = Icons.Rounded.DragIndicator,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack()
    )
}