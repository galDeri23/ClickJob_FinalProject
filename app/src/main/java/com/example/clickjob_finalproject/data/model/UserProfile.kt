package com.example.clickjob_finalproject.data.model

data class Document(
    val name: String = "",
    val url: String = ""
)

data class UserProfile(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val searchRadius: Int = 0,
    val availableDays: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val licenses: List<String> = emptyList(),
    val certificates: List<String> = emptyList(),
    val software: List<String> = emptyList(),
    val jobCategories: List<String> = emptyList(),
    val softSkills: List<String> = emptyList(),
    val other: List<String> = emptyList(),
    val cvUrl: String = "",
    val cvName: String = "",
    val documents: List<Document> = emptyList(),
    val instagramUrl: String = "",
    val bio: String = "",
    val rating: Double = 0.0,
    val ratingsCount: Int = 0,
    val profileImageUrl: String = "",
    val hasPostedJob: Boolean = false,
    val jobMatches: List<JobMatch> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)