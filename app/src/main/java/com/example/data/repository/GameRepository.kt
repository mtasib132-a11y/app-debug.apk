package com.example.data.repository

import com.example.data.local.GameHistoryDao
import com.example.data.local.GameHistoryEntity
import com.example.data.remote.ApiClientProvider
import com.example.data.remote.HgzyApiService
import com.example.data.remote.HgzyHistoryRequest
import com.example.engine.WinGoPeriodSyncBD
import com.example.model.ApiHistoryStatsData
import com.example.model.HistoryStats
import com.example.model.RecentTrendItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class GameRepository(
    private val dao: GameHistoryDao,
    private val apiService: HgzyApiService = ApiClientProvider.createService(),
    private val periodSync: WinGoPeriodSyncBD = WinGoPeriodSyncBD()
) {

    val allHistory: Flow<List<GameHistoryEntity>> = dao.getAllHistory()

    fun getRecentHistory(limit: Int = 50): Flow<List<GameHistoryEntity>> =
        dao.getRecentHistory(limit)

    suspend fun getRecentNumbers(limit: Int = 50): List<Int> = withContext(Dispatchers.IO) {
        val numbers = dao.getRecentNumbers(limit)
        numbers.reversed() // oldest to newest for analytical indicators
    }

    fun getStats(limit: Int = 100): Flow<HistoryStats> {
        return dao.getRecentHistory(limit).map { list ->
            calculateStats(list)
        }
    }

    private fun calculateStats(list: List<GameHistoryEntity>): HistoryStats {
        val total = list.size
        if (total == 0) {
            return HistoryStats(
                totalAnalyzedRounds = 0,
                bigCount = 0,
                bigPercentage = 0.0,
                smallCount = 0,
                smallPercentage = 0.0,
                greenCount = 0,
                greenPercentage = 0.0,
                redCount = 0,
                redPercentage = 0.0,
                violetCount = 0,
                violetPercentage = 0.0,
                numberDistribution = (0..9).associateWith { 0 }
            )
        }

        val bigCount = list.count { it.size == "BIG" }
        val smallCount = list.count { it.size == "SMALL" }
        val greenCount = list.count { it.color.contains("GREEN") }
        val redCount = list.count { it.color.contains("RED") }
        val violetCount = list.count { it.color.contains("VIOLET") }

        val numMap = (0..9).associateWith { num ->
            list.count { it.number == num }
        }

        return HistoryStats(
            totalAnalyzedRounds = total,
            bigCount = bigCount,
            bigPercentage = ((bigCount.toDouble() / total) * 100.0),
            smallCount = smallCount,
            smallPercentage = ((smallCount.toDouble() / total) * 100.0),
            greenCount = greenCount,
            greenPercentage = ((greenCount.toDouble() / total) * 100.0),
            redCount = redCount,
            redPercentage = ((redCount.toDouble() / total) * 100.0),
            violetCount = violetCount,
            violetPercentage = ((violetCount.toDouble() / total) * 100.0),
            numberDistribution = numMap
        )
    }

    suspend fun insertGameResult(periodId: String, number: Int, customTimeBd: String? = null): Long =
        withContext(Dispatchers.IO) {
            val size = if (number >= 5) "BIG" else "SMALL"
            val color = when (number) {
                0 -> "RED+VIOLET"
                5 -> "GREEN+VIOLET"
                1, 3, 7, 9 -> "GREEN"
                else -> "RED"
            }
            val createdAt = customTimeBd ?: periodSync.getCurrentBdTimeStr()

            val entity = GameHistoryEntity(
                periodId = periodId,
                number = number,
                size = size,
                color = color,
                createdAt = createdAt
            )
            dao.insert(entity)
        }

    suspend fun fetchAndSyncRemote(
        targetUrl: String,
        bearerToken: String,
        limit: Int = 30
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val tokenHeader = if (bearerToken.startsWith("Bearer ", ignoreCase = true)) {
                bearerToken
            } else {
                "Bearer $bearerToken"
            }

            val response = apiService.getWinGoHistory(
                url = targetUrl,
                authorization = tokenHeader,
                request = HgzyHistoryRequest(typeId = 1, pageNo = 1, pageSize = limit)
            )

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP ${response.code()}: ${response.message()}")
                )
            }

            val body = response.body()
            if (body == null) {
                return@withContext Result.failure(Exception("Empty API response"))
            }

            if (body.code != 0 && body.status != "success") {
                return@withContext Result.failure(
                    Exception("API Error: code=${body.code}, msg=${body.message}")
                )
            }

            val list = body.data?.list.orEmpty()
            if (list.isEmpty()) {
                return@withContext Result.success(0)
            }

            val entities = list.mapNotNull { item ->
                val pId = item.issueNumber ?: return@mapNotNull null
                val num = item.number?.toIntOrNull() ?: return@mapNotNull null
                val sz = if (num >= 5) "BIG" else "SMALL"
                val clr = when (num) {
                    0 -> "RED+VIOLET"
                    5 -> "GREEN+VIOLET"
                    1, 3, 7, 9 -> "GREEN"
                    else -> "RED"
                }
                val bdTime = if (!item.openTime.isNullOrBlank()) {
                    periodSync.convertCstToBst(item.openTime)
                } else {
                    periodSync.getCurrentBdTimeStr()
                }

                GameHistoryEntity(
                    periodId = pId,
                    number = num,
                    size = sz,
                    color = clr,
                    createdAt = bdTime
                )
            }

            dao.insertAll(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentNumbers = dao.getRecentNumbers(5)
        if (currentNumbers.isEmpty()) {
            val tz = TimeZone.getTimeZone("Asia/Dhaka")
            val cal = Calendar.getInstance(tz)
            val dateSdf = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = tz }
            val timeSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply { timeZone = tz }
            val dateStr = dateSdf.format(cal.time)

            // Seed realistic 100 historical rounds
            val sampleNumbers = listOf(
                7, 2, 8, 9, 3, 1, 6, 8, 4, 0,
                5, 9, 7, 3, 2, 8, 6, 9, 1, 4,
                7, 8, 5, 2, 3, 9, 6, 4, 1, 8,
                7, 9, 3, 6, 2, 8, 5, 7, 1, 0,
                3, 9, 6, 2, 8, 4, 5, 7, 9, 1,
                6, 3, 8, 2, 7, 5, 9, 0, 4, 8,
                1, 7, 3, 9, 6, 2, 5, 8, 4, 1,
                9, 7, 3, 6, 2, 8, 5, 0, 4, 7,
                9, 1, 3, 6, 8, 2, 5, 7, 9, 4,
                1, 8, 3, 6, 2, 7, 9, 5, 0, 8
            )

            val basePeriod = 500
            val entities = sampleNumbers.mapIndexed { index, num ->
                val pId = String.format(Locale.US, "%s1000%04d", dateStr, basePeriod - index)
                val calCopy = Calendar.getInstance(tz).apply {
                    add(Calendar.MINUTE, -index)
                }
                val sz = if (num >= 5) "BIG" else "SMALL"
                val clr = when (num) {
                    0 -> "RED+VIOLET"
                    5 -> "GREEN+VIOLET"
                    1, 3, 7, 9 -> "GREEN"
                    else -> "RED"
                }

                GameHistoryEntity(
                    periodId = pId,
                    number = num,
                    size = sz,
                    color = clr,
                    createdAt = timeSdf.format(calCopy.time)
                )
            }
            dao.insertAll(entities)
        }
    }

    suspend fun fetchHistoryStats(
        endpointUrl: String = "https://hgnice0.com/api/v1/history/stats",
        limit: Int = 100
    ): Pair<ApiHistoryStatsData, Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHistoryStats(endpointUrl, limit)
            if (response.isSuccessful && response.body()?.data != null) {
                val r = response.body()!!.data!!
                val mapped = ApiHistoryStatsData(
                    totalAnalyzedRounds = r.totalAnalyzedRounds,
                    recent10Trend = r.recent10Trend.map {
                        RecentTrendItem(it.number, it.size, it.color)
                    },
                    numberDistribution = r.numberDistribution,
                    message = "Successfully fetched from $endpointUrl"
                )
                return@withContext Pair(mapped, true)
            }
        } catch (_: Exception) {
            // fallback to local Room db
        }

        val list = dao.getRecentHistoryList(limit)
        val recent10 = list.take(10).map {
            RecentTrendItem(number = it.number, size = it.size, color = it.color)
        }
        val dist = (0..9).associate { num ->
            num.toString() to list.count { it.number == num }
        }
        val fallback = ApiHistoryStatsData(
            totalAnalyzedRounds = list.size,
            recent10Trend = recent10,
            numberDistribution = dist,
            message = "Loaded from local database (offline fallback)"
        )
        Pair(fallback, false)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }
}
