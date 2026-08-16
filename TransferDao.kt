package com.tripmanager.data.database

import androidx.room.*
import com.tripmanager.data.models.Transfer

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers WHERE trip_id = :tripId ORDER BY date DESC")
    suspend fun getTransfersByTrip(tripId: Long): List<Transfer>

    @Insert
    suspend fun insertTransfer(transfer: Transfer): Long
}