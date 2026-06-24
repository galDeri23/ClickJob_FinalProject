package com.example.clickjob_finalproject.data.model

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
    val createdAt: Long = System.currentTimeMillis()
)