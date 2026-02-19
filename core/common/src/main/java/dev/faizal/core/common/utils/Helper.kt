package dev.faizal.core.common.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate

fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "coffee" -> Color(0xFF6F4E37)
        "tea" -> Color(0xFF43A047)
        "snack" -> Color(0xFFFF9800)
        "dessert" -> Color(0xFFE91E63)
        "beverage", "drink" -> Color(0xFF2196F3)
        "food" -> Color(0xFFF44336)
        "breakfast" -> Color(0xFFFFA726)
        "lunch" -> Color(0xFF66BB6A)
        "dinner" -> Color(0xFF5C6BC0)
        else -> Color(0xFF9E9E9E)
    }
}

fun getBarColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF00BCD4), // Cyan
        Color(0xFF8BC34A), // Light Green
        Color(0xFFFF9800), // Orange
        Color(0xFF607D8B)  // Blue Grey
    )
    return colors[index % colors.size]
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDateInIndonesian(): String {
    val currentDate = LocalDate.now()
    val dayOfWeek = when (currentDate.dayOfWeek) {
        DayOfWeek.MONDAY -> "Senin"
        DayOfWeek.TUESDAY -> "Selasa"
        DayOfWeek.WEDNESDAY -> "Rabu"
        DayOfWeek.THURSDAY -> "Kamis"
        DayOfWeek.FRIDAY -> "Jumat"
        DayOfWeek.SATURDAY -> "Sabtu"
        DayOfWeek.SUNDAY -> "Minggu"
    }

    val month = when (currentDate.monthValue) {
        1 -> "Januari"
        2 -> "Februari"
        3 -> "Maret"
        4 -> "April"
        5 -> "Mei"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "Agustus"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        12 -> "Desember"
        else -> ""
    }

    return "$dayOfWeek, ${currentDate.dayOfMonth} $month ${currentDate.year}"
}

fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
        5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
        9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
        else -> ""
    }
}

fun formatDateToIndonesian(date: String): String {
    return try {
        // Dari "14/02/2026" ke "14 Februari 2026"
        val parts = date.split("/")
        val day = parts[0]
        val month = parts[1].toInt()
        val year = parts[2]

        val monthName = when (month) {
            1 -> "Januari"; 2 -> "Februari"; 3 -> "Maret"; 4 -> "April"
            5 -> "Mei"; 6 -> "Juni"; 7 -> "Juli"; 8 -> "Agustus"
            9 -> "September"; 10 -> "Oktober"; 11 -> "November"; 12 -> "Desember"
            else -> ""
        }

        "$day $monthName $year"
    } catch (e: Exception) {
        date // Return original if conversion fails
    }
}

fun getVisiblePages(currentPage: Int, totalPages: Int): List<Int> {
    if (totalPages <= 7) return (1..totalPages).toList()
    return when {
        currentPage <= 4 -> listOf(1, 2, 3, 4, 5, -1, totalPages)
        currentPage >= totalPages - 3 -> listOf(1, -1, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages)
        else -> listOf(1, -1, currentPage - 1, currentPage, currentPage + 1, -1, totalPages)
    }
}