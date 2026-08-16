package com.tripmanager.data.models

data class ExpenseParticipant(
    val id: Long = 0,
    val expenseId: Long,
    val participantName: String,
    val share: Double
)