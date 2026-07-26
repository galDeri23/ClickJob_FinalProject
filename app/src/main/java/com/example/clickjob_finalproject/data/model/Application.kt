package com.example.clickjob_finalproject.data.model

data class Application(
    val id: String = "",
    val jobId: String = "",
    val workerId: String = "",
    val employerId: String = "",
    val workerName: String = "",
    val workerPhone: String = "",
    val workerBio: String = "",
    val workerProfileImageUrl: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)