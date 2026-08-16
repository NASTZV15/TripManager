package com.tripmanager.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "accommodations",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Accommodation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "trip_id")
    val tripId: Long,

    val name: String,

    val address: String? = null,

    @ColumnInfo(name = "check_in")
    val checkIn: String? = null,

    @ColumnInfo(name = "check_out")
    val checkOut: String? = null,

    val price: Double? = null,

    val currency: String? = "RUB",

    @ColumnInfo(name = "contact_phone")
    val contactPhone: String? = null,

    @ColumnInfo(name = "contact_email")
    val contactEmail: String? = null,

    @ColumnInfo(name = "booking_reference")
    val bookingReference: String? = null,

    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)