package com.tripmanager.data.models

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String = "📌",
    val color: String = "#3498db"
)