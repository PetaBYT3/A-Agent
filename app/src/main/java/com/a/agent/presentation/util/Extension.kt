package com.a.agent.presentation.util

fun Long.toMegaByte(): String {
    val size = this / (1024 * 1024)
    return "${size} MB"
}