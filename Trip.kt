package com.tripmanager.data.models

data class Trip(
    val id: Long = 0L,

    val name: String,

    val destination: String? = null,

    val startDate: Long? = null,

    val endDate: Long? = null,

    val createdAt: Long = System.currentTimeMillis()
)