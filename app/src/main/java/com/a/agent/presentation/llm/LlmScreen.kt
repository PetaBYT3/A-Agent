@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.llm

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.R
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.presentation.llm.component.AllLlmPager
import com.a.agent.presentation.llm.component.DefaultLlmPager
import com.a.agent.presentation.llm.component.LlmFilter
import com.a.agent.presentation.llm.component.LocalLlmPager
import com.a.agent.presentation.llm.component.UrlLlmPager
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.CustomSnackBar
import com.a.agent.presentation.util.component.CustomContentBottomSheet
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomPopupMenu
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.spacer
import com.a.agent.presentation.util.openApplicationSettings
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelScreen(
    navBackStack: NavBackStack<NavKey>,
    viewModel: LlmViewModel = koinViewModel()
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
    state: LlmState,
    onAction: (LlmAction) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "LLM",
                action = {
                    CustomPopupMenu(
                        content = { expanded ->
                            IconButton(
                                onClick = { expanded() },
                                content = { Icon(Icons.Rounded.MoreVert, null) }
                            )
                        },
                        items = listOf(
                            Triple(
                                first = { onAction(LlmAction.DeleteOrphanFileButton) },
                                second = Icons.Rounded.Delete,
                                third = "Delete Orphan Files"
                            ),
                            Triple(
                                first = { onAction(LlmAction.AuthorizationKeyBottomSheet) },
                                second = Icons.Rounded.Key,
                                third = "Authorization Key"
                            )
                        )
                    )
                }
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
        floatingActionButton = {
            CustomFloatingActionButton(
                onClick = { navBackStack.add(NavigationRoute.LlmManagerScreen()) },
                icon = Icons.Rounded.Add
            )
        }
    )

    CustomContentBottomSheet(
        isBottomSheetVisible = state.isNotificationPermissionDeniedBottomSheetVisible,
        title = "Notification Access Denied",
        content = {
            item {
                SupportingText(
                    text = "Notification permission is denied, to start download please allow notification permission on app permission settings",
                    isSingleLine = false
                )
            }
        },
        confirmText = "Open Settings",
        onConfirm = { openApplicationSettings(context) },
        onCancel = { onAction(LlmAction.NotificationPermissionDeniedBottomSheet) }
    )

    CustomContentBottomSheet(
        isBottomSheetVisible = state.isAuthorizationKeyBottomSheetVisible,
        title = "Authorization Key",
        content = {
            item {
                CustomSegmentedListItem(
                    content = { Text(text = "Enable Default Key") },
                    supportingContent = { Text(text = stringResource(R.string.enable_authorization_key)) },
                    trailingContent = {
                        Switch(
                            checked = state.isEnableDefaultAuthorizationKey,
                            onCheckedChange = {
                                onAction(
                                    LlmAction.EnableDefaultAuthorizationKeySwitch(
                                        it
                                    )
                                )
                            }
                        )
                    }
                )
            }
            spacer()
            listTitle("Your Key")
            item {
                CustomTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text(text = "HuggingFace Authorization Key") },
                    value = state.authorizationKeyTextField,
                    onValueChange = { onAction(LlmAction.AuthorizationKeyTextField(it)) },
                    enabled = !state.isEnableDefaultAuthorizationKey
                )
            }
        },
        confirmText = "Save Your Key",
        onConfirm = { onAction(LlmAction.ButtonSaveAuthorizationKey) },
        onCancel = { onAction(LlmAction.AuthorizationKeyBottomSheet) },
    )
}

val pagerModifier = Modifier.fillMaxSize()
val pagerContentPadding = PaddingValues(horizontal = 16.dp)
@get:SuppressLint("ModifierFactoryExtensionFunction")
val LazyItemScope.pagerItemModifier: Modifier get() = Modifier.fillMaxWidth().animateItem()

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: LlmState,
    onAction: (LlmAction) -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val pagerState = rememberPagerState(
                pageCount = { LlmFilter.entries.size },
                initialPage = 0
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "Filter",
                style = MaterialTheme.typography.labelLarge
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.5.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = LlmFilter.entries
                ) { index, modelFilter ->
                    FilterChip(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        label = { Text(modelFilter.name) }
                    )
                }
            }
            HorizontalPager(
                modifier = Modifier
                    .weight(1f),
                state = pagerState
            ) { pagerIndex ->
                val currentFilter = LlmFilter.entries[pagerIndex]
                when (currentFilter) {
                    LlmFilter.All -> {
                        AllLlmPager(
                            navBackStack = navBackStack,
                            state = state,
                            onAction = onAction
                        )
                    }
                    LlmFilter.Default -> {
                        DefaultLlmPager(
                            navBackStack = navBackStack,
                            state = state,
                            onAction = onAction
                        )
                    }
                    LlmFilter.Url -> {
                        UrlLlmPager(
                            navBackStack = navBackStack,
                            state = state,
                            onAction = onAction
                        )
                    }
                    LlmFilter.Local -> {
                        LocalLlmPager(
                            navBackStack = navBackStack,
                            state = state,
                            onAction = onAction
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
        state = LlmState(
            isAuthorizationKeyBottomSheetVisible = true,
            downloadState = mutableMapOf("1" to Pair("Filename", DownloadInfo(0, 0, 0.50f, 0))),
            isAllLlmLoading = false,
            allLlm = listOf(
                LlmEntity(
                    id = "1",
                    name = "Preview",
                    url = "",
                    path = "",
                    fileName = "preview.ltiertlm",
                    totalBytes = 1293912,
                    llmSource = LlmSource.Url,
                    isDownloaded = false
                )
            )
        ),
        onAction = {}
    )
}