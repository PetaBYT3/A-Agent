package com.a.agent.presentation.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.a.agent.presentation.navigation.NavigationRoute

data class HomeMenuItem(
    val route: NavigationRoute,
    val icon: ImageVector,
    val content: String,
    val supportingContent: String? = null,
) {
    companion object {
        val HomeMenuItemList = listOf(
            HomeMenuItem(
                route = NavigationRoute.WorkflowScreen,
                icon = Icons.Rounded.Work,
                content = "Workflow",
            ),
            HomeMenuItem(
                route = NavigationRoute.ModelScreen,
                icon = Icons.Rounded.Memory,
                content = "Models",
            )
        )
    }
}
