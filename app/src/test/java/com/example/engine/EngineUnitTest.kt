package com.example.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineUnitTest {

    @Test
    fun testMartingaleCalculator() {
        val calculator = MartingaleCalculator()
        val plan = calculator.calculate(
            totalBalance = 7000.0,
            baseBetInput = 1000.0,
            multiplier = 2.0,
            payoutRatio = 1.96
        )

        assertEquals("SAFE", plan.status)
        assertTrue(plan.isSufficient)
        assertEquals(3, plan.steps.size)

        val s1 = plan.steps[0]
        assertEquals(1000.0, s1.betAmount, 0.01)
        assertEquals(1960.0, s1.winReturn, 0.01)
        assertEquals(960.0, s1.netProfit, 0.01)

        val s2 = plan.steps[1]
        assertEquals(2000.0, s2.betAmount, 0.01)
        assertEquals(3920.0, s2.winReturn, 0.01)
        // netProfit = 3920 - (1000 + 2000) = 920
        assertEquals(920.0, s2.netProfit, 0.01)

        val s3 = plan.steps[2]
        assertEquals(4000.0, s3.betAmount, 0.01)
        assertEquals(7840.0, s3.winReturn, 0.01)
        // netProfit = 7840 - 7000 = 840
        assertEquals(840.0, s3.netProfit, 0.01)

        assertEquals(7000.0, plan.totalCapitalRequired, 0.01)
    }

    @Test
    fun testPredictionEngineWithStrongBigTrend() {
        val engine = HGZYUltraAccuracyEngine()
        // 20 BIG numbers
        val numbers = List(25) { 8 }
        val result = engine.analyzeAndPredict(numbers, "2026-09-28 16:00:00")

        assertEquals("SUCCESS", result.status)
        assertEquals("TRADE", result.action)
        assertEquals("BIG", result.predictedSize)
        assertEquals("GREEN", result.predictedColor)
        assertTrue(result.confidence > 50.0)
    }

    @Test
    fun testPredictionEngineInsufficientData() {
        val engine = HGZYUltraAccuracyEngine()
        val numbers = listOf(1, 2, 3)
        val result = engine.analyzeAndPredict(numbers, "2026-09-28 16:00:00")

        assertEquals("SKIP", result.status)
        assertEquals("WAIT", result.action)
    }

    @Test
    fun testPeriodSyncBangladeshTime() {
        val sync = WinGoPeriodSyncBD()
        val info1m = sync.getLivePeriodInfo(com.example.model.WinGoMode.ONE_MINUTE)
        assertNotNull(info1m.currentPeriodId)
        assertTrue(info1m.remainingSeconds in 1..60)
        assertTrue(info1m.currentPeriodId.contains("1000"))

        val info30s = sync.getLivePeriodInfo(com.example.model.WinGoMode.THIRTY_SECONDS)
        assertNotNull(info30s.currentPeriodId)
        assertTrue(info30s.remainingSeconds in 1..30)
    }

    @Test
    fun test100RoundsBigSmallFrequency() {
        val seedNums = listOf(
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
        assertEquals(100, seedNums.size)
        val bigCount = seedNums.count { it >= 5 }
        val smallCount = seedNums.count { it < 5 }
        assertEquals(100, bigCount + smallCount)
        assertTrue(bigCount > 40 && bigCount < 60)
    }
}
