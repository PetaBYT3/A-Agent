package com.a.agent.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.presentation.home.component.HomeMenuItem
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomSurfaceIconButton
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.HeadlineText
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.TitleText
import com.a.agent.presentation.util.component.spacer

@Composable
fun HomeScreen(
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
                contentPadding = PaddingValues(end = 7.dp),
                title = "Home",
                action = {
                    CustomSurfaceIconButton(
                        onClick = {},
                        icon = Icons.Rounded.Person
                    )
                }
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(start = 15.dp, end = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        item {
            CustomSegmentedListItem(
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    leadingContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    supportingContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Icon(Icons.Rounded.Info, null)
                    }
                },
                content = {
                    Column {
                        HeadlineText(text = "Available Workflow")
                        SupportingText(text = 3.toString())
                        Spacer(modifier = Modifier.height(10.dp))
                        HeadlineText(text = "Available Model")
                        SupportingText(text = 3.toString())
                    }
                }
            )
        }
        spacer()
        itemsIndexed(
            items = HomeMenuItem.HomeMenuItemList
        ) { index, homeMenuItem ->
            CustomSegmentedListItem(
                onClick = { navBackStack.add(homeMenuItem.route) },
                index = index,
                count = HomeMenuItem.HomeMenuItemList.size,
                leadingContent = { Icon(homeMenuItem.icon, null) },
                content = { Text(text = homeMenuItem.content) }
            )
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