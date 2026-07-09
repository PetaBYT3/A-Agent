package com.a.agent.presentation.workflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.spacer

@Composable
fun WorkFlowScreen(
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navBackStack.add(NavigationRoute.WorkflowManagerScreen) },
                content = { Icon(Icons.Rounded.Add, null) }
            )
        }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(start = 15.dp, end = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack()
    )
}