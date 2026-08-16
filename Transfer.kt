package com.tripmanager.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transfers",
    foreignKeys = [
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["from_wallet_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["to_wallet_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transfer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "trip_id")
    val tripId: Long,

    @ColumnInfo(name = "from_wallet_id")
    val fromWalletId: Long,

    @ColumnInfo(name = "to_wallet_id")
    val toWalletId: Long,

    val amount: Double,

    @ColumnInfo(name = "converted_amount")
    val convertedAmount: Double? = null,

    val description: String? = null,

    val date: String? = null
)