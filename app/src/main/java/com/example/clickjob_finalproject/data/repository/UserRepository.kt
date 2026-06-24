package com.example.clickjob_finalproject.data.repository

import com.example.clickjob_finalproject.data.model.UserProfile
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveUserProfile(
        profile: UserProfile,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .set(profile)
            .addOnSuccessListener(TaskExecutors.MAIN_THREAD) { onSuccess() }
            .addOnFailureListener(TaskExecutors.MAIN_THREAD) { onFailure(it) }
    }

    fun checkUserExists(
        onExists: () -> Unit,
        onNotExists: () -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(TaskExecutors.MAIN_THREAD) { document ->
                if (document.exists()) onExists() else onNotExists()
            }
    }
}