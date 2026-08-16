package com.a.agent.data.util

fun Long?.toMegaByte(): String {
    return if (this != null) {
        "${this / (1024 * 1024)}MB"
    } else {
        "0MB"
    }
}