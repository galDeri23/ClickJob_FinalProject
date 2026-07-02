package com.example.clickjob_finalproject.data.model

import com.google.firebase.firestore.PropertyName

data class JobPost(
    val id: String = "",
    val employerId: String = "",
    val title: String = "",
    val company: String = "",
    val category: String = "",
    val salaryType: String = "hourly", // "hourly" or "daily"
    val salary: String = "",
    val workFrequency: String = "",
    val date: Long = 0L,      // Start date of the job
    val endDate: Long = 0L,   // End date - equals date for single-day jobs
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
    @get:PropertyName("isUrgent")
    @set:PropertyName("isUrgent")
    var isUrgent: Boolean = false,
    var ratingNotificationsSent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)