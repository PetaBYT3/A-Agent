package com.a.agent.presentation.conversationmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.CustomSnackBar
import com.a.agent.presentation.util.component.CustomContentBottomSheet
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ConversationManagerScreen(
    navBackStack: NavBackStack<NavKey>,
    conversationId: String,
    viewModel: ConversationManagerViewModel = koinViewModel {
        parametersOf(conversationId)
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction
    val snackBarHostState = remember { SnackbarHostState() }

    Screen(
        navBackStack = navBackStack,
        snackBarHostState = snackBarHostState,
        state = state,
        onAction = onAction
    )

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }
}

@Composable
private fun Screen(
    navBackStack: NavBackStack<NavKey>,
    snackBarHostState: SnackbarHostState,
    state: ConversationManagerState,
    onAction: (ConversationManagerAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "Conversation Manager"
            )
        },
        content = { innerPadding ->
            Content(
                navBackStack = navBackStack,
                innerPadding = innerPadding,
                state = state,
                onAction = onAction
            )
        },
        snackbarHost = { CustomSnackBar(snackBarHostState = snackBarHostState) },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (state.isOnEdit) {
                    Button(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .height(ButtonDefaults.MediumContainerHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        onClick = { onAction(ConversationManagerAction.DeleteConversationBottomSheet) },
                        content = { Icon(Icons.Rounded.Delete, null) }
                    )
                }
                Button(
                    modifier = Modifier
                        .height(ButtonDefaults.MediumContainerHeight),
                    onClick = { onAction(ConversationManagerAction.UpsertConversationButton) },
                    content = { Icon(Icons.Rounded.Save, null) },
                    enabled = state.conversationTitleTextField.isNotBlank()
                )
            }
        }
    )

    CustomContentBottomSheet(
        isBottomSheetVisible = state.isDeleteConversationBottomSheetVisible,
        title = "Delete",
        content = {
            item {
                SupportingText(
                    text = "Are you sure you want to delete this conversation ?"
                )
            }
        },
        isOnError = true,
        confirmText = "Yes",
        onConfirm = { onAction(ConversationManagerAction.DeleteConversationButton) },
        onCancel = { onAction(ConversationManagerAction.DeleteConversationBottomSheet) }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: ConversationManagerState,
    onAction: (ConversationManagerAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        when {
            state.isConversationLoading -> {
                item(key = "isConversationLoading") {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
            state.isConversationError != null -> {
                item(key = "isConversationError") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = state.isConversationError,
                        messageType = MessageType.Error
                    )
                }
            }
            else -> {
                item(key = "conversationData") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        CustomTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            label = { Text(text = "Title") },
                            value = state.conversationTitleTextField,
                            onValueChange = { onAction(ConversationManagerAction.ConversationTitleTextField(it)) }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        snackBarHostState = remember { SnackbarHostState() },
        state = ConversationManagerState(
            isConversationLoading = false
        ),
        onAction = {}
    )
}