package com.example.clickjob_finalproject.data.repository

import com.example.clickjob_finalproject.data.model.Application
import com.example.clickjob_finalproject.data.model.JobMatch
import com.example.clickjob_finalproject.data.model.JobPost
import com.example.clickjob_finalproject.data.model.Notification
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
    // Submit a job application
    fun applyToJob(
        job: JobPost,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        // Get worker profile first
        getUserProfile(
            onSuccess = { profile ->
                val appRef = db.collection("applications").document()
                val application = Application(
                    id = appRef.id,
                    jobId = job.id,
                    workerId = userId,
                    employerId = job.employerId,
                    workerName = profile.name,
                    workerPhone = profile.phone,
                    workerBio = profile.jobCategories.firstOrNull() ?: "",
                    workerProfileImageUrl = profile.profileImageUrl
                )

                appRef.set(application)
                    .addOnSuccessListener {
                        // Check if this is the 5th applicant - notify employer
                        checkAndNotifyEmployer(job)
                        onSuccess()
                    }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure = onFailure
        )
    }

    // Check if 5 applicants submitted - send notification to employer
    private fun checkAndNotifyEmployer(job: JobPost) {
        db.collection("applications")
            .whereEqualTo("jobId", job.id)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() % 5 == 0) {
                    val notifRef = db.collection("notifications").document()
                    val notification = Notification(
                        id = notifRef.id,
                        userId = job.employerId,
                        role = "employer",
                        type = "NEW_CANDIDATES",
                        title = "יש ${documents.size()} מועמדים חדשים למשרה ${job.title}",
                        dateTime = java.text.SimpleDateFormat("dd.MM.yy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date()),
                        jobId = job.id
                    )
                    notifRef.set(notification)
                }
            }
    }

    // Get all applications for a specific job
    fun getJobApplications(
        jobId: String,
        onSuccess: (List<Application>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .get()
            .addOnSuccessListener { documents ->
                val applications = documents.mapNotNull { it.toObject(Application::class.java) }
                onSuccess(applications)
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Employer approves an applicant
    fun approveApplicant(
        application: Application,
        job: JobPost,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("applications")
            .document(application.id)
            .update("status", "employer_approved")
            .addOnSuccessListener {
                // Send notification to worker
                val notifRef = db.collection("notifications").document()
                val dateStr = java.text.SimpleDateFormat("dd.MM.yy HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(job.date))
                val notification = Notification(
                    id = notifRef.id,
                    userId = application.workerId,
                    role = "worker",
                    type = "ALERT",
                    title = "נדרש אישור לעבודה ב\"${job.company}\"",
                    dateTime = "עבודה ב-$dateStr",
                    jobId = job.id,
                    applicationId = application.id
                )
                notifRef.set(notification)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Worker confirms the job (double check)
    fun confirmJob(
        application: Application,
        job: JobPost,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("applications")
            .document(application.id)
            .update("status", "confirmed")
            .addOnSuccessListener {
                // Update workers registered count on job
                db.collection("jobs")
                    .document(job.id)
                    .update("workersRegistered", job.workersRegistered + 1)

                // Save job to worker's upcoming shifts
                db.collection("users")
                    .document(application.workerId)
                    .update("upcomingShifts", com.google.firebase.firestore.FieldValue.arrayUnion(job.id))

                // Send CONFIRMED notification to worker with shift details
                val dateStr = java.text.SimpleDateFormat("dd.MM.yy", java.util.Locale.getDefault())
                    .format(java.util.Date(job.date))
                val workerNotifRef = db.collection("notifications").document()
                val workerNotification = Notification(
                    id = workerNotifRef.id,
                    userId = application.workerId,
                    role = "worker",
                    type = "CONFIRMED",
                    title = "התקבלת לעבודה ב\"${job.company}\"",
                    dateTime = "עבודה ביום $dateStr ${job.startTime}",
                    jobId = job.id,
                    applicationId = application.id
                )
                workerNotifRef.set(workerNotification)

                // Send WORKER_CONFIRMED notification to employer
                val employerNotifRef = db.collection("notifications").document()
                val employerNotification = Notification(
                    id = employerNotifRef.id,
                    userId = job.employerId,
                    role = "employer",
                    type = "WORKER_CONFIRMED",
                    title = "${application.workerName} אישר/ה את העבודה ב\"${job.company}\"",
                    dateTime = java.text.SimpleDateFormat("dd.MM.yy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date()),
                    jobId = job.id,
                    applicationId = application.id
                )
                employerNotifRef.set(employerNotification)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
    // Worker rejects the job
    fun rejectJob(
        application: Application,
        job: JobPost,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("applications")
            .document(application.id)
            .update("status", "rejected")
            .addOnSuccessListener {
                // Send notification to employer
                val notifRef = db.collection("notifications").document()
                val notification = Notification(
                    id = notifRef.id,
                    userId = job.employerId,
                    role = "employer",
                    type = "WORKER_CANCELLED",
                    title = "${application.workerName} ביטל/ה את העבודה ב\"${job.company}\"",
                    dateTime = java.text.SimpleDateFormat("dd.MM.yy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date()),
                    jobId = job.id,
                    applicationId = application.id
                )
                notifRef.set(notification)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Get notifications for current user
    fun getNotifications(
        role: String,
        onSuccess: (List<Notification>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("role", role)
            .get()
            .addOnSuccessListener { documents ->
                val notifications = documents
                    .mapNotNull { it.toObject(Notification::class.java) }
                    .sortedByDescending { it.createdAt }
                onSuccess(notifications)
            }
            .addOnFailureListener { onFailure(it) }
    }
    // Mark all notifications as read
    fun markNotificationsAsRead(role: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("role", role)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit()
            }
    }

    // Rate a worker
    fun rateWorker(
        workerId: String,
        score: Double,
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(workerId)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java) ?: return@addOnSuccessListener
                val newCount = profile.ratingsCount + 1
                val newRating = ((profile.rating * profile.ratingsCount) + score) / newCount

                db.collection("users").document(workerId)
                    .update(mapOf("rating" to newRating, "ratingsCount" to newCount))
                    .addOnSuccessListener {
                        // Mark notification as rated
                        db.collection("notifications").document(notificationId)
                            .update("isRated", true)
                            .addOnSuccessListener { onSuccess() }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Rate an employer
    fun rateEmployer(
        employerId: String,
        score: Double,
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(employerId)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java) ?: return@addOnSuccessListener
                val newCount = profile.ratingsCount + 1
                val newRating = ((profile.rating * profile.ratingsCount) + score) / newCount

                db.collection("users").document(employerId)
                    .update(mapOf("rating" to newRating, "ratingsCount" to newCount))
                    .addOnSuccessListener {
                        // Mark notification as rated
                        db.collection("notifications").document(notificationId)
                            .update("isRated", true)
                            .addOnSuccessListener { onSuccess() }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun listenToEmployerJobs(
        onUpdate: (List<JobPost>) -> Unit,
        onFailure: (Exception) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        val userId = auth.currentUser?.uid ?: return db.collection("jobs")
            .addSnapshotListener { _, _ -> }

        return db.collection("jobs")
            .whereEqualTo("employerId", userId)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }
                val jobs = documents?.mapNotNull { it.toObject(JobPost::class.java) } ?: emptyList()
                onUpdate(jobs)
            }
    }

    fun getUserProfileById(
        userId: String,
        onSuccess: (UserProfile) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                if (profile != null) onSuccess(profile)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getWorkerApplications(
        onSuccess: (List<Application>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("applications")
            .whereEqualTo("workerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val applications = documents.mapNotNull {
                    it.toObject(Application::class.java)
                }
                onSuccess(applications)
            }
            .addOnFailureListener { onFailure(it) }
    }
    fun recordWorkerArrival(
        jobId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .whereEqualTo("workerId", userId)
            .whereEqualTo("status", "confirmed")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onFailure(Exception("No confirmed application found"))
                    return@addOnSuccessListener
                }

                val appDoc = documents.first()
                appDoc.reference.update("status", "arrived")
                    .addOnSuccessListener {
                        // Load job details to build notification
                        db.collection("jobs").document(jobId)
                            .get()
                            .addOnSuccessListener { jobDoc ->
                                val company = jobDoc.getString("company") ?: ""
                                val endTime = jobDoc.getString("endTime") ?: ""
                                val date = jobDoc.getLong("date") ?: 0L
                                val dateStr = java.text.SimpleDateFormat("dd.MM.yy", java.util.Locale.getDefault())
                                    .format(java.util.Date(date))

                                // Send PENDING notification to worker (clock icon)
                                val notifRef = db.collection("notifications").document()
                                val notification = Notification(
                                    id = notifRef.id,
                                    userId = userId,
                                    role = "worker",
                                    type = "PENDING",
                                    title = "סריקה לסיום עבודה ב\"$company\"",
                                    dateTime = "עבודה הסתיימה ב-$dateStr $endTime:00",
                                    jobId = jobId
                                )
                                notifRef.set(notification)
                                    .addOnSuccessListener {
                                        checkAllWorkersArrived(jobId, onSuccess, onFailure)
                                    }
                                    .addOnFailureListener { onFailure(it) }
                            }
                            .addOnFailureListener { onFailure(it) }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
    // Check if all required workers have arrived - if so move job to history
    private fun checkAllWorkersArrived(
        jobId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("jobs").document(jobId)
            .get()
            .addOnSuccessListener { jobDoc ->
                val workersNeeded = jobDoc.getLong("workersNeeded")?.toInt() ?: return@addOnSuccessListener
                val employerId = jobDoc.getString("employerId") ?: return@addOnSuccessListener
                val jobTitle = jobDoc.getString("title") ?: ""
                val company = jobDoc.getString("company") ?: ""

                db.collection("applications")
                    .whereEqualTo("jobId", jobId)
                    .whereEqualTo("status", "arrived")
                    .get()
                    .addOnSuccessListener { arrivedDocs ->
                        if (arrivedDocs.size() >= workersNeeded) {
                            // All workers arrived - notify employer
                            val employerNotifRef = db.collection("notifications").document()
                            val employerNotif = Notification(
                                id = employerNotifRef.id,
                                userId = employerId,
                                role = "employer",
                                type = "CONFIRMED",
                                title = "כל העובדים הגיעו ל\"$jobTitle\"",
                                dateTime = java.text.SimpleDateFormat("dd.MM.yy HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date()),
                                jobId = jobId
                            )
                            employerNotifRef.set(employerNotif)

                            // Send RATING notification to employer
                            val employerRatingRef = db.collection("notifications").document()
                            val employerRatingNotif = Notification(
                                id = employerRatingRef.id,
                                userId = employerId,
                                role = "employer",
                                type = "RATING",
                                title = "דרג את העובדים ב\"$jobTitle\"",
                                dateTime = "דירוגים עוזרים לעובדים למצוא עבודה",
                                jobId = jobId
                            )
                            employerRatingRef.set(employerRatingNotif)

                            // Send RATING notification to each worker
                            arrivedDocs.forEach { arrivedDoc ->
                                val workerId = arrivedDoc.getString("workerId") ?: return@forEach
                                val workerRatingRef = db.collection("notifications").document()
                                val workerRatingNotif = Notification(
                                    id = workerRatingRef.id,
                                    userId = workerId,
                                    role = "worker",
                                    type = "RATING",
                                    title = "דרג את עבודה ב\"$company\"",
                                    dateTime = "דירוגים מעלים את הסיכוי למצוא עבודה",
                                    jobId = jobId
                                )
                                workerRatingRef.set(workerRatingNotif)
                            }
                        }
                        onSuccess()
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
    fun getUpcomingShifts(
        onSuccess: (List<JobPost>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        getUserProfile(
            onSuccess = { profile ->
                if (profile.upcomingShifts.isEmpty()) {
                    onSuccess(emptyList())
                    return@getUserProfile
                }

                db.collection("jobs")
                    .whereIn("id", profile.upcomingShifts)
                    .get()
                    .addOnSuccessListener { documents ->
                        val now = System.currentTimeMillis()
                        val jobs = documents.mapNotNull { it.toObject(JobPost::class.java) }
                            .filter { job ->
                                // Only show future shifts
                                val endHour = job.endTime.split(":")[0].toIntOrNull() ?: 0
                                val shiftEndMillis = java.util.Calendar.getInstance().apply {
                                    timeInMillis = job.date
                                    set(java.util.Calendar.HOUR_OF_DAY, endHour)
                                }.timeInMillis
                                shiftEndMillis > now
                            }
                            .sortedBy { it.date }
                            .take(3)
                        onSuccess(jobs)
                    }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure = onFailure
        )
    }
}