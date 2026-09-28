package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class HgzyHistoryRequest(
    @Json(name = "typeId") val typeId: Int = 1,
    @Json(name = "pageNo") val pageNo: Int = 1,
    @Json(name = "pageSize") val pageSize: Int = 30
)

@JsonClass(generateAdapter = true)
data class HgzyHistoryItem(
    @Json(name = "issueNumber") val issueNumber: String?,
    @Json(name = "number") val number: String?,
    @Json(name = "colour") val colour: String? = null,
    @Json(name = "openTime") val openTime: String? = null
)

@JsonClass(generateAdapter = true)
data class HgzyDataWrapper(
    @Json(name = "list") val list: List<HgzyHistoryItem>? = null,
    @Json(name = "total") val total: Int? = null
)

@JsonClass(generateAdapter = true)
data class HgzyApiResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: HgzyDataWrapper? = null
)

@JsonClass(generateAdapter = true)
data class ApiRecentTrendJson(
    @Json(name = "number") val number: Int = 0,
    @Json(name = "size") val size: String = "",
    @Json(name = "color") val color: String = ""
)

@JsonClass(generateAdapter = true)
data class ApiHistoryStatsDataJson(
    @Json(name = "total_analyzed_rounds") val totalAnalyzedRounds: Int = 0,
    @Json(name = "recent_10_trend") val recent10Trend: List<ApiRecentTrendJson> = emptyList(),
    @Json(name = "number_distribution") val numberDistribution: Map<String, Int> = emptyMap(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiHistoryStatsResponseJson(
    @Json(name = "status") val status: String = "SUCCESS",
    @Json(name = "data") val data: ApiHistoryStatsDataJson? = null
)

interface HgzyApiService {
    @POST
    suspend fun getWinGoHistory(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: HgzyHistoryRequest
    ): retrofit2.Response<HgzyApiResponse>

    @GET
    suspend fun getHistoryStats(
        @Url url: String,
        @Query("limit") limit: Int = 100
    ): retrofit2.Response<ApiHistoryStatsResponseJson>
}

object ApiClientProvider {
    fun createService(): HgzyApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                    .header("Accept", "application/json, text/plain, */*")
                    .build()
                chain.proceed(req)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://hgnice0.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(HgzyApiService::class.java)
    }
}
