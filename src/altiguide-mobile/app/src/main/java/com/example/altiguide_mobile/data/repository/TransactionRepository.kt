


package com.example.altiguide_mobile.data.repository

import com.example.altiguide_mobile.data.local.dao.TransactionDao
import com.example.altiguide_mobile.data.local.entity.CachedTransaction
import com.example.altiguide_mobile.data.model.HikingSessionModel
import com.example.altiguide_mobile.data.model.TransactionDetailResponse
import com.example.altiguide_mobile.data.model.TransactionListResponse
import com.example.altiguide_mobile.data.model.TransactionModel
import com.example.altiguide_mobile.data.network.AltiGuideApiService
import com.example.altiguide_mobile.util.AuthDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val apiService: AltiGuideApiService,
    private val authDataStore: AuthDataStore,
    private val transactionDao: TransactionDao
) {
    private fun getSafeEmailKey(email: String): String {
        return email.lowercase().trim().replace("@", "_").replace(".", "_")
    }

    suspend fun getTransactions(): TransactionListResponse {
        val email = authDataStore.authEmailFlow.first()
        android.util.Log.d("TransactionRepository", "getTransactions: email is '$email'")
        if (email.isEmpty()) {
            android.util.Log.w("TransactionRepository", "getTransactions: email is empty, returning empty list")
            return TransactionListResponse("success", emptyList())
        }

        val safeKey = getSafeEmailKey(email)
        val client = okhttp3.OkHttpClient()
        val gson = Gson()
        val url = "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/bookings/$safeKey.json"
        android.util.Log.d("TransactionRepository", "getTransactions: requesting URL $url")
        val request = okhttp3.Request.Builder().url(url).build()

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    android.util.Log.d("TransactionRepository", "getTransactions: response code is ${response.code}")
                    if (!response.isSuccessful) {
                        android.util.Log.w("TransactionRepository", "getTransactions: response unsuccessful, falling back to cache")
                        val cached = transactionDao.getTransactionsForEmail(email).map {
                            gson.fromJson(it.jsonContent, TransactionModel::class.java)
                        }
                        return@withContext TransactionListResponse("success", cached)
                    }
                    val body = response.body?.string()
                    android.util.Log.d("TransactionRepository", "getTransactions: response body is '$body'")
                    if (body.isNullOrEmpty() || body == "null") {
                        transactionDao.deleteTransactionsForEmail(email)
                        return@withContext TransactionListResponse("success", emptyList())
                    }
                    val mapType = object : TypeToken<Map<String, TransactionModel>>() {}.type
                    val map: Map<String, TransactionModel>? = gson.fromJson(body, mapType)
                    val list = map?.values?.toList() ?: emptyList()
                    android.util.Log.d("TransactionRepository", "getTransactions: successfully parsed ${list.size} transactions")

                    // Save to local cache database
                    transactionDao.deleteTransactionsForEmail(email)
                    if (list.isNotEmpty()) {
                        transactionDao.insertTransactions(list.map {
                            CachedTransaction(
                                id = it.id,
                                userEmail = email,
                                jsonContent = gson.toJson(it),
                                createdAt = it.createdAt
                            )
                        })
                    }

                    val sortedList = list.sortedByDescending { it.createdAt ?: "" }
                    TransactionListResponse("success", sortedList)
                }
            } catch (e: Exception) {
                android.util.Log.e("TransactionRepository", "Error fetching firebase bookings, falling back to cache", e)
                val cached = try {
                    transactionDao.getTransactionsForEmail(email).map {
                        gson.fromJson(it.jsonContent, TransactionModel::class.java)
                    }
                } catch (dbEx: Exception) {
                    android.util.Log.e("TransactionRepository", "Error querying cached transactions", dbEx)
                    emptyList()
                }
                TransactionListResponse("success", cached)
            }
        }
    }

    suspend fun getTransactionDetail(id: String): TransactionDetailResponse {
        val transactions = getTransactions().data
        val transaction = transactions.find { it.id == id }
            ?: throw IOException("Transaction with ID $id not found")
        return TransactionDetailResponse("success", transaction)
    }

    suspend fun downloadETicketPdf(id: String): ResponseBody {
        val mockPdfContent = "Mock PDF E-Ticket Content for booking ID: $id"
        val mediaType = "application/pdf".toMediaTypeOrNull()
        return mockPdfContent.toByteArray().toResponseBody(mediaType)
    }

    suspend fun getHikingSessions(): List<HikingSessionModel> {
        return getTransactions().data.mapNotNull { it.hikingSession }
    }

    suspend fun getHikingSessionDetail(id: String): HikingSessionModel {
        val sessions = getHikingSessions()
        return sessions.find { it.id == id }
            ?: throw IOException("Hiking session with ID $id not found")
    }
}


