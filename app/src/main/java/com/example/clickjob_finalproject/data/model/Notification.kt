package com.example.clickjob_finalproject.data.model

import com.google.firebase.firestore.PropertyName

data class Notification(
    val id: String = "",
    val userId: String = "",
    val role: String = "worker",
    val type: String = "",
    val title: String = "",
    val dateTime: String = "",
    val jobId: String = "",
    val applicationId: String = "",
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    @get:PropertyName("isRated")
    @set:PropertyName("isRated")
    var isRated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)