package com.a.agent.presentation.navigation

sealed interface Event {
    data class ShowSnackBar(val message: String): Event
    data object PopBackStack: Event
    data class Replace(val route: NavigationRoute): Event
    data class Navigate(val route: NavigationRoute): Event
}