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
fun LocalLlmPager(
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
            state.isLocalLlmLoading -> {
                item(key = "isLocalLlmLoading") {
                    LinearWavyProgressIndicator(
                        modifier = pagerItemModifier
                    )
                }
            }
            state.isLocalLlmError != null -> {
                item(key = "isLocalLlmError") {
                    Message(
                        modifier = pagerItemModifier,
                        message = state.isLocalLlmError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.localLlm.isEmpty() -> {
                item(key = "isLocalLlmEmpty") {
                    Message(
                        modifier = pagerItemModifier,
                        message = "Model Empty",
                        messageType = MessageType.Info
                    )
                }
            }
            else -> {
                itemsIndexed(
                    items = state.localLlm,
                    key = { index, modelEntity -> modelEntity.id }
                ) { index, modelEntity ->
                    CustomSegmentedListItem(
                        modifier = pagerItemModifier,
                        onClick = { navBackStack.add(NavigationRoute.LlmManagerScreen(modelEntity.id)) },
                        index = index,
                        count = state.localLlm.size,
                        content = {
                            HeadlineText(
                                text = modelEntity.name,
                                isSingleLine = true,
                                textDecoration = if (!modelEntity.isDownloaded) TextDecoration.LineThrough else null
                            )
                        },
                        supportingContent = {
                            SupportingText(
                                text = modelEntity.fileName,
                                isSingleLine = true
                            )
                        }
                    )
                }
            }
        }
    }
}