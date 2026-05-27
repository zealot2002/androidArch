package com.joy.tools.utils

object StringUtils {
    fun isEmpty(str: String?): Boolean = str == null || str.isEmpty()
    fun isNotEmpty(str: String?): Boolean = !isEmpty(str)
    fun isBlank(str: String?): Boolean = str == null || str.isBlank()
    fun isNotBlank(str: String?): Boolean = !isBlank(str)
    fun trim(str: String?): String = str?.trim() ?: ""
    fun equalsIgnoreCase(str1: String?, str2: String?): Boolean = str1?.equals(str2, ignoreCase = true) ?: (str2 == null)
    fun substringBefore(str: String, delimiter: String): String {
        val index = str.indexOf(delimiter)
        return if (index == -1) str else str.substring(0, index)
    }
    fun substringAfter(str: String, delimiter: String): String {
        val index = str.indexOf(delimiter)
        return if (index == -1) str else str.substring(index + delimiter.length)
    }
    fun replace(str: String, oldChar: Char, newChar: Char): String = str.replace(oldChar, newChar)
    fun split(str: String, delimiter: String): Array<String> = str.split(delimiter).toTypedArray()
    fun join(elements: Array<String>, delimiter: String): String = elements.joinToString(delimiter)
    fun startsWith(str: String, prefix: String): Boolean = str.startsWith(prefix)
    fun endsWith(str: String, suffix: String): Boolean = str.endsWith(suffix)
    fun contains(str: String, substring: String): Boolean = str.contains(substring)
}