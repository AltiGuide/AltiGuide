package com.example.altiguide_mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_transactions")
data class CachedTransaction(
    @PrimaryKey val id: String,
    val userEmail: String,
    val jsonContent: String,
    val createdAt: String?
)
