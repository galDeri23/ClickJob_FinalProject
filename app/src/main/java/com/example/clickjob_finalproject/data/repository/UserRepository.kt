package com.example.clickjob_finalproject.data.repository

import com.example.clickjob_finalproject.data.model.JobMatch
import com.example.clickjob_finalproject.data.model.JobPost
import com.example.clickjob_finalproject.data.model.UserProfile
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
            .set(profile, SetOptions.merge())
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
    fun generateBio(profile: UserProfile): String {
        val parts = mutableListOf<String>()

        if (profile.name.isNotEmpty()) parts.add("שמי ${profile.name}")
        if (profile.jobCategories.isNotEmpty()) parts.add("מחפש עבודה בתחומים: ${profile.jobCategories.joinToString(", ")}")
        if (profile.availableDays.isNotEmpty()) parts.add("זמין בימים: ${profile.availableDays.joinToString(", ")}")
        if (profile.languages.isNotEmpty()) parts.add("שפות: ${profile.languages.joinToString(", ")}")
        if (profile.softSkills.isNotEmpty()) parts.add("כישורים: ${profile.softSkills.joinToString(", ")}")
        if (profile.address.isNotEmpty()) parts.add("מתגורר ב${profile.address}")

        return parts.joinToString(". ")
    }

    fun getUserProfile(
        onSuccess: (UserProfile) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                if (profile != null) onSuccess(profile)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveJobPost(
        job: JobPost,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val jobRef = db.collection("jobs").document()
        val jobWithId = job.copy(id = jobRef.id, employerId = userId)

        jobRef.set(jobWithId)
            .addOnSuccessListener {
                // Mark user as having posted a job
                db.collection("users").document(userId)
                    .update("hasPostedJob", true)
                    .addOnSuccessListener { onSuccess(jobRef.id) }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getEmployerJobs(
        onSuccess: (List<JobPost>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("jobs")
            .whereEqualTo("employerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val jobs = documents.mapNotNull { it.toObject(JobPost::class.java) }
                onSuccess(jobs)
            }
            .addOnFailureListener { onFailure(it) }
    }
    fun getUrgentJobs(
        onSuccess: (List<JobPost>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("jobs")
            .whereEqualTo("isUrgent", true)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val jobs = documents.mapNotNull { it.toObject(JobPost::class.java) }
                onSuccess(jobs)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getBestMatchJobs(
        jobMatches: List<JobMatch>,
        onSuccess: (List<Pair<JobPost, Int>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (jobMatches.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        val jobIds = jobMatches.map { it.jobId }
        db.collection("jobs")
            .whereIn("id", jobIds)
            .get()
            .addOnSuccessListener { documents ->
                val jobs = documents.mapNotNull { it.toObject(JobPost::class.java) }
                // Pair each job with its match percent
                val result = jobs.mapNotNull { job ->
                    val match = jobMatches.find { it.jobId == job.id }
                    if (match != null) Pair(job, match.matchPercent) else null
                }.sortedByDescending { it.second }.take(10)
                onSuccess(result)
            }
            .addOnFailureListener { onFailure(it) }
    }
    fun getJobById(
        jobId: String,
        onSuccess: (JobPost) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("jobs")
            .document(jobId)
            .get()
            .addOnSuccessListener { document ->
                val job = document.toObject(JobPost::class.java)
                if (job != null) onSuccess(job)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun searchJobs(
        categories: List<String>,
        onSuccess: (List<JobPost>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var query = db.collection("jobs") as com.google.firebase.firestore.Query

        if (categories.isNotEmpty()) {
            query = query.whereIn("category", categories)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val jobs = documents.mapNotNull { it.toObject(JobPost::class.java) }
                    .sortedBy { it.date } // Sort by date - closest first
                onSuccess(jobs)
            }
            .addOnFailureListener { onFailure(it) }
    }
}