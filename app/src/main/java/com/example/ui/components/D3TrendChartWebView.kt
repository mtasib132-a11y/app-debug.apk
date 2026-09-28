package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.GameHistoryEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3TrendChartCard(
    historyList: List<GameHistoryEntity>,
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    chartHeight: Int = 410
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }

    // Take up to last 100 rounds and reverse to oldest->newest for left-to-right chronological line
    val chronological100 = remember(historyList) {
        val window = historyList.take(100)
        window.reversed()
    }

    val jsonPayload = remember(chronological100) {
        chronological100.joinToString(prefix = "[", postfix = "]") { item ->
            val safePeriod = item.periodId.replace("\"", "")
            val safeSize = item.size.replace("\"", "")
            val safeColor = item.color.replace("\"", "")
            """{"periodId":"$safePeriod","number":${item.number},"size":"$safeSize","color":"$safeColor"}"""
        }
    }

    // Push data to WebView whenever data changes and page is loaded
    LaunchedEffect(jsonPayload, isPageLoaded, webViewRef) {
        val wv = webViewRef
        if (wv != null && isPageLoaded && jsonPayload.length > 2) {
            val script = "if (window.updateDataFromAndroid) { window.updateDataFromAndroid($jsonPayload); }"
            wv.evaluateJavascript(script, null)
        }
    }

    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("d3_trend_chart_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Chart",
                        tint = EmeraldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isBengali) "D3.js ট্রেন্ড ও BIG/SMALL রেশিও চার্ট" else "D3.js Number Trend & Outcome Frequency",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBengali) "বিগত ১০০ রাউন্ডের লাইভ ইন্টারেক্টিভ বিশ্লেষণ" else "Interactive analysis over the last 100 rounds",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        webViewRef?.evaluateJavascript(
                            "if (window.updateDataFromAndroid) { window.updateDataFromAndroid($jsonPayload); }",
                            null
                        )
                    },
                    modifier = Modifier.size(32.dp).testTag("btn_refresh_d3_chart")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh D3 Chart",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // WebView Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(Color.parseColor("#0B1120"))

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isPageLoaded = true
                                    if (jsonPayload.length > 2) {
                                        val script = "if (window.updateDataFromAndroid) { window.updateDataFromAndroid($jsonPayload); }"
                                        view?.evaluateJavascript(script, null)
                                    }
                                }
                            }
                            webChromeClient = WebChromeClient()

                            loadUrl("file:///android_asset/d3_chart.html")
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        webViewRef = view
                        if (isPageLoaded && jsonPayload.length > 2) {
                            val script = "if (window.updateDataFromAndroid) { window.updateDataFromAndroid($jsonPayload); }"
                            view.evaluateJavascript(script, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(chartHeight.dp)
                )
            }
        }
    }
}
