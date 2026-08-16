package com.a.agent.util

import java.io.Serializable

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
): Serializable {
    override fun toString(): String {
        return "($first, $second, $third, $fourth)"
    }
}

fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)