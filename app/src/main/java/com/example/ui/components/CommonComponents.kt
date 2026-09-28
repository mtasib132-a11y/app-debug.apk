package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WinGoMode
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RubyDark
import com.example.ui.theme.RubyLight
import com.example.ui.theme.RubySecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletDark

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    textSize: Int = 18
) {
    val backgroundBrush = when (number) {
        0 -> Brush.linearGradient(listOf(RubySecondary, VioletAccent))
        5 -> Brush.linearGradient(listOf(EmeraldPrimary, VioletAccent))
        1, 3, 7, 9 -> Brush.verticalGradient(listOf(EmeraldLight, EmeraldPrimary, EmeraldDark))
        else -> Brush.verticalGradient(listOf(RubyLight, RubySecondary, RubyDark))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .testTag("number_ball_$number")
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = textSize.sp
        )
    }
}

@Composable
fun SizeBadge(
    size: String,
    modifier: Modifier = Modifier
) {
    val isBig = size.equals("BIG", ignoreCase = true)
    val bgColor = if (isBig) EmeraldPrimary.copy(alpha = 0.2f) else RubySecondary.copy(alpha = 0.2f)
    val textColor = if (isBig) EmeraldLight else RubyLight
    val borderColor = if (isBig) EmeraldPrimary.copy(alpha = 0.6f) else RubySecondary.copy(alpha = 0.6f)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(6.dp))
    ) {
        Text(
            text = size.uppercase(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ColorBadge(
    color: String,
    modifier: Modifier = Modifier
) {
    val chipColor = when {
        color.contains("GREEN") && color.contains("VIOLET") -> VioletAccent
        color.contains("RED") && color.contains("VIOLET") -> VioletAccent
        color.contains("GREEN") -> EmeraldPrimary
        else -> RubySecondary
    }

    Surface(
        color = chipColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.border(1.dp, chipColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
    ) {
        Text(
            text = color,
            color = chipColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun BangladeshTimeBar(
    serverTimeBd: String,
    modifier: Modifier = Modifier,
    isBengali: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

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
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "বাংলাদেশ সময় (BST UTC+6)" else "Bangladesh Time (BST UTC+6)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Clock",
                    tint = EmeraldLight,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = serverTimeBd.ifBlank { "--:--:--" },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WinGoModeSelector(
    selectedMode: WinGoMode,
    onModeSelected: (WinGoMode) -> Unit,
    modifier: Modifier = Modifier,
    isBengali: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WinGoMode.values().forEach { mode ->
            val isSelected = mode == selectedMode
            FilterChip(
                selected = isSelected,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        text = if (isBengali) {
                            if (mode == WinGoMode.ONE_MINUTE) "উইনগো ১ মিনিট (60s)" else "উইনগো ৩০ সেকেন্ড (30s)"
                        } else {
                            mode.title
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
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
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_chip_${mode.name}")
            )
        }
    }
}

@Composable
fun PeriodCountdownCard(
    periodId: String,
    remainingSeconds: Int,
    totalDuration: Int,
    modifier: Modifier = Modifier,
    isBengali: Boolean = false
) {
    val progress = (remainingSeconds.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    val isUrgent = remainingSeconds <= 8

    val timerColor = when {
        remainingSeconds <= 5 -> RubySecondary
        remainingSeconds <= 10 -> AmberDark
        else -> EmeraldPrimary
    }

    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isUrgent) RubySecondary.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBengali) "চলতি পিরিয়ড আইডি" else "Active Period ID",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = periodId.ifBlank { "লোডিং..." },
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBengali) "লাইভ সিগন্যাল অ্যালগরিদম সক্রিয়" else "Real-time sync engine active",
                    color = EmeraldLight,
                    fontSize = 11.sp
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(70.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(70.dp),
                    color = timerColor,
                    strokeWidth = 6.dp,
                    trackColor = DarkBorder
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${remainingSeconds}s",
                        color = if (isUrgent) RubyLight else TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isBengali) "বাকি" else "left",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
