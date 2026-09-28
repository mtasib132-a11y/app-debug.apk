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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameHistoryEntity
import com.example.model.HistoryStats
import com.example.ui.components.ColorBadge
import com.example.ui.components.D3TrendChartCard
import com.example.ui.components.NumberBall
import com.example.ui.components.SizeBadge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RubyLight
import com.example.ui.theme.RubySecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HistoryStatsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterSize by remember { mutableStateOf("ALL") }
    var showManualAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(uiState.historyList, searchQuery, filterSize) {
        uiState.historyList.filter { item ->
            val matchesSearch = searchQuery.isBlank() || item.periodId.contains(searchQuery.trim())
            val matchesFilter = when (filterSize) {
                "BIG" -> item.size == "BIG"
                "SMALL" -> item.size == "SMALL"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analytics",
                        tint = EmeraldLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (uiState.isBengali) "হিস্ট্রি ও ট্রেন্ড অ্যানালিটিক্স" else "Game History & Trend Analytics",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isBengali) "বিগত রাউন্ডগুলোর পরিসংখ্যান ও সংখ্যা বণ্টন" else "Historical distribution & outcome frequencies",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // D3.js Historical Number Trend & BIG vs SMALL Frequency Chart
            item {
                D3TrendChartCard(
                    historyList = uiState.historyList,
                    isBengali = uiState.isBengali
                )
            }

            // Stats Cards
            item {
                HistoryStatsOverview(stats = uiState.stats, isBengali = uiState.isBengali)
            }

            // Number Distribution (0-9)
            item {
                NumberDistributionCard(
                    distribution = uiState.stats.numberDistribution,
                    totalRounds = uiState.stats.totalAnalyzedRounds,
                    isBengali = uiState.isBengali
                )
            }

            // Recent 15 Rounds Bead Matrix
            item {
                RecentBeadsRow(
                    recentItems = uiState.historyList.take(15),
                    isBengali = uiState.isBengali
                )
            }

            // Search & Filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (uiState.isBengali) "পিরিয়ড আইডি দিয়ে খুঁজুন..." else "Search by Period ID...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_history_search"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder
                        ),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "BIG", "SMALL").forEach { filter ->
                            val isSelected = filterSize == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { filterSize = filter },
                                label = {
                                    Text(
                                        text = if (uiState.isBengali) {
                                            when (filter) {
                                                "BIG" -> "বড় (BIG)"
                                                "SMALL" -> "ছোট (SMALL)"
                                                else -> "সকল (ALL)"
                                            }
                                        } else filter
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = EmeraldLight,
                                    containerColor = DarkSurfaceElevated,
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

            // History Records Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isBengali) "রেকর্ড তালিকা (${filteredList.size})" else "History Records (${filteredList.size})",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // History List Items
            items(filteredList, key = { it.periodId }) { item ->
                HistoryRecordRow(item = item)
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to add manual round
        FloatingActionButton(
            onClick = { showManualAddDialog = true },
            containerColor = EmeraldPrimary,
            contentColor = DarkBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_result")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Result")
        }
    }

    if (showManualAddDialog) {
        ManualAddResultDialog(
            defaultPeriodId = uiState.livePeriodInfo.currentPeriodId,
            onDismiss = { showManualAddDialog = false },
            onConfirm = { period, num ->
                viewModel.addManualResult(period, num)
                showManualAddDialog = false
            },
            isBengali = uiState.isBengali
        )
    }
}

@Composable
fun HistoryStatsOverview(stats: HistoryStats, isBengali: Boolean) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBengali) "মোট পর্যালোচিত রাউন্ড" else "Analyzed Sample Window",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${stats.totalAnalyzedRounds} Rounds",
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Big vs Small Ratio Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BIG: ${stats.bigCount} (${String.format("%.1f", stats.bigPercentage)}%)",
                    color = EmeraldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SMALL: ${stats.smallCount} (${String.format("%.1f", stats.smallPercentage)}%)",
                    color = RubyLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dual colored ratio bar
            val bigWeight = stats.bigPercentage.toFloat().coerceIn(1f, 99f)
            val smallWeight = (100f - bigWeight).coerceIn(1f, 99f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(bigWeight)
                        .fillMaxSize()
                        .background(EmeraldPrimary)
                )
                Box(
                    modifier = Modifier
                        .weight(smallWeight)
                        .fillMaxSize()
                        .background(RubySecondary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color breakdown chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "GREEN", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${String.format("%.1f", stats.greenPercentage)}%", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "RED", color = RubyLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${String.format("%.1f", stats.redPercentage)}%", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "VIOLET (0,5)", color = VioletAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${String.format("%.1f", stats.violetPercentage)}%", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun NumberDistributionCard(
    distribution: Map<Int, Int>,
    totalRounds: Int,
    isBengali: Boolean
) {
    val maxCount = (distribution.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "সংখ্যার পুনরাবৃত্তি বণ্টন (০ থেকে ৯)" else "Number Frequency (0 to 9)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                (0..9).forEach { num ->
                    val count = distribution[num] ?: 0
                    val heightRatio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.1f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$count",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((heightRatio * 50).dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (num >= 5) EmeraldPrimary else RubySecondary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        NumberBall(
                            number = num,
                            size = 24.dp,
                            textSize = 10
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentBeadsRow(
    recentItems: List<GameHistoryEntity>,
    isBengali: Boolean
) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "সাম্প্রতিক ফলাফল ক্রম (Recent Beads)" else "Recent Outcome Beads",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentItems) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NumberBall(number = item.number, size = 34.dp, textSize = 13)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (item.size == "BIG") "B" else "S",
                            color = if (item.size == "BIG") EmeraldLight else RubyLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRecordRow(
    item: GameHistoryEntity,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumberBall(number = item.number, size = 36.dp, textSize = 15)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.periodId,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.createdAt,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SizeBadge(size = item.size)
                ColorBadge(color = item.color)
            }
        }
    }
}
