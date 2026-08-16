package com.a.agent.presentation.util.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    isSingleLine: Boolean = true,
    textAlign: TextAlign? = null
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = MaterialTheme.typography.titleLarge,
        overflow = TextOverflow.Ellipsis,
        maxLines = if (isSingleLine) 1 else Int.MAX_VALUE,
        textAlign = textAlign
    )
}

@Composable
fun HeadlineText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    isSingleLine: Boolean = true,
    textAlign: TextAlign? = null,
    textDecoration: TextDecoration? = null
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyLarge,
        overflow = TextOverflow.Ellipsis,
        maxLines = if (isSingleLine) 1 else Int.MAX_VALUE,
        textAlign = textAlign,
        textDecoration = textDecoration
    )
}

@Composable
fun SupportingText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    isSingleLine: Boolean = true,
    textAlign: TextAlign? = null
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        maxLines = if (isSingleLine) 1 else Int.MAX_VALUE,
        textAlign = textAlign
    )
}

@Composable
fun TrailingText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    isSingleLine: Boolean = true,
    textAlign: TextAlign? = null
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        overflow = TextOverflow.Ellipsis,
        maxLines = if (isSingleLine) 1 else Int.MAX_VALUE,
        textAlign = textAlign
    )
}