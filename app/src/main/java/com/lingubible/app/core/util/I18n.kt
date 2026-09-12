package com.lingubible.app.core.util

import android.content.Context
import androidx.annotation.StringRes

object I18n {
    /**
     * Maps web key (e.g. "auth.signIn", "animal.🐱", "404.title") to Android resource identifier.
     */
    fun keyToResName(key: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < key.length) {
            val codePoint = key.codePointAt(i)
            val charCount = Character.charCount(codePoint)
            if (codePoint > 127) {
                sb.append("u").append(Integer.toHexString(codePoint))
            } else {
                val ch = key[i]
                when (ch) {
                    '.', '-' -> sb.append('_')
                    else -> sb.append(ch)
                }
            }
            i += charCount
        }
        val sanitized = sb.toString()
        return if (sanitized.isNotEmpty() && sanitized[0].isDigit()) "_$sanitized" else sanitized
    }

    @StringRes
    fun getStringResId(context: Context, key: String): Int {
        val resName = keyToResName(key)
        return context.resources.getIdentifier(resName, "string", context.packageName)
    }

    fun t(context: Context, key: String, vararg args: Any): String {
        val resId = getStringResId(context, key)
        return if (resId != 0) {
            if (args.isEmpty()) context.getString(resId) else context.getString(resId, *args)
        } else {
            key
        }
    }

    /**
     * Handles singular|plural formats (e.g., "Found %1$s course|courses")
     */
    fun processPluralTranslation(text: String, count: Number): String {
        val pluralRegex = Regex("""(\w+)\|(\w+)""")
        val isSingular = when (count) {
            is Int -> count == 1
            is Long -> count == 1L
            is Double -> count == 1.0
            else -> count.toInt() == 1
        }
        return pluralRegex.replace(text) { matchResult ->
            if (isSingular) matchResult.groupValues[1] else matchResult.groupValues[2]
        }
    }
}
