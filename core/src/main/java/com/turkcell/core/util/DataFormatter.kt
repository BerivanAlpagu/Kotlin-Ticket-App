package com.turkcell.core.util

import java.text.SimpleDateFormat
import java.util.Locale

object DataFormatter {
    fun formatDate(isoString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
            val date = parser.parse(isoString)
            date?.let { formatter.format(it) } ?: isoString
        } catch (e: Exception) {
            isoString
        }
    }
}
