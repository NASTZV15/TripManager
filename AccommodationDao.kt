package com.tripmanager.data.database

import androidx.room.*
import com.tripmanager.data.models.Accommodation

@Dao
interface AccommodationDao {
    @Query("SELECT * FROM accommodations WHERE trip_id = :tripId ORDER BY check_in")
    suspend fun getAccommodationsByTrip(tripId: Long): List<Accommodation>

    @Insert
    suspend fun insertAccommodation(accommodation: Accommodation): Long

    @Delete
    suspend fun deleteAccommodation(accommodation: Accommodation)
}