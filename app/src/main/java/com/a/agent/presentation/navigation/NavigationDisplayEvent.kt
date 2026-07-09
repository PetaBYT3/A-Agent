package com.a.agent.presentation.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class NavigationDisplayEvent {
    private val _event = Channel<Event>()
    val event = _event.receiveAsFlow()

    suspend fun sendEvent(event: Event) {
        _event.send(event)
    }
}