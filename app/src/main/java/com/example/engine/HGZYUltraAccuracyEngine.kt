package com.example.engine

import com.example.model.IndicatorVote
import com.example.model.PredictionResult
import kotlin.math.exp
import kotlin.math.min

class HGZYUltraAccuracyEngine(private val historyLookback: Int = 30) {

    private fun indicatorMomentum(sizes: List<Int>): Pair<String?, Double> {
        val n = sizes.size
        if (n == 0) return Pair(null, 0.50)

        val weights = DoubleArray(n) { i ->
            val step = if (n > 1) 1.5 / (n - 1) else 0.0
            exp(-1.5 + (step * i))
        }
        val sumWeights = weights.sum().coerceAtLeast(1e-6)
        val normalizedWeights = weights.map { it / sumWeights }

        var score = 0.0
        for (i in sizes.indices) {
            score += sizes[i] * normalizedWeights[i]
        }

        return when {
            score >= 0.60 -> Pair("BIG", score)
            score <= 0.40 -> Pair("SMALL", 1.0 - score)
            else -> Pair(null, 0.50)
        }
    }

    private fun indicatorMarkovChain(sizes: List<Int>): Pair<String?, Double> {
        if (sizes.size < 6) return Pair(null, 0.50)

        val lastState = Pair(sizes[sizes.size - 2], sizes[sizes.size - 1])
        val transitions = mutableMapOf<Pair<Int, Int>, MutableList<Int>>()

        for (i in 0 until sizes.size - 2) {
            val state = Pair(sizes[i], sizes[i + 1])
            val nextVal = sizes[i + 2]
            transitions.getOrPut(state) { mutableListOf() }.add(nextVal)
        }

        val nextList = transitions[lastState]
        if (!nextList.isNullOrEmpty()) {
            val bigProb = nextList.sum().toDouble() / nextList.size
            return when {
                bigProb >= 0.65 -> Pair("BIG", bigProb)
                bigProb <= 0.35 -> Pair("SMALL", 1.0 - bigProb)
                else -> Pair(null, 0.50)
            }
        }
        return Pair(null, 0.50)
    }

    private fun indicatorStreakTrend(sizes: List<Int>): Pair<String?, Double> {
        if (sizes.size < 2) return Pair(null, 0.50)

        var currentStreak = 1
        for (i in sizes.size - 1 downTo 1) {
            if (sizes[i] == sizes[i - 1]) {
                currentStreak++
            } else {
                break
            }
        }

        val lastVal = sizes.last()
        return when {
            currentStreak >= 5 -> Pair(null, 0.50) // Overbought / potential reversal exhaustion
            currentStreak >= 2 -> {
                val pred = if (lastVal == 1) "BIG" else "SMALL"
                val conf = min(0.60 + (currentStreak * 0.08), 0.85)
                Pair(pred, conf)
            }
            else -> Pair(null, 0.50)
        }
    }

    private fun indicatorPatternMatching(sizes: List<Int>): Pair<String?, Double> {
        if (sizes.size < 12) return Pair(null, 0.50)

        val pattern = sizes.takeLast(4)
        val matches = mutableListOf<Int>()

        for (i in 0 until sizes.size - 4) {
            if (sizes.subList(i, i + 4) == pattern) {
                matches.add(sizes[i + 4])
            }
        }

        if (matches.size >= 2) {
            val bigRatio = matches.sum().toDouble() / matches.size
            return when {
                bigRatio >= 0.67 -> Pair("BIG", bigRatio)
                bigRatio <= 0.33 -> Pair("SMALL", 1.0 - bigRatio)
                else -> Pair(null, 0.50)
            }
        }
        return Pair(null, 0.50)
    }

    fun analyzeAndPredict(historyNumbers: List<Int>, serverTimeBd: String): PredictionResult {
        if (historyNumbers.size < 15) {
            return PredictionResult(
                status = "SKIP",
                action = "WAIT",
                predictedSize = null,
                predictedColor = null,
                confidence = 0.0,
                consensusScore = "0/4 Indicators",
                bigVotes = 0,
                smallVotes = 0,
                votes = emptyList(),
                message = "⚠️ পর্যাপ্ত ডাটা নেই (কমপক্ষে ১৫টি রেকর্ড প্রয়োজন) / Insufficient history data (need >= 15 rounds).",
                serverTimeBd = serverTimeBd
            )
        }

        val recentNumbers = historyNumbers.takeLast(historyLookback)
        val sizes = recentNumbers.map { if (it >= 5) 1 else 0 }

        val (momPred, momConf) = indicatorMomentum(sizes)
        val (markovPred, markovConf) = indicatorMarkovChain(sizes)
        val (streakPred, streakConf) = indicatorStreakTrend(sizes)
        val (patternPred, patternConf) = indicatorPatternMatching(sizes)

        val votes = listOf(
            IndicatorVote(
                name = "Momentum (EMA)",
                nameBn = "মোমেন্টাম (এক্সপোনেনশিয়াল)",
                prediction = momPred,
                confidence = momConf,
                description = when (momPred) {
                    "BIG" -> "Strong upward trend momentum detected"
                    "SMALL" -> "Strong downward pressure detected"
                    else -> "Momentum neutral (40%-60% range)"
                }
            ),
            IndicatorVote(
                name = "Markov Chain",
                nameBn = "মারকোভ চেইন ম্যাট্রিক্স",
                prediction = markovPred,
                confidence = markovConf,
                description = when (markovPred) {
                    "BIG" -> "High state transition probability towards BIG (>=65%)"
                    "SMALL" -> "High state transition probability towards SMALL (>=65%)"
                    else -> "State transition distribution balanced"
                }
            ),
            IndicatorVote(
                name = "Streak Trend",
                nameBn = "ধারাবাহিক ট্রেন্ড অ্যানালাইসিস",
                prediction = streakPred,
                confidence = streakConf,
                description = when {
                    streakPred == "BIG" -> "Active BIG streak continuation favored"
                    streakPred == "SMALL" -> "Active SMALL streak continuation favored"
                    else -> "Streak exhausted or choppy market (<2 or >=5)"
                }
            ),
            IndicatorVote(
                name = "Pattern Matching",
                nameBn = "প্যাটার্ন রিকগনিশন (৪-ধাপ)",
                prediction = patternPred,
                confidence = patternConf,
                description = when (patternPred) {
                    "BIG" -> "Historical 4-step sequence matched -> BIG favored"
                    "SMALL" -> "Historical 4-step sequence matched -> SMALL favored"
                    else -> "Insufficient sequence repetitions in window"
                }
            )
        )

        val bigVotes = votes.count { it.prediction == "BIG" }
        val smallVotes = votes.count { it.prediction == "SMALL" }

        val validConfs = votes
            .filter { it.prediction != null && it.confidence > 0.50 }
            .map { it.confidence }

        val avgConfidence = if (validConfs.isNotEmpty()) {
            validConfs.sum() / validConfs.size
        } else {
            0.70
        }

        val finalPrediction = when {
            bigVotes >= 3 -> "BIG"
            smallVotes >= 3 -> "SMALL"
            else -> null
        }

        if (finalPrediction == null) {
            return PredictionResult(
                status = "SKIP",
                action = "WAIT",
                predictedSize = null,
                predictedColor = null,
                confidence = (avgConfidence * 100.0),
                consensusScore = "${maxOf(bigVotes, smallVotes)}/4 Indicators",
                bigVotes = bigVotes,
                smallVotes = smallVotes,
                votes = votes,
                message = "⚠️ ইন্ডিকেটরগুলোর মধ্যে একমত নেই (Low Consensus $bigVotes BIG vs $smallVotes SMALL)। এই রাউন্ড স্কিপ করুন!",
                serverTimeBd = serverTimeBd
            )
        }

        val predictedColor = if (finalPrediction == "BIG") "GREEN" else "RED"

        return PredictionResult(
            status = "SUCCESS",
            action = "TRADE",
            predictedSize = finalPrediction,
            predictedColor = predictedColor,
            confidence = (avgConfidence * 100.0),
            consensusScore = "${maxOf(bigVotes, smallVotes)}/4 Indicators",
            bigVotes = bigVotes,
            smallVotes = smallVotes,
            votes = votes,
            message = "✅ হাই কনফিডেন্স সিগন্যাল প্রস্তুত! নিয়ম মেনে ৩-লেভেল মার্টিংগেল সহ ট্রেড করুন।",
            serverTimeBd = serverTimeBd
        )
    }
}
