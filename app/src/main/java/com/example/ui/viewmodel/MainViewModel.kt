package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.GameHistoryEntity
import com.example.data.repository.GameRepository
import com.example.engine.HGZYUltraAccuracyEngine
import com.example.engine.MartingaleCalculator
import com.example.engine.WinGoPeriodSyncBD
import com.example.model.HistoryStats
import com.example.model.LivePeriodInfo
import com.example.model.MartingalePlan
import com.example.model.PredictionResult
import com.example.model.WinGoMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedMode: WinGoMode = WinGoMode.ONE_MINUTE,
    val livePeriodInfo: LivePeriodInfo = LivePeriodInfo("", 60, 60, ""),
    val predictionResult: PredictionResult = PredictionResult(
        status = "WAIT",
        action = "WAIT",
        predictedSize = null,
        predictedColor = null,
        confidence = 0.0,
        consensusScore = "0/4 Indicators",
        bigVotes = 0,
        smallVotes = 0,
        votes = emptyList(),
        message = "ইনলাইজিং সিগন্যাল ইঞ্জিন...",
        serverTimeBd = ""
    ),
    val historyList: List<GameHistoryEntity> = emptyList(),
    val stats: HistoryStats = HistoryStats(
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
        numberDistribution = emptyMap()
    ),
    val martingalePlan: MartingalePlan? = null,
    val balanceInput: String = "5000",
    val baseBetInput: String = "",
    val multiplier: Double = 2.0,
    val payoutRatio: Double = 1.96,
    val liveMartingaleStep: Int = 1,
    val sessionProfit: Double = 0.0,
    val sessionWins: Int = 0,
    val sessionLosses: Int = 0,
    val authToken: String = "YOUR_BEARER_TOKEN_HERE",
    val targetApiUrl: String = "https://hgnice0.com/api/web/game/winGo/history",
    val isAutoSyncEnabled: Boolean = false,
    val syncIntervalSeconds: Int = 15,
    val lastSyncStatus: String? = null,
    val isSyncing: Boolean = false,
    val isBengali: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val periodSync = WinGoPeriodSyncBD()
    private val predictionEngine = HGZYUltraAccuracyEngine()
    private val martingaleCalculator = MartingaleCalculator()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var syncJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.gameHistoryDao())

        // Recalculate initial Martingale plan
        recalculateMartingale()

        // Seed initial mock/real data if db is empty
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Observe Room DB History
        viewModelScope.launch {
            repository.allHistory.collectLatest { list ->
                _uiState.update { it.copy(historyList = list) }
                refreshPrediction()
            }
        }

        // Observe Stats
        viewModelScope.launch {
            repository.getStats(100).collectLatest { stats ->
                _uiState.update { it.copy(stats = stats) }
            }
        }

        // Start 1-second clock loop
        startClockTicker()
    }

    private fun startClockTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val currentMode = _uiState.value.selectedMode
                val info = periodSync.getLivePeriodInfo(currentMode)

                // Detect period roll-over
                val previousPeriod = _uiState.value.livePeriodInfo.currentPeriodId
                if (previousPeriod.isNotBlank() && previousPeriod != info.currentPeriodId) {
                    // Period changed: Refresh prediction & if auto sync is on, trigger sync
                    refreshPrediction()
                    if (_uiState.value.isAutoSyncEnabled) {
                        triggerSync()
                    }
                }

                _uiState.update { it.copy(livePeriodInfo = info) }
                delay(1000)
            }
        }
    }

    fun setMode(mode: WinGoMode) {
        _uiState.update { it.copy(selectedMode = mode) }
        val info = periodSync.getLivePeriodInfo(mode)
        _uiState.update { it.copy(livePeriodInfo = info) }
        refreshPrediction()
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isBengali = !it.isBengali) }
    }

    fun refreshPrediction() {
        viewModelScope.launch {
            val numbers = repository.getRecentNumbers(35)
            val serverTime = periodSync.getCurrentBdTimeStr()
            val pred = predictionEngine.analyzeAndPredict(numbers, serverTime)
            _uiState.update { it.copy(predictionResult = pred) }
        }
    }

    fun updateMartingaleInputs(
        balance: String? = null,
        baseBet: String? = null,
        multiplier: Double? = null,
        payoutRatio: Double? = null
    ) {
        _uiState.update { current ->
            current.copy(
                balanceInput = balance ?: current.balanceInput,
                baseBetInput = baseBet ?: current.baseBetInput,
                multiplier = multiplier ?: current.multiplier,
                payoutRatio = payoutRatio ?: current.payoutRatio
            )
        }
        recalculateMartingale()
    }

    private fun recalculateMartingale() {
        val balance = _uiState.value.balanceInput.toDoubleOrNull() ?: 5000.0
        val baseBet = _uiState.value.baseBetInput.toDoubleOrNull()
        val plan = martingaleCalculator.calculate(
            totalBalance = balance,
            baseBetInput = baseBet,
            multiplier = _uiState.value.multiplier,
            payoutRatio = _uiState.value.payoutRatio
        )
        _uiState.update { it.copy(martingalePlan = plan) }
    }

    fun recordMartingaleWin() {
        val plan = _uiState.value.martingalePlan ?: return
        val currentStep = _uiState.value.liveMartingaleStep
        val stepInfo = plan.steps.firstOrNull { it.step == currentStep }
        val net = stepInfo?.netProfit ?: 0.0

        _uiState.update {
            it.copy(
                liveMartingaleStep = 1, // Reset to Level 1 on win!
                sessionProfit = it.sessionProfit + net,
                sessionWins = it.sessionWins + 1
            )
        }
    }

    fun recordMartingaleLoss() {
        val plan = _uiState.value.martingalePlan ?: return
        val currentStep = _uiState.value.liveMartingaleStep
        val stepInfo = plan.steps.firstOrNull { it.step == currentStep }
        val bet = stepInfo?.betAmount ?: 0.0

        val nextStep = if (currentStep < 3) currentStep + 1 else 1
        _uiState.update {
            it.copy(
                liveMartingaleStep = nextStep,
                sessionProfit = it.sessionProfit - bet,
                sessionLosses = it.sessionLosses + 1
            )
        }
    }

    fun resetSessionTracker() {
        _uiState.update {
            it.copy(
                liveMartingaleStep = 1,
                sessionProfit = 0.0,
                sessionWins = 0,
                sessionLosses = 0
            )
        }
    }

    fun addManualResult(periodId: String, number: Int) {
        viewModelScope.launch {
            repository.insertGameResult(periodId, number)
            refreshPrediction()
        }
    }

    fun updateSyncSettings(
        token: String? = null,
        url: String? = null,
        interval: Int? = null,
        autoSync: Boolean? = null
    ) {
        _uiState.update { current ->
            current.copy(
                authToken = token ?: current.authToken,
                targetApiUrl = url ?: current.targetApiUrl,
                syncIntervalSeconds = interval ?: current.syncIntervalSeconds,
                isAutoSyncEnabled = autoSync ?: current.isAutoSyncEnabled
            )
        }

        if (autoSync == true) {
            startAutoSyncWorker()
        } else if (autoSync == false) {
            syncJob?.cancel()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.fetchAndSyncRemote(
                targetUrl = _uiState.value.targetApiUrl,
                bearerToken = _uiState.value.authToken,
                limit = 30
            )

            result.fold(
                onSuccess = { count ->
                    val time = periodSync.getCurrentBdTimeStr()
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "সফলভাবে $count টি রেকর্ড সিঙ্ক হয়েছে ($time BST) / Synced $count records"
                        )
                    }
                    refreshPrediction()
                },
                onFailure = { error ->
                    val time = periodSync.getCurrentBdTimeStr()
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncStatus = "সিঙ্ক ত্রুটি: ${error.localizedMessage} ($time BST)"
                        )
                    }
                }
            )
        }
    }

    private fun startAutoSyncWorker() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            while (isActive && _uiState.value.isAutoSyncEnabled) {
                triggerSync()
                delay((_uiState.value.syncIntervalSeconds * 1000L).coerceAtLeast(10000L))
            }
        }
    }

    fun resetLocalDatabase() {
        viewModelScope.launch {
            repository.clearAll()
            repository.seedInitialDataIfEmpty()
            refreshPrediction()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        syncJob?.cancel()
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(application) as T
                }
            }
    }
}
