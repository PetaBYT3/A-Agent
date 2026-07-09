package com.a.agent.presentation.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.repository.ModelRepository
import com.a.agent.domain.usecase.ModelUseCases
import com.a.agent.presentation.navigation.Event
import com.a.agent.presentation.navigation.NavigationDisplayEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModelViewModel(
    private val modelRepository: ModelRepository,
    private val modelUseCases: ModelUseCases,
    private val navigationDisplayEvent: NavigationDisplayEvent
): ViewModel() {
    private val _state = MutableStateFlow(ModelState())
    val state = _state.onStart {
        initialize()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModelState())

    private fun initialize() = viewModelScope.launch {
        coroutineScope {
            launch {
                viewModelScope.launch {
                    modelRepository.downloadState.collect { map ->
                        Log.d("DownloadDebugging", "Map: $map")
                        _state.update { it.copy(downloadState = map) }
                    }
                }
            }
            launch {
                modelUseCases.getModels().collect { either ->
                    either.onRight { modelEntities ->
                        _state.update { it.copy(modelEntities = modelEntities) }
                    }
                }
            }
        }
    }

    fun onAction(action: ModelAction) {
        when (action) {
            is ModelAction.ToggleDownload -> toggleDownload(action.modelEntity)
        }
    }

    private fun toggleDownload(modelEntity: ModelEntity) = viewModelScope.launch {
        modelUseCases.toggleDownload(
            modelEntity = modelEntity
        ).onLeft { e ->
            navigationDisplayEvent.sendEvent(Event.ShowSnackBar(e))
        }
    }
}