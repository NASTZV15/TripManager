package com.tripmanager.data.database

import androidx.room.*
import com.tripmanager.data.models.TripPlan

@Dao
interface TripPlanDao {
    @Query("SELECT * FROM trip_plan WHERE trip_id = :tripId ORDER BY date")
    suspend fun getPlansByTrip(tripId: Long): List<TripPlan>

    @Insert
    suspend fun insertTripPlan(plan: TripPlan): Long

    @Delete
    suspend fun deleteTripPlan(plan: TripPlan)
}