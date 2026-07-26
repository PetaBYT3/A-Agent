@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.presentation.model.component.AllModelPager
import com.a.agent.presentation.model.component.DefaultModelPager
import com.a.agent.presentation.model.component.LocalModelPager
import com.a.agent.presentation.model.component.ModelFilter
import com.a.agent.presentation.model.component.UrlModelPager
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomTopAppBar
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModelScreen(
    navBackStack: NavBackStack<NavKey>,
    viewModel: ModelViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    Screen(
        navBackStack = navBackStack,
        state = state,
        onAction = onAction
    )
}

@Composable
private fun Screen(
    navBackStack: NavBackStack<NavKey>,
    state: ModelState,
    onAction: (ModelAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "Models",
            )
        },
        content = { innerPadding ->
            Content(
                navBackStack = navBackStack,
                innerPadding = innerPadding,
                state = state,
                onAction = onAction
            )
        }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: ModelState,
    onAction: (ModelAction) -> Unit
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
                pageCount = { ModelFilter.entries.size },
                initialPage = 0
            )
            Row(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
                    .height(AssistChipDefaults.Height),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 5.dp),
                    text = "Filter Llm",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                AssistChip(
                    onClick = { navBackStack.add(NavigationRoute.ModelManagerScreen()) },
                    label = { Text(text = "Add Llm") }
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = ModelFilter.entries
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
                val currentFilter = ModelFilter.entries[pagerIndex]
                when (currentFilter) {
                    ModelFilter.All -> {
                        AllModelPager(
                            navBackStack = navBackStack,
                            state = state,
                            onAction = onAction
                        )
                    }
                    ModelFilter.Default -> {
                        DefaultModelPager(
                            navBackStack = navBackStack,
                            state = state,
                            onAction = onAction
                        )
                    }
                    ModelFilter.Url -> {
                        UrlModelPager(
                            navBackStack = navBackStack,
                            state = state,
                            onAction = onAction
                        )
                    }
                    ModelFilter.Local -> {
                        LocalModelPager(
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
        state = ModelState(
            downloadState = mutableMapOf("1" to DownloadInfo(0, 0, 0f, 0))
        ),
        onAction = {}
    )
}