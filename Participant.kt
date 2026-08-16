package com.tripmanager.data.models

data class Participant(
    val id: Long = 0,
    val tripId: Long,
    val name: String,
    val createdAt: String? = null
)