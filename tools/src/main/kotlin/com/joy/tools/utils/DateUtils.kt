package com.joy.tools.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss"
    
    fun format(date: Date?, pattern: String = DEFAULT_FORMAT): String {
        if (date == null) return ""
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }
    
    fun parse(dateStr: String, pattern: String = DEFAULT_FORMAT): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getCurrentTime(): Long = System.currentTimeMillis()
    
    fun getCurrentDate(): Date = Date()
    
    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               today.get(Calendar.MONTH) == target.get(Calendar.MONTH) &&
               today.get(Calendar.DAY_OF_MONTH) == target.get(Calendar.DAY_OF_MONTH)
    }
    
    fun getDaysBetween(start: Date, end: Date): Long {
        return (end.time - start.time) / (1000 * 60 * 60 * 24)
    }
    
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }
    
    fun toCalendar(date: Date): Calendar {
        return Calendar.getInstance().apply { time = date }
    }
}