package com.example.clickjob_finalproject.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickjob_finalproject.AppViewModel
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.NotificationItem
import com.example.clickjob_finalproject.adapters.NotificationStatus
import com.example.clickjob_finalproject.adapters.NotificationsAdapter
import com.example.clickjob_finalproject.data.model.Notification
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentNotificationsBinding
import androidx.core.content.edit

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NotificationsAdapter
    private var isWorkerMode = true
    private val appViewModel: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToggle()
        setupSearch()
        checkUserMode()
    }

    // Recreate the adapter with the correct mode, then attach it
    private fun recreateAdapter(isEmployer: Boolean) {
        adapter = NotificationsAdapter(
            items = emptyList(),
            isEmployerMode = isEmployer,
            onApprove = { item -> handleApprove(item) },
            onCancel = { item -> handleCancel(item) },
            onRate = { item -> handleRate(item) },
            onJobPage = { item -> handleItemClick(item) },
            onItemClick = { item -> handleItemClick(item) }
        )
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    // Check if user has posted a job to show toggle
    private fun checkUserMode() {
        UserRepository.getUserProfile(
            onSuccess = { profile ->
                if (profile.hasPostedJob) {
                    binding.toggleContainer.visibility = View.VISIBLE
                    val savedMode = getSavedMode()
                    isWorkerMode = !savedMode
                    if (savedMode) switchToEmployerMode() else switchToWorkerMode()
                } else {
                    binding.toggleContainer.visibility = View.GONE
                    isWorkerMode = true
                    switchToWorkerMode()
                }
            },
            onFailure = {
                binding.toggleContainer.visibility = View.GONE
                isWorkerMode = true
                switchToWorkerMode()
            }
        )
    }

    // Load notifications from Firestore by role
    private fun loadNotifications(role: String) {
        UserRepository.getNotifications(
            role = role,
            onSuccess = { notifications ->
                val items = notifications.map { it.toNotificationItem() }
                adapter.updateItems(items)
                UserRepository.markNotificationsAsRead(role)
            },
            onFailure = { }
        )
    }

    // Convert Firestore Notification to UI NotificationItem
    private fun Notification.toNotificationItem(): NotificationItem {
        val status = when (type) {
            "ALERT" -> NotificationStatus.ALERT
            "CONFIRMED", "WORKER_CONFIRMED" -> NotificationStatus.CONFIRMED
            "PENDING" -> NotificationStatus.PENDING
            "CANCELLED", "WORKER_CANCELLED" -> NotificationStatus.CANCELLED
            "PEOPLE", "NEW_CANDIDATES" -> NotificationStatus.PEOPLE
            "RATING" -> NotificationStatus.RATING
            else -> NotificationStatus.PENDING
        }
        return NotificationItem(
            id = id,
            title = title,
            dateTime = dateTime,
            timeAgo = getTimeAgo(createdAt),
            status = status,
            jobId = jobId,
            applicationId = applicationId,
            actionRequired = actionRequired,
            isRated = isRated
        )
    }

    // Calculate time ago from timestamp
    private fun getTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 60 -> "לפני $minutes דקות"
            hours < 24 -> "לפני $hours שעות"
            else -> "לפני $days ימים"
        }
    }

    // Worker approves job (double check)
    private fun handleApprove(item: NotificationItem) {
        if (item.jobId.isEmpty() || item.applicationId.isEmpty()) return

        UserRepository.getJobById(
            jobId = item.jobId,
            onSuccess = { job ->
                UserRepository.getJobApplications(
                    jobId = item.jobId,
                    onSuccess = { applications ->
                        val application = applications.find { it.id == item.applicationId }
                            ?: return@getJobApplications
                        UserRepository.confirmJob(
                            application = application,
                            job = job,
                            onSuccess = { loadNotifications("worker") },
                            onFailure = { }
                        )
                    },
                    onFailure = { }
                )
            },
            onFailure = { }
        )
    }

    // Worker cancels job
    private fun handleCancel(item: NotificationItem) {
        if (item.jobId.isEmpty() || item.applicationId.isEmpty()) return

        UserRepository.getJobById(
            jobId = item.jobId,
            onSuccess = { job ->
                UserRepository.getJobApplications(
                    jobId = item.jobId,
                    onSuccess = { applications ->
                        val application = applications.find { it.id == item.applicationId }
                            ?: return@getJobApplications
                        UserRepository.rejectJob(
                            application = application,
                            job = job,
                            onSuccess = { loadNotifications("worker") },
                            onFailure = { }
                        )
                    },
                    onFailure = { }
                )
            },
            onFailure = { }
        )
    }

    // Open rating dialog
    private fun handleRate(item: NotificationItem) {
        if (item.jobId.isEmpty()) return

        if (isWorkerMode) {
            UserRepository.getJobById(
                jobId = item.jobId,
                onSuccess = { job ->
                    RatingDialog.newInstance(job.company) { score ->
                        UserRepository.rateEmployer(
                            employerId = job.employerId,
                            score = score,
                            notificationId = item.id,
                            onSuccess = { loadNotifications("worker") },
                            onFailure = { }
                        )
                    }.show(childFragmentManager, "RatingDialog")
                },
                onFailure = { }
            )
            return
        }

        if (item.applicationId.isEmpty()) return

        UserRepository.getApplicationById(
            applicationId = item.applicationId,
            onSuccess = { application ->
                UserRepository.getUserProfileById(
                    userId = application.workerId,
                    onSuccess = { profile ->
                        EmployerRatingDialog.newInstance(
                            workerName = profile.name,
                            workerImageUrl = profile.profileImageUrl,
                            shiftDetails = item.dateTime,
                            onRatingSubmit = { score ->
                                UserRepository.rateWorker(
                                    workerId = application.workerId,
                                    score = score,
                                    notificationId = item.id,
                                    onSuccess = { loadNotifications("employer") },
                                    onFailure = { }
                                )
                            }
                        ).show(childFragmentManager, "EmployerRatingDialog")
                    },
                    onFailure = { }
                )
            },
            onFailure = { }
        )
    }

    private fun handleItemClick(item: NotificationItem) {
        if (item.jobId.isEmpty()) return

        if (!isWorkerMode) {
            // Employer - navigate to worker sorting
            UserRepository.getJobById(
                jobId = item.jobId,
                onSuccess = { job ->
                    val bundle = bundleOf(
                        "jobId" to item.jobId,
                        "jobTitle" to job.title,
                        "jobCompany" to job.company,
                        "jobCategory" to job.category,
                        "jobDate" to "",
                        "jobPrice" to job.salary,
                        "workersNeeded" to job.workersNeeded
                    )
                    findNavController().navigate(
                        R.id.action_notificationsFragment_to_workerSortingFragment,
                        bundle
                    )
                },
                onFailure = { }
            )
        } else {
            // Worker - navigate to job details
            val args = bundleOf(
                "jobId" to item.jobId,
                "applicationId" to if (item.actionRequired) item.applicationId else null
            )
            findNavController().navigate(
                R.id.action_notificationsFragment_to_jobDetailsFragment,
                args
            )
        }
    }

    private fun setupToggle() {
        binding.toggleWorker.setOnClickListener {
            if (!isWorkerMode) {
                isWorkerMode = true
                saveMode(isEmployer = false)
                switchToWorkerMode()
            }
        }
        binding.toggleEmployer.setOnClickListener {
            if (isWorkerMode) {
                isWorkerMode = false
                saveMode(isEmployer = true)
                switchToEmployerMode()
            }
        }
    }

    private fun switchToWorkerMode() {
        appViewModel.setWorkerMode()
        binding.toggleWorker.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected)
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleEmployer.background = null
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        recreateAdapter(isEmployer = false)
        loadNotifications("worker")
    }

    private fun switchToEmployerMode() {
        appViewModel.setEmployerMode()
        binding.toggleEmployer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected_teal)
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleWorker.background = null
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        recreateAdapter(isEmployer = true)
        loadNotifications("employer")
    }

    private fun saveMode(isEmployer: Boolean) {
        requireContext()
            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit { putBoolean("is_employer_mode", isEmployer) }
    }

    private fun getSavedMode(): Boolean {
        return requireContext()
            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_employer_mode", false)
    }

    private fun setupSearch() {
        binding.ivSearch.setOnClickListener {
            // TODO: search/filter notifications
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}