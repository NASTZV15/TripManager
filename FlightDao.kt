package com.tripmanager.data.database

import androidx.room.*
import com.tripmanager.data.models.Flight

@Dao
interface FlightDao {
    @Query("SELECT * FROM flights WHERE trip_id = :tripId ORDER BY departure_date")
    suspend fun getFlightsByTrip(tripId: Long): List<Flight>

    @Insert
    suspend fun insertFlight(flight: Flight): Long

    @Delete
    suspend fun deleteFlight(flight: Flight)
}