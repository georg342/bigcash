package com.bigcash.core.database

import androidx.room3.ColumnTypeConverter

/** Room 3 converters for values stored as primitive columns. */
class Converters {

    @ColumnTypeConverter
    fun fromStringList(value: List<String>?): String =
        value.orEmpty().filter { it.isNotBlank() }.joinToString(separator = TAG_SEPARATOR)

    @ColumnTypeConverter
    fun toStringList(value: String?): List<String> =
        value.orEmpty()
            .split(TAG_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private companion object {
        const val TAG_SEPARATOR = "|"
    }
}
