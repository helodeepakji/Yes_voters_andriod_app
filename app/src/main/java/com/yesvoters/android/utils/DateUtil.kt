package com.yesvoters.android.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateUtil {
    fun getFormattedDate(isoDateTime: String): String {
        val zonedDateTime = ZonedDateTime.parse(isoDateTime)
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return zonedDateTime.format(dateFormatter)
    }

    fun getFormattedTime(isoDateTime: String): String {
        val zonedDateTime = ZonedDateTime.parse(isoDateTime)
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        return zonedDateTime.format(timeFormatter)
    }
}