package com.a.agent.presentation.util.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun CustomTransparentTextField(
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent
    ),
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    maxLines: Int =  if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    MaterialExpressiveTheme(
        motionScheme = MotionScheme.standard()
    ) {
        TextField(
            modifier = modifier,
            shape = TextFieldDefaults.roundedShape,
            colors = colors,
            label = label,
            placeholder = placeholder,
            value = value,
            onValueChange = { onValueChange(it) },
            isError = isError,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int =  if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    MaterialExpressiveTheme(
        motionScheme = MotionScheme.standard()
    ) {
        TextField(
            modifier = modifier,
            colors = colors,
            label = label,
            placeholder = placeholder,
            value = value,
            onValueChange = { onValueChange(it) },
            isError = isError,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun CustomPasswordTextField(
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    label: @Composable (() -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    var isPasswordVisible by remember {
        mutableStateOf(true)
    }
    MaterialExpressiveTheme(
        motionScheme = MotionScheme.standard()
    ) {
        TextField(
            modifier = modifier,
            colors = colors,
            label = label,
            value = value,
            onValueChange = { onValueChange(it) },
            isError = isError,
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            supportingText = supportingText
        )
    }
}