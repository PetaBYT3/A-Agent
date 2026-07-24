package com.a.agent.presentation.model.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.presentation.util.component.CustomShrinkDownAnimatedVisibility
import com.a.agent.presentation.util.toMegaByte

@Composable
fun CustomLinearProgressIndicator(
    modifier: Modifier = Modifier,
    downloadInfo: DownloadInfo?
) {
    CustomShrinkDownAnimatedVisibility(
        visible = downloadInfo != null
    ) {
        Column(
            modifier = modifier
        ) {
            Row {
                Text(text = "${downloadInfo?.downloadedBytes?.toMegaByte() ?: 0}/${downloadInfo?.totalBytes?.toMegaByte() ?: 0}")
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${downloadInfo?.percentage ?: 0}%")
            }
            val progress by animateFloatAsState(
                targetValue = downloadInfo?.progress ?: 0f
            )
            LinearWavyProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(),
                progress = { progress }
            )
        }
    }
}