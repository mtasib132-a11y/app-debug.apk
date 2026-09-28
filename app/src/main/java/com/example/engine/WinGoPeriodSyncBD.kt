package com.example.engine

import com.example.model.LivePeriodInfo
import com.example.model.WinGoMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class WinGoPeriodSyncBD {

    private val bangladeshTimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun getLivePeriodInfo(mode: WinGoMode): LivePeriodInfo {
        val duration = mode.durationSeconds
        val cal = Calendar.getInstance(bangladeshTimeZone)

        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val millis = cal.get(Calendar.MILLISECOND)

        val secondsSinceMidnight = (hour * 3600) + (minute * 60) + second + (millis / 1000.0)

        val periodNumber = (secondsSinceMidnight / duration).toLong() + 1
        val remainingSeconds = (duration - (secondsSinceMidnight.toLong() % duration)).toInt()

        val dateSdf = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            timeZone = bangladeshTimeZone
        }
        val fullTimeSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = bangladeshTimeZone
        }

        val dateStr = dateSdf.format(cal.time)
        val serverTimeBd = fullTimeSdf.format(cal.time)
        val currentPeriodId = String.format(Locale.US, "%s1000%04d", dateStr, periodNumber)

        return LivePeriodInfo(
            currentPeriodId = currentPeriodId,
            remainingSeconds = remainingSeconds,
            totalDurationSeconds = duration,
            serverTimeBd = serverTimeBd,
            timezone = "Asia/Dhaka (BST - UTC+6)"
        )
    }

    fun getCurrentBdTimeStr(): String {
        val cal = Calendar.getInstance(bangladeshTimeZone)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = bangladeshTimeZone
        }
        return sdf.format(cal.time)
    }

    fun convertCstToBst(cstTimeStr: String): String {
        return try {
            val cstSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            }
            val date = cstSdf.parse(cstTimeStr)
            val bstSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = bangladeshTimeZone
            }
            if (date != null) bstSdf.format(date) else getCurrentBdTimeStr()
        } catch (_: Exception) {
            getCurrentBdTimeStr()
        }
    }
}
