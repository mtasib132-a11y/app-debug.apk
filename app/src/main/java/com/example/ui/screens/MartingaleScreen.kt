package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MartingalePlan
import com.example.model.MartingaleStep
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MartingaleScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val plan = uiState.martingalePlan

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Shield",
                tint = EmeraldLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (uiState.isBengali) "৩-স্টেপ মার্টিংগেল মানি ম্যানেজমেন্ট" else "3-Step Martingale Risk Management",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.isBengali) "ক্যাপিটাল প্রোটেকশন ও রিকভারি ক্যালকুলেটর" else "Capital Protection & Recovery Calculator",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Input Card
        Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Total Balance Input
                OutlinedTextField(
                    value = uiState.balanceInput,
                    onValueChange = { viewModel.updateMartingaleInputs(balance = it) },
                    label = { Text(if (uiState.isBengali) "টোটাল ব্যালেন্স (BDT/টাকা)" else "Total Capital Balance") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Wallet", tint = EmeraldLight)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_total_balance"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    singleLine = true
                )

                // Base Bet Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.baseBetInput,
                        onValueChange = { viewModel.updateMartingaleInputs(baseBet = it) },
                        label = { Text(if (uiState.isBengali) "বেস বেট (ঐচ্ছিক)" else "Base Bet (Optional)") },
                        placeholder = { Text(plan?.recommendedBaseBet?.toString() ?: "Auto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_base_bet"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder
                        ),
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = { viewModel.updateMartingaleInputs(baseBet = "") },
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(if (uiState.isBengali) "অটো সেফ" else "Auto Safe")
                    }
                }

                // Multiplier Chips
                Text(
                    text = if (uiState.isBengali) "মাল্টিপ্লায়ার (Multiplier):" else "Bet Multiplier:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1.5, 2.0, 2.2, 2.5).forEach { mult ->
                        val isSelected = uiState.multiplier == mult
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateMartingaleInputs(multiplier = mult) },
                            label = { Text("${mult}x") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = EmeraldLight,
                                containerColor = DarkSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) EmeraldPrimary else DarkBorder
                            )
                        )
                    }
                }
            }
        }

        // Risk Assessment Summary Card
        if (plan != null) {
            RiskStatusCard(plan = plan, isBengali = uiState.isBengali)
        }

        // Live Step Assistant Controller
        LiveMartingaleAssistant(
            currentStep = uiState.liveMartingaleStep,
            plan = plan,
            isBengali = uiState.isBengali,
            onWin = { viewModel.recordMartingaleWin() },
            onLoss = { viewModel.recordMartingaleLoss() }
        )

        // 3-Level Steps Breakdown
        if (plan != null) {
            Text(
                text = if (uiState.isBengali) "৩-ধাপের প্ল্যান বিবরণ" else "3-Level Step Breakdown",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            plan.steps.forEach { step ->
                MartingaleStepCard(
                    step = step,
                    isCurrent = step.step == uiState.liveMartingaleStep,
                    isBengali = uiState.isBengali
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RiskStatusCard(plan: MartingalePlan, isBengali: Boolean) {
    val isSafe = plan.isSufficient
    val borderColor = if (isSafe) EmeraldPrimary else RubySecondary
    val bgColor = if (isSafe) EmeraldDark.copy(alpha = 0.25f) else RubyDark.copy(alpha = 0.25f)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = "Status",
                    tint = if (isSafe) EmeraldLight else RubyLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSafe) {
                        if (isBengali) "ক্যাপিটাল স্ট্যাটাস: নিরাপদ (SAFE)" else "CAPITAL STATUS: SAFE"
                    } else {
                        if (isBengali) "ক্যাপিটাল স্ট্যাটাস: ঝুঁকি (INSUFFICIENT)" else "CAPITAL STATUS: AT RISK"
                    },
                    color = if (isSafe) EmeraldLight else RubyLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isBengali) "৩ ধাপে প্রয়োজনীয় পুঁজি" else "Total 3-Step Capital Required",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format("%.2f", plan.totalCapitalRequired)} BDT",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isBengali) "সুপারিশকৃত নিরাপদ বেস" else "Recommended Safe Base",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format("%.2f", plan.recommendedBaseBet)} BDT",
                        color = EmeraldLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isBengali) plan.safetyNoteBn else plan.safetyNoteEn,
                color = if (isSafe) EmeraldLight.copy(alpha = 0.9f) else RubyLight.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun LiveMartingaleAssistant(
    currentStep: Int,
    plan: MartingalePlan?,
    isBengali: Boolean,
    onWin: () -> Unit,
    onLoss: () -> Unit
) {
    val currentStepData = plan?.steps?.firstOrNull { it.step == currentStep }

    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBengali) "লাইভ ট্রেডিং অ্যাসিস্ট্যান্ট (মার্টিংগেল গাইড)" else "Live Martingale Trading Assistant",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Step Indicator Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..3).forEach { stepNum ->
                    val isActive = stepNum == currentStep
                    val isPast = stepNum < currentStep

                    val circleBg = when {
                        isActive -> EmeraldPrimary
                        isPast -> RubySecondary.copy(alpha = 0.6f)
                        else -> DarkSurface
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(circleBg)
                                .border(1.dp, if (isActive) Color.White else DarkBorder, CircleShape)
                        ) {
                            Text(
                                text = "L$stepNum",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stepNum == 1) "Base" else "Recover",
                            color = if (isActive) EmeraldLight else TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    if (stepNum < 3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .padding(horizontal = 8.dp)
                                .background(if (isPast) RubySecondary else DarkBorder)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action for Current Step
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBengali) "বর্তমান সক্রিয় লেভেল: Level $currentStep" else "Active Level: Level $currentStep",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isBengali) "ট্রেড অ্যামাউন্ট: ${currentStepData?.betAmount ?: 0.0} BDT" else "Place Bet: ${currentStepData?.betAmount ?: 0.0} BDT",
                            color = EmeraldLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isBengali) "জিতলে নিট প্রফিট:" else "Net Win Profit:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+${currentStepData?.netProfit ?: 0.0} BDT",
                            color = EmeraldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onWin,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_martingale_won")
                ) {
                    Text(text = if (isBengali) "উইন (রিসেট L1) 🏆" else "Won (Reset L1)")
                }

                Button(
                    onClick = onLoss,
                    colors = ButtonDefaults.buttonColors(containerColor = RubySecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_martingale_lost")
                ) {
                    Text(text = if (isBengali) "লস (পরের ধাপ) ➡️" else "Lost (Next Level)")
                }
            }
        }
    }
}

@Composable
fun MartingaleStepCard(
    step: MartingaleStep,
    isCurrent: Boolean,
    isBengali: Boolean
) {
    Surface(
        color = if (isCurrent) EmeraldDark.copy(alpha = 0.25f) else DarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCurrent) EmeraldPrimary else DarkBorder,
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) EmeraldPrimary else DarkSurface)
                        .border(1.dp, if (isCurrent) Color.White else DarkBorder, CircleShape)
                ) {
                    Text(
                        text = "${step.step}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isBengali) step.label else "Level ${step.step}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBengali) "বেট: ${step.betAmount} BDT" else "Bet: ${step.betAmount} BDT",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isBengali) "উইন রিটার্ন: ${step.winReturn}" else "Payout: ${step.winReturn}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = if (isBengali) "নিট লাভ: +${step.netProfit} BDT" else "Net: +${step.netProfit} BDT",
                    color = EmeraldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
