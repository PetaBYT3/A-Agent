package com.a.agent.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.a.agent.domain.repository.EngineRepository
import com.a.agent.domain.repository.PermissionRepository
import com.a.agent.presentation.navigation.NavigationDisplay
import com.a.agent.ui.theme.AAgentTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val permissionRepository: PermissionRepository by inject()
    private val engineRepository: EngineRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionRepository.updatePermission()
        setContent {
            AAgentTheme {
                NavigationDisplay()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionRepository.updatePermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        engineRepository.destroyEngine()
        engineRepository.destroyConversation()
    }
}