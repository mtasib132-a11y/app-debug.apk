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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
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
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsSyncScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var tokenInput by remember(uiState.authToken) { mutableStateOf(uiState.authToken) }
    var urlInput by remember(uiState.targetApiUrl) { mutableStateOf(uiState.targetApiUrl) }
    var isTokenVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = EmeraldLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (uiState.isBengali) "লাইভ সিঙ্ক ও কনফিগারেশন" else "Live Data Sync & Platform Settings",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.isBengali) "hgnice0.com এপিআই ও ব্যাকগ্রাউন্ড সিঙ্ক সেটিংস" else "API credentials & real-time background worker",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Live Sync Status Card
        SyncStatusCard(
            isAutoSyncEnabled = uiState.isAutoSyncEnabled,
            isSyncing = uiState.isSyncing,
            lastStatus = uiState.lastSyncStatus,
            syncInterval = uiState.syncIntervalSeconds,
            isBengali = uiState.isBengali,
            onManualSync = { viewModel.triggerSync() }
        )

        // API Credentials Section
        Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = if (uiState.isBengali) "এপিআই অথরাইজেশন ও টার্গেট URL" else "API Endpoints & Bearer Token",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Target URL
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(if (uiState.isBengali) "টার্গেট API URL" else "Target API URL") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = "URL", tint = EmeraldLight)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_api_url"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    singleLine = true
                )

                // Auth Token
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text(if (uiState.isBengali) "Bearer অথরাইজেশন টোকেন" else "Bearer Authorization Token") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Token", tint = EmeraldLight)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                            Icon(
                                imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("input_auth_token"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    singleLine = true
                )

                // Save Credentials Button
                Button(
                    onClick = { viewModel.updateSyncSettings(token = tokenInput, url = urlInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_credentials")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (uiState.isBengali) "টোকেন ও URL আপডেট করুন" else "Save Token & URL")
                }
            }
        }

        // Auto-Sync Settings
        Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (uiState.isBengali) "স্বয়ংক্রিয় লাইভ সিঙ্ক" else "Automatic Live Sync",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isBengali) "প্রতি রাউন্ড শেষ হওয়ার সাথে সাথে ডাটা সিঙ্ক" else "Fetch live outcomes periodically",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = uiState.isAutoSyncEnabled,
                        onCheckedChange = { viewModel.updateSyncSettings(autoSync = it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldLight,
                            checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("switch_auto_sync")
                    )
                }

                Text(
                    text = if (uiState.isBengali) "সিঙ্ক ইন্টারভ্যাল (Sync Interval):" else "Sync Interval:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 15, 30, 60).forEach { interval ->
                        val isSelected = uiState.syncIntervalSeconds == interval
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSyncSettings(interval = interval) },
                            label = { Text("${interval}s") },
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

        // Language & Tools
        Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (uiState.isBengali) "অ্যাপ ল্যাঙ্গুয়েজ ও ডাটাবেজ টুলস" else "Language & Local Database",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Language Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Language", tint = EmeraldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isBengali) "বাংলা ভাষা সক্রিয়" else "Bengali Language Mode",
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(text = if (uiState.isBengali) "English এ যান" else "বাংলায় দেখুন")
                    }
                }

                // Reset / Seed Buttons
                OutlinedButton(
                    onClick = { viewModel.resetLocalDatabase() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Seed")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (uiState.isBengali) "৩৫টি নমুনা রাউন্ড ডাটা পুনরায় লোড করুন" else "Reload 35 Sample Historical Rounds")
                }
            }
        }

        // Algorithm Methodology Info Card
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (uiState.isBengali) "💡 ৪-ইন্ডিকেটর প্রেডিকশন ইঞ্জিন কৌশল:" else "💡 4-Indicator Engine Methodology:",
                    color = EmeraldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.isBengali) {
                        "১. মোমেন্টাম (EMA): সাম্প্রতিক ফলাফলগুলোকে এক্সপোনেনশিয়াল ওয়েট দিয়ে ড্রাফট নির্ধারণ করে।\n" +
                        "২. মারকোভ চেইন: বিগত দুটি রাউন্ডের ট্রানজিশন প্রোবাবিলিটি ম্যাট্রিক্স বিশ্লেষণ।\n" +
                        "৩. স্ট্রিক ট্রেন্ড: চলমান ট্রেন্ডের ধারাবাহিকতা মূল্যায়ন (৫টির বেশি হলে কুলডাউন)।\n" +
                        "৪. প্যাটার্ন ম্যাচিং: ৪-স্টেপ সিকোয়েন্স ইতিহাসের সাথে হুবহু ম্যাচ করে প্রেডিকশন।\n" +
                        "ভোটিং নিয়ম: ৪টির মধ্যে কমপক্ষে ৩টি ইন্ডিকেটর একমত হলেই কেবল ট্রেড সিগন্যাল কার্যকর হয়।"
                    } else {
                        "1. Momentum (EMA): Exponential recency weighting of historical outcomes.\n" +
                        "2. 2nd-Order Markov Chain: Two-state condition transition probability table.\n" +
                        "3. Streak Trend: Trend-following filter (exhaustion pause if streak >= 5).\n" +
                        "4. Pattern Matching: 4-step sequence sliding window matcher.\n" +
                        "Voting Consensus: At least 3/4 indicators must agree before issuing a TRADE signal."
                    },
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SyncStatusCard(
    isAutoSyncEnabled: Boolean,
    isSyncing: Boolean,
    lastStatus: String?,
    syncInterval: Int,
    isBengali: Boolean,
    onManualSync: () -> Unit
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isAutoSyncEnabled) EmeraldPrimary else RubySecondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAutoSyncEnabled) {
                            if (isBengali) "সিঙ্ক স্ট্যাটাস: অ্যাক্টিভ (${syncInterval}s)" else "Sync Status: ACTIVE (${syncInterval}s)"
                        } else {
                            if (isBengali) "সিঙ্ক স্ট্যাটাস: বন্ধ (PAUSED)" else "Sync Status: PAUSED"
                        },
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onManualSync,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .testTag("btn_manual_sync_now")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = EmeraldLight,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isBengali) "সিঙ্ক নাও" else "Sync Now", color = EmeraldLight, fontSize = 12.sp)
                    }
                }
            }

            if (!lastStatus.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lastStatus,
                        color = if (lastStatus.contains("ত্রুটি") || lastStatus.contains("Error")) RubyLight else EmeraldLight,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
