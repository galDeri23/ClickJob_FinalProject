package com.example.clickjob_finalproject.auth

import androidx.lifecycle.ViewModel
import com.example.clickjob_finalproject.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth

class RegisterViewModel : ViewModel() {

    // Step 1
    var name: String = ""
    var phone: String = ""
    var email: String = ""
    var city: String = ""
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

    // Profile image - set once at registration, replaced later if user uploads their own
    var profileImageUrl: String = ""

    // Sets default profile image: Google account photo if exists, otherwise UI Avatars with initials
    fun setDefaultProfileImage() {
        if (profileImageUrl.isNotEmpty()) return

        val googlePhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
        profileImageUrl = if (!googlePhotoUrl.isNullOrEmpty()) {
            googlePhotoUrl
        } else {
            val encodedName = java.net.URLEncoder.encode(name, "UTF-8")
            "https://ui-avatars.com/api/?name=$encodedName&background=E5097F&color=fff&size=256"
        }
    }

    // Build UserProfile from all collected data
    fun buildUserProfile(): UserProfile {
        return UserProfile(
            name = name,
            phone = phone,
            email = email,
            address = address,
            city = city,
            searchRadius = searchRadius,
            availableDays = availableDays,
            languages = languages,
            licenses = licenses,
            certificates = certificates,
            software = software,
            jobCategories = jobCategories,
            softSkills = softSkills,
            other = other,
            cvUrl = cvUrl,
            profileImageUrl = profileImageUrl
        )
    }
}