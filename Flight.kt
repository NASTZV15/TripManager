package com.tripmanager.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "flights",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Flight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "trip_id")
    val tripId: Long,

    val airline: String? = null,

    @ColumnInfo(name = "flight_number")
    val flightNumber: String? = null,

    @ColumnInfo(name = "departure_airport")
    val departureAirport: String? = null,

    @ColumnInfo(name = "arrival_airport")
    val arrivalAirport: String? = null,

    @ColumnInfo(name = "departure_date")
    val departureDate: String? = null,

    @ColumnInfo(name = "departure_time")
    val departureTime: String? = null,

    @ColumnInfo(name = "arrival_date")
    val arrivalDate: String? = null,

    @ColumnInfo(name = "arrival_time")
    val arrivalTime: String? = null,

    val price: Double? = null,

    val currency: String? = "RUB",

    @ColumnInfo(name = "booking_reference")
    val bookingReference: String? = null,

    @ColumnInfo(name = "seat_number")
    val seatNumber: String? = null,

    @ColumnInfo(name = "baggage_info")
    val baggageInfo: String? = null,

    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)