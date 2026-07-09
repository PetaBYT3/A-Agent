package com.a.agent.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.a.agent.presentation.navigation.NavigationDisplay
import com.a.agent.ui.theme.AAgentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AAgentTheme {
                NavigationDisplay()
            }
        }
    }
}