package com.example.clickjob_finalproject.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.NotificationItem
import com.example.clickjob_finalproject.adapters.NotificationStatus
import com.example.clickjob_finalproject.adapters.NotificationsAdapter
import com.example.clickjob_finalproject.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NotificationsAdapter
    private var isWorkerMode = true

    // Worker notifications - matches the 6 Figma icon types
    private val workerNotifications = listOf(
        NotificationItem(
            title = "נדרש אישור לעבודה ב\"מיט-בר\"",
            dateTime = "עבודה ביום שני 23.03.26 19:00",
            timeAgo = "לפני 12 דקות",
            status = NotificationStatus.ALERT
        ),
        NotificationItem(
            title = "תזכורת לסיום עבודה ב\"מיט-בר\"",
            dateTime = "27.03.26 00:00",
            timeAgo = "לפני 12 דקות",
            status = NotificationStatus.PENDING
        ),
        NotificationItem(
            title = "התקבלת ל\"מאבטח/ת לאירוע\"",
            dateTime = "שעת אבטחה אושרה. הופעה בשבת ב-20:00",
            timeAgo = "לפני כשעה",
            status = NotificationStatus.CONFIRMED
        ),
        NotificationItem(
            title = "דירוג עבודה ב\"חומוס מצמיה\"",
            dateTime = "הזמינו אותך להעריך את העבודה על מיה",
            timeAgo = "לפני יום וחצי",
            status = NotificationStatus.RATING
        ),
        NotificationItem(
            title = "תזכורת להתחלת עבודה ב\"מיט-בר\"",
            dateTime = "27.03.26 17:00 ב-7 שעות",
            timeAgo = "לפני 7 שעות",
            status = NotificationStatus.PEOPLE
        ),
        NotificationItem(
            title = "עבודה בוטלה ב\"השמן\"",
            dateTime = "לא אושר קבלה",
            timeAgo = "לפני 11 ימים",
            status = NotificationStatus.CANCELLED
        )
    )

    // Employer notifications - placeholder for later
    private val employerNotifications = listOf<NotificationItem>()

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

        setupList()
        setupToggle()
        setupSearch()
    }

    private fun setupList() {
        adapter = NotificationsAdapter(
            items = workerNotifications,
            onApprove = { /* TODO: approve action */ },
            onCancel = { /* TODO: cancel action */ },
            onRate = { /* TODO: open rating screen */ }
        )
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    private fun setupToggle() {
        binding.toggleWorker.setOnClickListener {
            if (!isWorkerMode) {
                isWorkerMode = true
                switchToWorkerMode()
            }
        }
        binding.toggleEmployer.setOnClickListener {
            if (isWorkerMode) {
                isWorkerMode = false
                switchToEmployerMode()
            }
        }
    }

    private fun switchToWorkerMode() {
        binding.toggleWorker.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected)
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleEmployer.background = null
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))

        adapter.updateItems(workerNotifications)
    }

    private fun switchToEmployerMode() {
        binding.toggleEmployer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected_teal)
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleWorker.background = null
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))

        adapter.updateItems(employerNotifications)
    }

    private fun setupSearch() {
        binding.ivSearch.setOnClickListener {
            // TODO: open search/filter for notifications
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}