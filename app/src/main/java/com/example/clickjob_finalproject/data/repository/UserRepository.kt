package com.example.clickjob_finalproject.data.repository

import android.util.Log
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
        db.collection("candidates")
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
        db.collection("candidates")
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
        db.collection("candidates")
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
                db.collection("candidates").document(userId)
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
                Log.d("DEBUG", "Urgent jobs count: ${documents.size()}")
                val jobs = documents.mapNotNull { it.toObject(JobPost::class.java) }
                Log.d("DEBUG", "Urgent jobs mapped: ${jobs.size}")
                onSuccess(jobs)
            }
            .addOnFailureListener {
                Log.e("DEBUG", "Error: ${it.message}")
                onFailure(it)
            }
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
                    .filter { isJobStillOpen(it) }
                    .sortedBy { it.date }
                onSuccess(jobs)
            }
            .addOnFailureListener { onFailure(it) }
    }
    fun applyToJob(
        job: JobPost,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
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
                        checkAndNotifyEmployer(job)
                        onSuccess()
                    }
                    .addOnFailureListener { onFailure(it) }
            },
            onFailure = onFailure
        )
    }

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
                db.collection("jobs")
                    .document(job.id)
                    .update("workersRegistered", job.workersRegistered + 1)

                db.collection("candidates")
                    .document(application.workerId)
                    .update("upcomingShifts", com.google.firebase.firestore.FieldValue.arrayUnion(job.id))

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

    // Employer cancels a worker: notifies the worker and rolls back confirmation side effects
    fun cancelWorkerByEmployer(
        application: Application,
        job: JobPost,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("applications")
            .document(application.id)
            .update("status", "rejected")
            .addOnSuccessListener {
                // Roll back what confirmJob did (relevant when the worker already confirmed)
                db.collection("jobs")
                    .document(job.id)
                    .update(
                        "workersRegistered",
                        com.google.firebase.firestore.FieldValue.increment(-1)
                    )

                db.collection("candidates")
                    .document(application.workerId)
                    .update(
                        "upcomingShifts",
                        com.google.firebase.firestore.FieldValue.arrayRemove(job.id)
                    )

                // Notify the worker that the employer cancelled the job
                val notifRef = db.collection("notifications").document()
                val notification = Notification(
                    id = notifRef.id,
                    userId = application.workerId,
                    role = "worker",
                    type = "ALERT",
                    title = "העבודה ב\"${job.company}\" בוטלה על ידי המעסיק",
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

    fun rateWorker(
        workerId: String,
        score: Double,
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("candidates").document(workerId)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java) ?: return@addOnSuccessListener
                val newCount = profile.ratingsCount + 1
                val newRating = ((profile.rating * profile.ratingsCount) + score) / newCount
                db.collection("candidates").document(workerId)
                    .update(mapOf("rating" to newRating, "ratingsCount" to newCount))
                    .addOnSuccessListener {
                        db.collection("notifications").document(notificationId)
                            .update("isRated", true)
                            .addOnSuccessListener { onSuccess() }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun rateEmployer(
        employerId: String,
        score: Double,
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("candidates").document(employerId)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java) ?: return@addOnSuccessListener
                val newCount = profile.ratingsCount + 1
                val newRating = ((profile.rating * profile.ratingsCount) + score) / newCount
                db.collection("candidates").document(employerId)
                    .update(mapOf("rating" to newRating, "ratingsCount" to newCount))
                    .addOnSuccessListener {
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
        db.collection("candidates")
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
                val applications = documents.mapNotNull { it.toObject(Application::class.java) }
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
                        db.collection("jobs").document(jobId)
                            .get()
                            .addOnSuccessListener { jobDoc ->
                                val company = jobDoc.getString("company") ?: ""
                                val endTime = jobDoc.getString("endTime") ?: ""
                                val date = jobDoc.getLong("date") ?: 0L
                                val dateStr = java.text.SimpleDateFormat("dd.MM.yy", java.util.Locale.getDefault())
                                    .format(java.util.Date(date))
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

                            val employerRatingRef = db.collection("notifications").document()
                            val employerRatingNotif = Notification(
                                id = employerRatingRef.id,
                                userId = employerId,
                                role = "employer",
                                type = "RATING",
                                title = "דרג את העובדים במשרת $jobTitle",
                                dateTime = "דירוגים עוזרים לעובדים למצוא עבודה",
                                jobId = jobId
                            )
                            employerRatingRef.set(employerRatingNotif)

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

    fun checkFinishedShiftsAndCreateRatingNotifications(
        onComplete: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val now = System.currentTimeMillis()

        db.collection("jobs")
            .get()
            .addOnSuccessListener { jobDocs ->

                val finishedJobs = jobDocs
                    .mapNotNull { it.toObject(JobPost::class.java) }
                    .filter { job ->
                        !job.ratingNotificationsSent && isShiftEnded(job, now)
                    }

                if (finishedJobs.isEmpty()) {
                    onComplete()
                    return@addOnSuccessListener
                }

                var completedCount = 0

                finishedJobs.forEach { job ->
                    createRatingNotificationsForJob(
                        job = job,
                        onComplete = {
                            completedCount++
                            if (completedCount == finishedJobs.size) {
                                onComplete()
                            }
                        },
                        onFailure = onFailure
                    )
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    private fun isShiftEnded(job: JobPost, now: Long): Boolean {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = if (job.endDate != 0L) job.endDate else job.date

            val parts = job.endTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis < now
    }

    private fun createRatingNotificationsForJob(
        job: JobPost,
        onComplete: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        db.collection("applications")
            .whereEqualTo("jobId", job.id)
            .get()
            .addOnSuccessListener { applicationDocs ->

                val applications = applicationDocs
                    .mapNotNull { it.toObject(Application::class.java) }
                    .filter { it.status == "confirmed" || it.status == "arrived" }

                if (applications.isEmpty()) {
                    db.collection("jobs")
                        .document(job.id)
                        .update("ratingNotificationsSent", true)
                        .addOnSuccessListener { onComplete() }
                        .addOnFailureListener { onFailure(it) }

                    return@addOnSuccessListener
                }

                val batch = db.batch()

                applications.forEach { application ->

                    val employerRef = db.collection("notifications").document()
                    val employerNotification = Notification(
                        id = employerRef.id,
                        userId = job.employerId,
                        role = "employer",
                        type = "RATING",
                        title = "דירוג העובד \"${application.workerName}\"",
                        dateTime = "דירוגים מסייעים למצוא עובדים מתאימים",
                        jobId = job.id,
                        applicationId = application.id
                    )
                    batch.set(employerRef, employerNotification)

                    if (application.status == "arrived") {
                        val workerRef = db.collection("notifications").document()
                        val workerNotification = Notification(
                            id = workerRef.id,
                            userId = application.workerId,
                            role = "worker",
                            type = "RATING",
                            title = "דירוג עבודה ב\"${job.company}\"",
                            dateTime = "דירוגים מעלים את הסיכוי למצוא עבודה",
                            jobId = job.id,
                            applicationId = application.id
                        )
                        batch.set(workerRef, workerNotification)
                    }
                }

                val jobRef = db.collection("jobs").document(job.id)
                batch.update(jobRef, "ratingNotificationsSent", true)

                batch.commit()
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Returns true if the job can still be applied to (start date+time has not passed)
    fun isJobStillOpen(job: JobPost): Boolean {
        val startMillis = java.util.Calendar.getInstance().apply {
            timeInMillis = job.date
            val parts = job.startTime.split(":")
            set(java.util.Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 0)
            set(java.util.Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return startMillis > System.currentTimeMillis()
    }
    fun getApplicationById(
        applicationId: String,
        onSuccess: (Application) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("applications")
            .document(applicationId)
            .get()
            .addOnSuccessListener { document ->
                val application = document.toObject(Application::class.java)
                if (application != null) {
                    onSuccess(application)
                } else {
                    onFailure(Exception("Application not found"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

}