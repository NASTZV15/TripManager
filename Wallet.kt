package com.tripmanager.data.models

data class Wallet(
    val id: Long = 0L,

    val tripId: Long,

    val name: String,

    val currency: String = "RUB",

    val balance: Double = 0.0,

    val owner: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)