package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IndicatorVote
import com.example.model.PredictionResult
import com.example.ui.components.BangladeshTimeBar
import com.example.ui.components.NumberBall
import com.example.ui.components.PeriodCountdownCard
import com.example.ui.components.WinGoModeSelector
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RubyDark
import com.example.ui.theme.RubyLight
import com.example.ui.theme.RubySecondary
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LiveSignalScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showManualAddDialog by remember { mutableStateOf(false) }
    var showIndicatorsExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Bangladesh Time Bar
        BangladeshTimeBar(
            serverTimeBd = uiState.livePeriodInfo.serverTimeBd,
            isBengali = uiState.isBengali
        )

        // 2. WinGo Mode Selector (1M vs 30S)
        WinGoModeSelector(
            selectedMode = uiState.selectedMode,
            onModeSelected = { viewModel.setMode(it) },
            isBengali = uiState.isBengali
        )

        // 3. Countdown & Period ID Card
        PeriodCountdownCard(
            periodId = uiState.livePeriodInfo.currentPeriodId,
            remainingSeconds = uiState.livePeriodInfo.remainingSeconds,
            totalDuration = uiState.livePeriodInfo.totalDurationSeconds,
            isBengali = uiState.isBengali
        )

        // 4. Main AI Prediction Card
        PredictionMasterCard(
            prediction = uiState.predictionResult,
            isBengali = uiState.isBengali,
            onRefresh = { viewModel.refreshPrediction() }
        )

        // 5. 4-Indicator Voting Breakdown Card
        IndicatorBreakdownCard(
            votes = uiState.predictionResult.votes,
            consensusScore = uiState.predictionResult.consensusScore,
            isExpanded = showIndicatorsExpanded,
            onToggleExpand = { showIndicatorsExpanded = !showIndicatorsExpanded },
            isBengali = uiState.isBengali
        )

        // 6. Session Live Win/Loss Tracker
        SessionTrackerCard(
            wins = uiState.sessionWins,
            losses = uiState.sessionLosses,
            profit = uiState.sessionProfit,
            onRecordWin = { viewModel.recordMartingaleWin() },
            onRecordLoss = { viewModel.recordMartingaleLoss() },
            onReset = { viewModel.resetSessionTracker() },
            isBengali = uiState.isBengali
        )

        // 7. Manual Result Submission
        Button(
            onClick = { showManualAddDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurfaceElevated,
                contentColor = EmeraldLight
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .testTag("btn_manual_add_result")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (uiState.isBengali) "+ শেষ রাউন্ডের ফলাফল ম্যানুয়ালি যোগ করুন" else "+ Add Last Round Result Manually",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showManualAddDialog) {
        ManualAddResultDialog(
            defaultPeriodId = uiState.livePeriodInfo.currentPeriodId,
            onDismiss = { showManualAddDialog = false },
            onConfirm = { period, number ->
                viewModel.addManualResult(period, number)
                showManualAddDialog = false
            },
            isBengali = uiState.isBengali
        )
    }
}

@Composable
fun PredictionMasterCard(
    prediction: PredictionResult,
    isBengali: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTrade = prediction.action == "TRADE" && prediction.predictedSize != null
    val isBig = prediction.predictedSize == "BIG"

    val cardBorderColor = when {
        isTrade && isBig -> EmeraldPrimary
        isTrade && !isBig -> RubySecondary
        else -> AmberGold.copy(alpha = 0.6f)
    }

    val cardBgBrush = when {
        isTrade && isBig -> Brush.verticalGradient(listOf(DarkSurface, EmeraldDark.copy(alpha = 0.35f)))
        isTrade && !isBig -> Brush.verticalGradient(listOf(DarkSurface, RubyDark.copy(alpha = 0.35f)))
        else -> Brush.verticalGradient(listOf(DarkSurface, AmberDark.copy(alpha = 0.25f)))
    }

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(cardBgBrush, RoundedCornerShape(20.dp))
            .border(1.5.dp, cardBorderColor, RoundedCornerShape(20.dp))
            .testTag("prediction_master_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title & Consensus Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isTrade) EmeraldPrimary else AmberGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "আল্ট্রা-একুরেসি ৪-ইন্ডিকেটর সিগন্যাল" else "Ultra 4-Indicator Signal",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp).testTag("btn_refresh_prediction")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isTrade) {
                // Signal Main Banner (BIG or SMALL)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isBig) EmeraldPrimary.copy(alpha = 0.18f) else RubySecondary.copy(alpha = 0.18f))
                        .border(
                            1.dp,
                            if (isBig) EmeraldPrimary.copy(alpha = 0.5f) else RubySecondary.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            color = if (isBig) EmeraldPrimary else RubySecondary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isBengali) "ট্রেড নির্দেশ: নিশ্চিত সিগন্যাল" else "RECOMMENDED ACTION: TRADE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBig) "BIG (৫-৯)" else "SMALL (০-৪)",
                            color = if (isBig) EmeraldLight else RubyLight,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isBengali) {
                                if (isBig) "সম্ভাব্য সংখ্যা: 5, 6, 7, 8, 9" else "সম্ভাব্য সংখ্যা: 0, 1, 2, 3, 4"
                            } else {
                                if (isBig) "Potential Numbers: 5, 6, 7, 8, 9" else "Potential Numbers: 0, 1, 2, 3, 4"
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Predicted Color Orb
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isBig) EmeraldPrimary else RubySecondary)
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Text(
                                text = if (isBig) "G" else "R",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBig) "GREEN" else "RED",
                            color = if (isBig) EmeraldLight else RubyLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Confidence Bar & Consensus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBengali) "নিশ্চয়তা (Confidence)" else "Confidence Rating",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${String.format("%.1f", prediction.confidence)}%",
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (prediction.confidence / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldPrimary,
                    trackColor = DarkBorder
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBengali) "ইন্ডিকেটর কনসেন্সাস স্কোর" else "Consensus Agreement",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Surface(
                        color = EmeraldDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = prediction.consensusScore,
                            color = EmeraldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                // SKIP / WAIT CARD
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AmberDark.copy(alpha = 0.25f))
                        .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Skip",
                        tint = AmberGold,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) "স্কিপ করুন (SKIP / WAIT)" else "SKIP ROUND / LOW CONSENSUS",
                        color = AmberGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = prediction.message ?: if (isBengali) {
                            "ইন্ডিকেটরগুলোর মধ্যে মতৈক্য নেই (${prediction.bigVotes} BIG বনাম ${prediction.smallVotes} SMALL)। ঝুঁকির কারণে এই রাউন্ড স্কিপ করুন।"
                        } else {
                            "Indicators disagree (${prediction.bigVotes} BIG vs ${prediction.smallVotes} SMALL). Risk management dictates skipping this round."
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun IndicatorBreakdownCard(
    votes: List<IndicatorVote>,
    consensusScore: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBengali) "৪-ইন্ডিকেটর ভোটিং বিস্তারিত" else "4-Indicator Voting Breakdown",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBengali) "ঐকমত্য: $consensusScore" else "Agreement: $consensusScore",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    votes.forEach { vote ->
                        IndicatorItemRow(vote = vote, isBengali = isBengali)
                    }
                }
            }
        }
    }
}

@Composable
fun IndicatorItemRow(
    vote: IndicatorVote,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val pred = vote.prediction
    val isBig = pred == "BIG"
    val isSmall = pred == "SMALL"

    val badgeColor = when {
        isBig -> EmeraldPrimary
        isSmall -> RubySecondary
        else -> TextDisabled
    }

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBengali) vote.nameBn else vote.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = vote.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = badgeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.border(1.dp, badgeColor, RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = pred ?: (if (isBengali) "নিউট্রাল" else "NEUTRAL"),
                    color = if (pred != null) badgeColor else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SessionTrackerCard(
    wins: Int,
    losses: Int,
    profit: Double,
    onRecordWin: () -> Unit,
    onRecordLoss: () -> Unit,
    onReset: () -> Unit,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val totalRounds = wins + losses
    val winRate = if (totalRounds > 0) (wins.toDouble() / totalRounds) * 100.0 else 0.0

    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "লাইভ ট্রেড ট্র্যাকার ও একুরেসি" else "Live Session Tracker",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onReset) {
                    Text(
                        text = if (isBengali) "রিসেট" else "Reset",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isBengali) "উইন রেট" else "Win Rate",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format("%.1f", winRate)}%",
                        color = if (winRate >= 50.0) EmeraldLight else RubyLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = if (isBengali) "জয় / পরাজয়" else "Win / Loss",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$wins W - $losses L",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = if (isBengali) "সেশন লাভ/ক্ষতি" else "P&L (Est.)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${if (profit >= 0) "+" else ""}${String.format("%.1f", profit)}",
                        color = if (profit >= 0) EmeraldLight else RubyLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRecordWin,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_record_win")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Won")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBengali) "উইন হয়েছে ✅" else "Won Round")
                }

                Button(
                    onClick = onRecordLoss,
                    colors = ButtonDefaults.buttonColors(containerColor = RubySecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_record_loss")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Lost")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBengali) "লস হয়েছে ❌" else "Lost Round")
                }
            }
        }
    }
}

@Composable
fun ManualAddResultDialog(
    defaultPeriodId: String,
    onDismiss: () -> Unit,
    onConfirm: (periodId: String, number: Int) -> Unit,
    isBengali: Boolean
) {
    var periodInput by remember { mutableStateOf(defaultPeriodId) }
    var selectedNumber by remember { mutableStateOf(7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBengali) "রাউন্ডের ফলাফল ইনপুট করুন" else "Input Drawn Result",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = periodInput,
                    onValueChange = { periodInput = it },
                    label = { Text(if (isBengali) "পিরিয়ড আইডি" else "Period ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = if (isBengali) "ড্র হওয়া সংখ্যা সিলেক্ট করুন (০-৯):" else "Select Drawn Number (0-9):",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0..4).forEach { num ->
                        NumberBall(
                            number = num,
                            modifier = Modifier
                                .clickable { selectedNumber = num }
                                .then(
                                    if (selectedNumber == num) {
                                        Modifier.border(2.5.dp, Color.White, CircleShape)
                                    } else Modifier
                                ),
                            size = 38.dp,
                            textSize = 15
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (5..9).forEach { num ->
                        NumberBall(
                            number = num,
                            modifier = Modifier
                                .clickable { selectedNumber = num }
                                .then(
                                    if (selectedNumber == num) {
                                        Modifier.border(2.5.dp, Color.White, CircleShape)
                                    } else Modifier
                                ),
                            size = 38.dp,
                            textSize = 15
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (periodInput.isNotBlank()) {
                        onConfirm(periodInput, selectedNumber)
                    }
                }
            ) {
                Text(if (isBengali) "সংরক্ষণ করুন" else "Save Result")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        },
        containerColor = DarkSurfaceElevated
    )
}
