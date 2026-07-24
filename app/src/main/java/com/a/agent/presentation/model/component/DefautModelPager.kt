package com.a.agent.presentation.model.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.a.agent.presentation.model.ModelAction
import com.a.agent.presentation.model.ModelState
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.HeadlineText
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText

@Composable
fun DefaultModelPager(
    navBackStack: NavBackStack<NavKey>,
    state: ModelState,
    onAction: (ModelAction) -> Unit
) {
    LazyColumn(
        modifier = ModelPagerModifier,
        contentPadding = ModelLazyColumnPadding,
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        when {
            state.isDefaultModelsLoading -> {
                item(key = "isDefaultModelsLoading") {
                    LinearWavyProgressIndicator(
                        modifier = modelPagerMessagePadding()
                    )
                }
            }
            state.isDefaultModelsError != null -> {
                item(key = "isDefaultModelsError") {
                    Message(
                        modifier = modelPagerMessagePadding(),
                        message = state.isDefaultModelsError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.defaultModels.isEmpty() -> {
                item(key = "isDefaultModelsEmpty") {
                    Message(
                        modifier = modelPagerMessagePadding(),
                        message = "Model Empty",
                        messageType = MessageType.Info
                    )
                }
            }
        }
        itemsIndexed(
            items = state.defaultModels,
            key = { index, modelEntity -> modelEntity.id }
        ) { index, modelEntity ->
            val downloadInfo = state.downloadState[modelEntity.id]
            CustomSegmentedListItem(
                modifier = Modifier
                    .animateItem(),
                onClick = { navBackStack.add(NavigationRoute.ModelManagerScreen(modelEntity.id)) },
                index = index,
                count = state.defaultModels.size,
                content = {
                    HeadlineText(
                        text = modelEntity.name,
                        isSingleLine = true
                    )
                },
                supportingContent = {
                    SupportingText(
                        text = modelEntity.fileName,
                        isSingleLine = true
                    )
                },
                trailingContent = {
                    if (!modelEntity.isDownloaded) {
                        DownloadProgressIndicator(
                            downloadInfo = downloadInfo,
                            modelEntity = modelEntity,
                            onAction = onAction
                        )
                    }
                }
            )
        }
    }
}