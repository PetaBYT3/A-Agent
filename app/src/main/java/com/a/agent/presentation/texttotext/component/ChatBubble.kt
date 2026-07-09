package com.a.agent.presentation.texttotext.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a.agent.presentation.util.component.SupportingText

@Composable
fun ChatBubble(
    modifier: Modifier = Modifier,
    fromUser: Boolean = true,
    message: String
) {
    val alignment = if (fromUser) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }
    val shape = if (fromUser) {
        AbsoluteRoundedCornerShape(topLeft = 20.dp, topRight = 5.dp, bottomLeft = 20.dp, bottomRight = 20.dp)
    } else {
        AbsoluteRoundedCornerShape(topLeft = 5.dp, topRight = 20.dp, bottomLeft = 20.dp, bottomRight = 20.dp)
    }
    val colors = if (fromUser) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .align(alignment)
                .widthIn(max = maxWidth * 0.8f),
            shape = shape,
            colors = colors
        ) {
            SupportingText(
                modifier = Modifier
                    .padding(15.dp),
                text = message,
                isSingleLine = false
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ChatBubble(
        fromUser = false,
        message = "Satefakos"
    )
}