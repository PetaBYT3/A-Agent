package com.a.agent.data.util

import kotlinx.serialization.json.Json

inline fun <reified T> jsonByteArrayToDataClass(byteArray: ByteArray): T? {
    return try {
        val stringJson = String(byteArray, Charsets.UTF_8)
        Json.decodeFromString<T>(stringJson)
    } catch (e: Exception) {
        null
    }
}