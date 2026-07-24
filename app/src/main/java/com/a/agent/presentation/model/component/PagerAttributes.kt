package com.a.agent.presentation.model.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val ModelPagerModifier = Modifier.fillMaxSize().padding(top = 10.dp)
val ModelLazyColumnPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 100.dp)

@SuppressLint("ModifierFactoryExtensionFunction")
fun LazyItemScope.modelPagerMessagePadding(): Modifier {
    return Modifier.padding(bottom = 10.dp).animateItem()
}