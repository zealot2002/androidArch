package com.joy.tools.utils

object ValidateUtils {
    fun isEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) return false
        val pattern = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return pattern.matches(email)
    }
    
    fun isPhone(phone: String?): Boolean {
        if (phone.isNullOrEmpty()) return false
        val pattern = Regex("1[3-9]\\d{9}")
        return pattern.matches(phone)
    }
    
    fun isUrl(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        val pattern = Regex("https?://[\\w-]+(\\.[\\w-]+)+[/\\w-._~:/?#[\\]@!$&'()*+,;=%]*")
        return pattern.matches(url)
    }
    
    fun isNumeric(str: String?): Boolean {
        if (str.isNullOrEmpty()) return false
        return str.all { it.isDigit() }
    }
    
    fun isDecimal(str: String?): Boolean {
        if (str.isNullOrEmpty()) return false
        return str.matches(Regex("-?\\d+\\.\\d+"))
    }
    
    fun isEmpty(str: String?): Boolean = str.isNullOrEmpty()
    
    fun isNotEmpty(str: String?): Boolean = !isEmpty(str)
    
    fun lengthBetween(str: String?, min: Int, max: Int): Boolean {
        if (str == null) return false
        return str.length in min..max
    }
}