package com.a.agent.presentation.navigation

sealed interface Effect {
    data class ShowSnackBar(val message: String): Effect
}