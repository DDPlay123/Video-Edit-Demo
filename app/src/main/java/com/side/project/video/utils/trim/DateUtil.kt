package com.side.project.video.utils.trim

import android.text.TextUtils
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    /**
     * second to HH:MM:ss
     * @param seconds
     * @return
     */
    fun convertSecondsToTime(seconds: Long): String {
        val timeStr: String?
        val hour: Int
        var minute: Int
        val second: Int
        if (seconds <= 0) return "00:00" else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr = unitFormat(minute) + ":" + unitFormat(second)
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = (seconds - hour * 3600 - minute * 60).toInt()
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second)
            }
        }
        return timeStr
    }

    fun convertSecondsToFormat(seconds: Long, format: String?): String {
        if (TextUtils.isEmpty(format)) return ""
        val date = Date(seconds)
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(date)
    }

    private fun unitFormat(i: Int): String =
        if (i in 0..9) "0$i" else "" + i
}