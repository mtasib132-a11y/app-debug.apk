package com.example.model

enum class WinGoMode(val title: String, val durationSeconds: Int) {
    ONE_MINUTE("WinGo 1 Min", 60),
    THIRTY_SECONDS("WinGo 30 Sec", 30)
}

data class LivePeriodInfo(
    val currentPeriodId: String,
    val remainingSeconds: Int,
    val totalDurationSeconds: Int,
    val serverTimeBd: String,
    val timezone: String = "Asia/Dhaka (BST - UTC+6)"
)

data class IndicatorVote(
    val name: String,
    val nameBn: String,
    val prediction: String?, // "BIG", "SMALL", or null
    val confidence: Double,
    val description: String
)

data class PredictionResult(
    val status: String, // "SUCCESS", "SKIP", "WAIT"
    val action: String, // "TRADE", "WAIT"
    val predictedSize: String?, // "BIG", "SMALL"
    val predictedColor: String?, // "GREEN", "RED"
    val confidence: Double,
    val consensusScore: String,
    val bigVotes: Int,
    val smallVotes: Int,
    val votes: List<IndicatorVote>,
    val message: String?,
    val serverTimeBd: String
)

data class MartingaleStep(
    val step: Int,
    val label: String,
    val betAmount: Double,
    val winReturn: Double,
    val netProfit: Double
)

data class MartingalePlan(
    val status: String, // "SAFE", "INSUFFICIENT_BALANCE"
    val totalBalance: Double,
    val recommendedBaseBet: Double,
    val baseBet: Double,
    val multiplier: Double,
    val payoutRatio: Double,
    val totalCapitalRequired: Double,
    val isSufficient: Boolean,
    val safetyNoteEn: String,
    val safetyNoteBn: String,
    val steps: List<MartingaleStep>
)

data class HistoryStats(
    val totalAnalyzedRounds: Int,
    val bigCount: Int,
    val bigPercentage: Double,
    val smallCount: Int,
    val smallPercentage: Double,
    val greenCount: Int,
    val greenPercentage: Double,
    val redCount: Int,
    val redPercentage: Double,
    val violetCount: Int,
    val violetPercentage: Double,
    val numberDistribution: Map<Int, Int>
)

data class RecentTrendItem(
    val number: Int,
    val size: String,
    val color: String
)

data class ApiHistoryStatsData(
    val totalAnalyzedRounds: Int = 0,
    val recent10Trend: List<RecentTrendItem> = emptyList(),
    val numberDistribution: Map<String, Int> = emptyMap(),
    val message: String? = null
)

data class ApiHistoryStatsResponse(
    val status: String = "SUCCESS",
    val data: ApiHistoryStatsData? = null
)
