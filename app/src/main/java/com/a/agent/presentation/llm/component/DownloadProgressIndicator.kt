package com.a.agent.presentation.llm.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.data.util.toMegaByte
import com.a.agent.presentation.llm.LlmAction
import com.a.agent.presentation.util.component.CustomFadeAnimatedVisibility
import com.a.agent.presentation.util.component.CustomShrinkLeftAnimatedVisibility
import com.a.agent.presentation.util.component.SupportingText

@Composable
fun DownloadProgressIndicator(
    modifier: Modifier = Modifier,
    downloadInfo: DownloadInfo?,
    llmEntity: LlmEntity,
    onAction: (LlmAction) -> Unit
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CustomShrinkLeftAnimatedVisibility(
            visible = downloadInfo != null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SupportingText(
                    text = "${downloadInfo?.totalBytes?.toMegaByte() ?: "0MB"}/${downloadInfo?.downloadedBytes?.toMegaByte() ?: "0MB"}"
                )
                SupportingText(
                    text = "${downloadInfo?.percentage ?: "0"}%"
                )
            }
        }
        Box(
            modifier = Modifier
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress by animateFloatAsState(
                targetValue = downloadInfo?.progress ?: 0f
            )
            CustomFadeAnimatedVisibility(
                visible = downloadInfo != null
            ) {
                CircularWavyProgressIndicator(
                    progress = { progress }
                )
            }
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (downloadInfo != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalContentColor.current
                    }
                ),
                onClick = { onAction(LlmAction.ToggleDownload(llmEntity)) },
                content = {
                    Icon(
                        imageVector = if (downloadInfo != null) {
                            Icons.Rounded.Close
                        } else {
                            Icons.Rounded.Download
                        },
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DownloadProgressIndicator(
        downloadInfo = DownloadInfo(
            totalBytes = 0,
            downloadedBytes = 0,
            progress = 0.5f,
            percentage = 54
        ),
        llmEntity = LlmEntity.Empty,
        onAction = {}
    )
}