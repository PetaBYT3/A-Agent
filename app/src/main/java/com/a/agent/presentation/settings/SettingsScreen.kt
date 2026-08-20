package com.a.agent.presentation.settings

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.R
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.settings.component.KeepScreenOn
import com.a.agent.presentation.util.CustomSnackBar
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.CustomUndismissableBottomSheet
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.spacer
import com.a.agent.presentation.util.restartApplication
import com.a.agent.util.Quadruple
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    navBackStack: NavBackStack<NavKey>,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction
    val snackBarHostState = remember { SnackbarHostState() }

    Screen(
        navBackStack = navBackStack,
        snackBarHostState = snackBarHostState,
        state = state,
        onAction = onAction
    )

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }
}

@Composable
private fun Screen(
    navBackStack: NavBackStack<NavKey>,
    snackBarHostState: SnackbarHostState,
    state: SettingsState,
    onAction: (SettingsAction) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "Settings"
            )
        },
        snackbarHost = { CustomSnackBar(snackBarHostState = snackBarHostState) },
        content = { innerPadding ->
            Content(
                navBackStack = navBackStack,
                innerPadding = innerPadding,
                state = state,
                onAction = onAction
            )
        }
    )

    CustomUndismissableBottomSheet(
        isBottomSheetVisible = state.isExportBackupBottomSheetVisible,
        title = "Export Backup",
        content = {
            item(key = "exportBackupProcess") {
                SupportingText(
                    text = state.exportBackupFile ?: "Complete",
                    isSingleLine = true
                )
            }
            spacer()
            item {
                val progress by animateFloatAsState(
                    targetValue = state.exportBackupProgress
                )
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    progress = { progress }
                )
            }
            spacer()
            item(key = "doneButton") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var isKeepScreenOn by remember {
                        mutableStateOf(false)
                    }
                    KeepScreenOn(isKeepScreenOn)
                    Switch(
                        checked = isKeepScreenOn,
                        onCheckedChange = { isKeepScreenOn = it }
                    )
                    SupportingText(text = "Keep Screen On")
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onAction(SettingsAction.ExportBackupDoneButton) },
                        content = { Text(text = "Done") },
                        enabled = state.isExportBackupComplete
                    )
                }
            }
        }
    )

    CustomUndismissableBottomSheet(
        isBottomSheetVisible = state.isImportBackupBottomSheetVisible,
        title = "Import Backup",
        content = {
            item(key = "importBackupProcess") {
                SupportingText(
                    text = state.importBackupFile ?: "Complete",
                    isSingleLine = true
                )
            }
            spacer()
            item("importBackupProgress") {
                val progress by animateFloatAsState(
                    targetValue = state.importBackupProgress
                )
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    progress = { progress }
                )
            }
            spacer()
            item(key = "restartAppButton") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var isKeepScreenOn by remember {
                        mutableStateOf(false)
                    }
                    KeepScreenOn(isKeepScreenOn)
                    Switch(
                        checked = isKeepScreenOn,
                        onCheckedChange = { isKeepScreenOn = it }
                    )
                    SupportingText(text = "Keep Screen On")
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { restartApplication(context) },
                        content = { Text(text = "Restart App") },
                        enabled = state.isImportBackupComplete
                    )
                }
            }
        }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: SettingsState,
    onAction: (SettingsAction) -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val fileSaverLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings(),
        onResult = { platformFile ->
            if (platformFile != null) {
                onAction(SettingsAction.ExportBackupButton(platformFile))
            }
        }
    )
    val zipFilePicker = rememberFilePickerLauncher(
        type = FileKitType.File(".zip"),
        onResult = { platformFile ->
            if (platformFile != null) {
                onAction(SettingsAction.ImportBackupButton(platformFile))
            }
        }
    )

    val linkedinImageVector = ImageVector.vectorResource(R.drawable.linkedin)
    val githubImageVector = ImageVector.vectorResource(R.drawable.github)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        listTitle("Backup Options")
        val backupOptions = listOf(
            Quadruple(
                first = { fileSaverLauncher.launch("A Agent Backup", "zip") },
                second = Icons.Rounded.Backup,
                third = "Export Backup",
                fourth = R.string.backup_export,
            ),
            Quadruple(
                first = { zipFilePicker.launch() },
                second = Icons.Rounded.Restore,
                third = "Import Backup",
                fourth = R.string.backup_import
            )
        )
        itemsIndexed(
            items = backupOptions,
            key = { index, quadruple -> quadruple.third }
        ) { index, quadruple ->
            CustomSegmentedListItem(
                modifier = Modifier
                    .animateItem(),
                index = index,
                count = backupOptions.size,
                onClick = { quadruple.first() },
                leadingContent = { Icon(quadruple.second, null) },
                content = { Text(text = quadruple.third) },
                supportingContent = { Text(text = stringResource(quadruple.fourth)) }
            )
        }
        spacer()
        listTitle("Contact Developer")
        val contacts = listOf(
            Quadruple(
                first = {
                    try {
                        val email = "andreahussanini.2103@gmail.com"
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:$email".toUri()
                        }
                        context.startActivity(emailIntent)
                    } catch (e: Exception) {
                        onAction(SettingsAction.ShowSnackBar("No App Can Handle This Action"))
                    }
                },
                second = Icons.Rounded.Email,
                third = "Email",
                fourth = "andreahussanini.2103@gmail.com"
            ),
            Quadruple(
                first = {
                    val link = "https://www.linkedin.com/in/andrea-hussanini-274223218/"
                    uriHandler.openUri(link)
                },
                second = linkedinImageVector,
                third = "Linkedin",
                fourth = "Andrea Hussanini"
            ),
            Quadruple(
                first = {
                    val link = "https://github.com/PetaBYT3"
                    uriHandler.openUri(link)
                },
                second = githubImageVector,
                third = "GitHub",
                fourth = "PetaBYT3"
            )
        )
        itemsIndexed(
            items = contacts,
            key = { index, quadruple -> quadruple.third }
        ) { index, quadruple ->
            CustomSegmentedListItem(
                modifier = Modifier
                    .animateItem(),
                index = index,
                count = contacts.size,
                onClick = { quadruple.first() },
                leadingContent = {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        imageVector = quadruple.second,
                        contentDescription = null
                    )
                },
                content = { Text(text = quadruple.third) },
                supportingContent = { Text(text = quadruple.fourth) }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        snackBarHostState = remember { SnackbarHostState() },
        state = SettingsState(
            isExportBackupBottomSheetVisible = false
        ),
        onAction = {}
    )
}