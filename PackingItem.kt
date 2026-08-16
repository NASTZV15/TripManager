package com.tripmanager.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "packing_list",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PackingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "trip_id")
    val tripId: Long,

    val category: String,

    val item: String,

    @ColumnInfo(name = "is_checked")
    val isChecked: Int = 0,

    val quantity: Int = 1,

    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)