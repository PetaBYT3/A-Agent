package com.a.agent.presentation.navigation

sealed interface BackStack {
    data object PopBackStack: BackStack
    data class Replace(val route: NavigationRoute): BackStack
    data class Navigate(val route: NavigationRoute): BackStack
}