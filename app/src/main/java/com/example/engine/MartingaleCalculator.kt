package com.example.engine

import com.example.model.MartingalePlan
import com.example.model.MartingaleStep
import kotlin.math.round

class MartingaleCalculator {

    private fun round2(value: Double): Double = round(value * 100.0) / 100.0

    fun calculate(
        totalBalance: Double,
        baseBetInput: Double? = null,
        multiplier: Double = 2.0,
        payoutRatio: Double = 1.96
    ): MartingalePlan {
        val totalUnits = 1.0 + multiplier + (multiplier * multiplier)
        val recommendedBase = round2(totalBalance / totalUnits)

        val l1Bet = if (baseBetInput != null && baseBetInput > 0) {
            round2(baseBetInput)
        } else {
            recommendedBase
        }

        val l2Bet = round2(l1Bet * multiplier)
        val l3Bet = round2(l2Bet * multiplier)
        val totalCapitalRequired = round2(l1Bet + l2Bet + l3Bet)

        val isSufficient = totalBalance >= totalCapitalRequired

        val step1 = MartingaleStep(
            step = 1,
            label = "Level 1 (বেস ট্রেড)",
            betAmount = l1Bet,
            winReturn = round2(l1Bet * payoutRatio),
            netProfit = round2((l1Bet * payoutRatio) - l1Bet)
        )

        val step2 = MartingaleStep(
            step = 2,
            label = "Level 2 (রিকভারি ১)",
            betAmount = l2Bet,
            winReturn = round2(l2Bet * payoutRatio),
            netProfit = round2((l2Bet * payoutRatio) - (l1Bet + l2Bet))
        )

        val step3 = MartingaleStep(
            step = 3,
            label = "Level 3 (রিকভারি ২)",
            betAmount = l3Bet,
            winReturn = round2(l3Bet * payoutRatio),
            netProfit = round2((l3Bet * payoutRatio) - totalCapitalRequired)
        )

        val safetyNoteEn = if (isSufficient) {
            "Capital is sufficient for all 3 levels. Maintain discipline and stick to the plan."
        } else {
            "⚠️ Warning: Insufficient balance to sustain 3 Martingale steps! Reduce base bet to avoid liquidation risk."
        }

        val safetyNoteBn = if (isSufficient) {
            "ব্যালেন্স পর্যাপ্ত আছে। ৩টি লেভেল খেলার পুঁজি সুরক্ষিত, নিয়ম মেনে ট্রেড করুন।"
        } else {
            "⚠️ সতর্কতা: আপনার ব্যালেন্স অনুযায়ী ৩টি লেভেল খেলার পুঁজি নেই! বেস বেট কমান।"
        }

        return MartingalePlan(
            status = if (isSufficient) "SAFE" else "INSUFFICIENT_BALANCE",
            totalBalance = totalBalance,
            recommendedBaseBet = recommendedBase,
            baseBet = l1Bet,
            multiplier = multiplier,
            payoutRatio = payoutRatio,
            totalCapitalRequired = totalCapitalRequired,
            isSufficient = isSufficient,
            safetyNoteEn = safetyNoteEn,
            safetyNoteBn = safetyNoteBn,
            steps = listOf(step1, step2, step3)
        )
    }
}
