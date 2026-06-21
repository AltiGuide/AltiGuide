package com.example.altiguide_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.altiguide_mobile.data.local.entity.CachedTransaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM cached_transactions WHERE userEmail = :email ORDER BY createdAt DESC")
    suspend fun getTransactionsForEmail(email: String): List<CachedTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<CachedTransaction>)

    @Query("DELETE FROM cached_transactions WHERE userEmail = :email")
    suspend fun deleteTransactionsForEmail(email: String)
}
