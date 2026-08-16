package com.a.agent.presentation.llm.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.presentation.llm.LlmAction
import com.a.agent.presentation.llm.LlmState
import com.a.agent.presentation.llm.pagerContentPadding
import com.a.agent.presentation.llm.pagerItemModifier
import com.a.agent.presentation.llm.pagerModifier
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.HeadlineText
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText

@Composable
fun AllLlmPager(
    navBackStack: NavBackStack<NavKey>,
    state: LlmState,
    onAction: (LlmAction) -> Unit
) {
    LazyColumn(
        modifier = pagerModifier,
        contentPadding = pagerContentPadding,
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        when {
            state.isAllLlmLoading -> {
                item(key = "isAllLlmLoading") {
                    LinearWavyProgressIndicator(
                        modifier = pagerItemModifier
                    )
                }
            }
            state.isAllLlmError != null -> {
                item(key = "isAllLlmError") {
                    Message(
                        modifier = pagerItemModifier,
                        message = state.isAllLlmError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.allLlm.isEmpty() -> {
                item(key = "isAllLlmEmpty") {
                    Message(
                        modifier = pagerItemModifier,
                        message = "Model Empty",
                        messageType = MessageType.Info
                    )
                }
            }
            else -> {
                itemsIndexed(
                    items = state.allLlm,
                    key = { index, modelEntity -> modelEntity.id }
                ) { index, modelEntity ->
                    val downloadInfo = state.downloadState[modelEntity.id]
                    CustomSegmentedListItem(
                        modifier = pagerItemModifier,
                        onClick = { navBackStack.add(NavigationRoute.LlmManagerScreen(modelEntity.id)) },
                        index = index,
                        count = state.allLlm.size,
                        content = {
                            HeadlineText(
                                text = modelEntity.name,
                                isSingleLine = true,
                                textDecoration = if (modelEntity.llmSource == LlmSource.Local && !modelEntity.isDownloaded) {
                                    TextDecoration.LineThrough
                                } else null
                            )
                        },
                        supportingContent = {
                            SupportingText(
                                text = modelEntity.fileName,
                                isSingleLine = true
                            )
                        },
                        trailingContent = {
                            if (modelEntity.llmSource != LlmSource.Local && !modelEntity.isDownloaded) {
                                DownloadProgressIndicator(
                                    downloadInfo = downloadInfo?.second,
                                    llmEntity = modelEntity,
                                    onAction = onAction
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}