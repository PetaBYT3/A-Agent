package com.a.agent.presentation.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class NavigationDisplayBackStack {
    private val _backStack = Channel<BackStack>()
    val event = _backStack.receiveAsFlow()

    suspend fun sendEvent(backStack: BackStack) {
        _backStack.send(backStack)
    }
}