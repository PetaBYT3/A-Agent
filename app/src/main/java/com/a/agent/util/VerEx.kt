package com.a.agent.util

import java.util.regex.Pattern

class VerEx {
    private val prefixes = StringBuilder()
    private val source = StringBuilder()
    private val suffixes = StringBuilder()
    private var modifiers = Pattern.MULTILINE

    fun sanitize(value: String): String {
        return value.replace("[\\|\\Q\\\\E\\$\\^\\.\\?\\*\\+\\(\\)\\[\\]\\{\\}]".toRegex(), "\\\\$0")
    }

    fun add(value: String): VerEx = apply { source.append(value) }

    fun then(value: String): VerEx = add("(${sanitize(value)})")

    fun maybe(value: String): VerEx = add("(${sanitize(value)})?")

    fun beginCapture(): VerEx = add("(")

    fun endCapture(): VerEx = add(")")

    fun anythingBut(value: String): VerEx = add("([^${sanitize(value)}]+)")

    fun toRegex(): Regex {
        return toString().toRegex()
    }

    override fun toString(): String {
        return prefixes.toString() + source.toString() + suffixes.toString()
    }
}