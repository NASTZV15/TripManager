package com.tripmanager.data.models

data class Expense(
    val id: Long = 0,

    val tripId: Long,

    val walletId: Long,

    val categoryId: Long? = null,

    val description: String,

    val amount: Double,

    val paidBy: String,

    val date: String? = null,

    val createdAt: String? = null
)