package com.example.clickjob_finalproject.auth

import androidx.lifecycle.ViewModel
import com.example.clickjob_finalproject.data.model.UserProfile

class RegisterViewModel : ViewModel() {

    // Step 1
    var name: String = ""
    var phone: String = ""
    var email: String = ""
    var address: String = ""
    var searchRadius: Int = 10

    // Step 2
    var availableDays: List<String> = emptyList()

    // Step 3
    var languages: List<String> = emptyList()
    var licenses: List<String> = emptyList()
    var certificates: List<String> = emptyList()
    var software: List<String> = emptyList()
    var jobCategories: List<String> = emptyList()
    var softSkills: List<String> = emptyList()
    var other: List<String> = emptyList()

    // Step 4
    var cvUrl: String = ""
    var cvName: String = ""

    // Build UserProfile from all collected data
    fun buildUserProfile(): UserProfile {
        return UserProfile(
            name = name,
            phone = phone,
            email = email,
            address = address,
            searchRadius = searchRadius,
            availableDays = availableDays,
            languages = languages,
            licenses = licenses,
            certificates = certificates,
            software = software,
            jobCategories = jobCategories,
            softSkills = softSkills,
            other = other,
            cvUrl = cvUrl
        )
    }
}