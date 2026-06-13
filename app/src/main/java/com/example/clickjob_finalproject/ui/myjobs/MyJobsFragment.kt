package com.example.clickjob_finalproject.ui.myjobs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.EmployerJobItem
import com.example.clickjob_finalproject.adapters.EmployerJobsAdapter
import com.example.clickjob_finalproject.adapters.JobTabType
import com.example.clickjob_finalproject.adapters.MyJobItem
import com.example.clickjob_finalproject.adapters.MyJobsAdapter
import com.example.clickjob_finalproject.databinding.FragmentMyJobsBinding

class MyJobsFragment : Fragment() {

    private var _binding: FragmentMyJobsBinding? = null
    private val binding get() = _binding!!

    private var isWorkerMode = true
    private var currentTab = JobTabType.ACTIVE

    // ===== Worker data =====
    private val workerActive = mutableListOf(
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "מחר", "הפקה ואירועים", needsApproval = true),
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "מחר", "מסעדות", needsApproval = true),
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "מחר", "מכירות ואופנה", needsApproval = true)
    )
    private val workerPending = mutableListOf<MyJobItem>()
    private val workerHistory = mutableListOf(
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "אתמול", "אחזקה"),
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "אתמול", "בניין וייצור")
    )

    // ===== Employer data =====
    private val employerActive = mutableListOf(
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 3, 7),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 1, 5),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 4, 4)
    )
    private val employerPending = mutableListOf(
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 2, 6, countdownMillis = 18000000L),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 3, 7, countdownMillis = 18000000L),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 1, 3, countdownMillis = 18000000L)
    )
    private val employerHistory = mutableListOf(
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 7, 7),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 5, 5),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 3, 6)
    )

    // Adapters
    private lateinit var workerAdapter: MyJobsAdapter
    private lateinit var employerAdapter: EmployerJobsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboard()
        setupWorkerAdapter()
        setupTabs()
        setupToggle()
        updateTabLabels()
    }

    private fun setupKeyboard() {
        binding.etSearch.isFocusable = false
        binding.etSearch.isFocusableInTouchMode = false
        binding.root.setOnClickListener {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    // Setup worker adapter (default mode)
    private fun setupWorkerAdapter() {
        workerAdapter = MyJobsAdapter(
            items = workerActive,
            tabType = JobTabType.ACTIVE,
            onApproveClick = { item ->
                workerActive.remove(item)
                workerPending.add(item.copy(needsApproval = false, countdownMillis = 18000000L))
                updateTabLabels()
                workerAdapter.updateItems(workerActive.toList(), JobTabType.ACTIVE)
            }
        )
        binding.rvJobs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJobs.adapter = workerAdapter
    }

    // Setup employer adapter
    private fun setupEmployerAdapter() {
        employerAdapter = EmployerJobsAdapter(
            items = employerActive,
            tabType = JobTabType.ACTIVE,
            onQrClick = { item ->
                // TODO: open camera for QR scan
            }
        )
        binding.rvJobs.adapter = employerAdapter
    }

    private fun setupTabs() {
        val tabs = listOf(binding.tabActive, binding.tabPending, binding.tabHistory)

        binding.tabActive.setOnClickListener {
            currentTab = JobTabType.ACTIVE
            tabs.forEach { setTabUnselected(it) }
            setTabSelected(binding.tabActive)
            if (isWorkerMode)
                workerAdapter.updateItems(workerActive.toList(), JobTabType.ACTIVE)
            else
                employerAdapter.updateItems(employerActive.toList(), JobTabType.ACTIVE)
        }

        binding.tabPending.setOnClickListener {
            currentTab = JobTabType.PENDING
            tabs.forEach { setTabUnselected(it) }
            setTabSelected(binding.tabPending)
            if (isWorkerMode)
                workerAdapter.updateItems(workerPending.toList(), JobTabType.PENDING)
            else
                employerAdapter.updateItems(employerPending.toList(), JobTabType.PENDING)
        }

        binding.tabHistory.setOnClickListener {
            currentTab = JobTabType.HISTORY
            tabs.forEach { setTabUnselected(it) }
            setTabSelected(binding.tabHistory)
            if (isWorkerMode)
                workerAdapter.updateItems(workerHistory.toList(), JobTabType.HISTORY)
            else
                employerAdapter.updateItems(employerHistory.toList(), JobTabType.HISTORY)
        }
    }

    private fun updateTabLabels() {
        if (isWorkerMode) {
            binding.tabActive.text  = "פעילות (${workerActive.size})"
            binding.tabPending.text = "בהמתנה (${workerPending.size})"
        } else {
            binding.tabActive.text  = "פעילות (${employerActive.size})"
            binding.tabPending.text = "בהמתנה (${employerPending.size})"
        }
        binding.tabHistory.text = "היסטוריה"
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
        // Toggle colors: pink
        binding.toggleWorker.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected)
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleEmployer.background = null
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))

        // Tab colors: pink
        updateTabColors(isPink = true)

        // Switch adapter to worker
        currentTab = JobTabType.ACTIVE
        binding.rvJobs.adapter = workerAdapter
        workerAdapter.updateItems(workerActive.toList(), JobTabType.ACTIVE)
        updateTabLabels()

        val tabs = listOf(binding.tabActive, binding.tabPending, binding.tabHistory)
        tabs.forEach { setTabUnselected(it) }
        setTabSelected(binding.tabActive)
    }

    private fun switchToEmployerMode() {
        // Toggle colors: teal
        binding.toggleEmployer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected_teal)
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleWorker.background = null
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))

        // Tab colors: teal
        updateTabColors(isPink = false)

        // Switch adapter to employer
        currentTab = JobTabType.ACTIVE
        setupEmployerAdapter()
        updateTabLabels()

        val tabs = listOf(binding.tabActive, binding.tabPending, binding.tabHistory)
        tabs.forEach { setTabUnselected(it) }
        setTabSelected(binding.tabActive)
    }

    // Switch all tab colors between pink and teal
    private fun updateTabColors(isPink: Boolean) {
        val selectedDrawable = if (isPink)
            R.drawable.bg_tab_selected
        else
            R.drawable.bg_tab_selected_teal

        // Reset all to unselected first
        listOf(binding.tabActive, binding.tabPending, binding.tabHistory)
            .forEach { setTabUnselected(it) }

        // Active tab gets selected color
        binding.tabActive.background = ContextCompat.getDrawable(requireContext(), selectedDrawable)
        binding.tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setTabSelected(tab: TextView) {
        val selectedDrawable = if (isWorkerMode)
            R.drawable.bg_tab_selected
        else
            R.drawable.bg_tab_selected_teal

        tab.background = ContextCompat.getDrawable(requireContext(), selectedDrawable)
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setTabUnselected(tab: TextView) {
        tab.background = null
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}