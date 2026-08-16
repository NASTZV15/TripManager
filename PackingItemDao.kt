package com.tripmanager.data.database

import androidx.room.*
import com.tripmanager.data.models.PackingItem

@Dao
interface PackingItemDao {
    @Query("SELECT * FROM packing_list WHERE trip_id = :tripId ORDER BY category")
    suspend fun getPackingItemsByTrip(tripId: Long): List<PackingItem>

    @Insert
    suspend fun insertPackingItem(item: PackingItem): Long

    @Update
    suspend fun updatePackingItem(item: PackingItem)

    @Delete
    suspend fun deletePackingItem(item: PackingItem)
}