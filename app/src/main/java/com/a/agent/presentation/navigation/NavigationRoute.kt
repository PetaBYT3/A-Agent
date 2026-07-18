package com.a.agent.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationRoute: NavKey {
    @Serializable
    data object HomeScreen: NavigationRoute, NavKey

    @Serializable
    data object WorkflowScreen: NavigationRoute, NavKey

    @Serializable
    data object WorkflowManagerScreen: NavigationRoute, NavKey

    @Serializable
    data object ModelScreen: NavigationRoute, NavKey

    @Serializable
    data class ModelManagerScreen(val modelId: String = ""): NavigationRoute, NavKey

    @Serializable
    data class ConversationScreen(val conversationId: String): NavigationRoute, NavKey
}