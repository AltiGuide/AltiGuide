package com.example.altiguide_mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.altiguide_mobile.data.local.dao.RouteDao
import com.example.altiguide_mobile.data.local.dao.WaypointDao
import com.example.altiguide_mobile.data.local.dao.TransactionDao
import com.example.altiguide_mobile.data.local.entity.CachedRoute
import com.example.altiguide_mobile.data.local.entity.CachedWaypoint
import com.example.altiguide_mobile.data.local.entity.CachedTransaction

@Database(
    entities = [CachedRoute::class, CachedWaypoint::class, CachedTransaction::class],
    version = 2,
    exportSchema = false
)
abstract class AltiGuideDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun waypointDao(): WaypointDao
    abstract fun transactionDao(): TransactionDao
}

