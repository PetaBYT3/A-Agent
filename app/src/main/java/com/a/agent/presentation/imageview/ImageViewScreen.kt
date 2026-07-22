package com.a.agent.presentation.imageview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import coil.compose.AsyncImage
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomFadeBox
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ImageViewScreen(
    navBackStack: NavBackStack<NavKey>,
    imagePath: String
) {
    Screen(
        navBackStack = navBackStack,
        imagePath = imagePath
    )
}

@Composable
private fun Screen(
    navBackStack: NavBackStack<NavKey>,
    imagePath: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(horizontal = 7.dp),
                navigationIcon = {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        onClick = { navBackStack.popBackStack() },
                        content = { Icon(Icons.Rounded.ArrowBack, null) }
                    )
                },
                title = {}
            )
        },
        content = { innerPadding ->
            CustomFadeBox(
                fadeTop = innerPadding.calculateTopPadding(),
                fadeBottom = innerPadding.calculateBottomPadding()
            ) {
                Content(
                    innerPadding = innerPadding,
                    imagePath = imagePath
                )
            }
        }
    )
}

@Composable
private fun Content(
    innerPadding: PaddingValues,
    imagePath: String
) {
    ZoomableAsyncImage(
        modifier = Modifier
            .fillMaxSize(),
        model = imagePath,
        contentDescription = null,
    )
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        imagePath = ""
    )
}