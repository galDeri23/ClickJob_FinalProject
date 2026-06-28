package com.example.clickjob_finalproject.data.model

data class JobPost(
    val id: String = "",
    val employerId: String = "",
    val title: String = "",
    val company: String = "",
    val category: String = "",
    val salaryType: String = "hourly", // "hourly" or "daily"
    val salary: String = "",
    val date: Long = 0L,
    val startTime: String = "",
    val endTime: String = "",
    val workersNeeded: Int = 1,
    val workersRegistered: Int = 0,
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val phone: String = "",
    val address: String = "",
    val link: String = "",
    val imageUrl: String = "",
    val isUrgent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)