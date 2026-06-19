package com.example.clickjob_finalproject.ui.myjobs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clickjob_finalproject.AppViewModel
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

    // Survives navigation - remembers mode and tab when user leaves and returns
    private val viewModel: MyJobsViewModel by viewModels()

    // Shared with MainActivity and NotificationsFragment to sync bottom nav color
    private val appViewModel: AppViewModel by activityViewModels()

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
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 7, 7),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 5, 5),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 4, 4)
    )
    private val employerPending = mutableListOf(
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 2, 6, countdownMillis = 18000000L),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 3, 7, countdownMillis = 18000000L),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 1, 3, countdownMillis = 18000000L)
    )
    private val employerHistory = mutableListOf<EmployerJobItem>()

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
        setupEmployerAdapter()
        setupTabs()
        setupToggle()
        setupPostJobButtons()

        // If the user arrived from the "פרסום משרה" button in HomeFragment,
        // force employer mode + history tab regardless of the saved ViewModel state.
        val openEmployerHistory = arguments?.getBoolean("openEmployerHistory", false) ?: false
        if (openEmployerHistory) {
            arguments?.remove("openEmployerHistory")
            viewModel.isWorkerMode = false
            viewModel.currentTab = JobTabType.HISTORY
        }

        // Restore the saved mode and tab (either just forced above, or from a previous visit)
        if (viewModel.isWorkerMode) {
            applyWorkerMode(animated = false)
        } else {
            applyEmployerMode(animated = false)
        }
        restoreTab(viewModel.currentTab)
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

    private fun setupEmployerAdapter() {
        employerAdapter = EmployerJobsAdapter(
            items = employerActive,
            tabType = JobTabType.ACTIVE,
            onQrClick = { /* TODO: open camera for QR scan */ },
            onDuplicateClick = { item ->
                // TODO: navigate to PostJobFragment with item's data pre-filled
                // findNavController().navigate(
                //     R.id.action_myJobsFragment_to_postJobFragment,
                //     bundleOf("jobData" to item)
                // )
            }
        )
    }

    private fun setupPostJobButtons() {
        // "פרסום עבודה" card (top of list when history exists)
        binding.cardPostJob.setOnClickListener {
            // TODO: navigate to PostJobFragment (empty)
            // findNavController().navigate(R.id.action_myJobsFragment_to_postJobFragment)
        }

        // Empty state card (when no history exists yet)
        binding.cardEmptyHistory.setOnClickListener {
            // TODO: navigate to PostJobFragment (empty)
            // findNavController().navigate(R.id.action_myJobsFragment_to_postJobFragment)
        }
    }

    private fun setupTabs() {
        binding.tabActive.setOnClickListener {
            viewModel.currentTab = JobTabType.ACTIVE
            applyTab(JobTabType.ACTIVE)
        }
        binding.tabPending.setOnClickListener {
            viewModel.currentTab = JobTabType.PENDING
            applyTab(JobTabType.PENDING)
        }
        binding.tabHistory.setOnClickListener {
            viewModel.currentTab = JobTabType.HISTORY
            applyTab(JobTabType.HISTORY)
        }
    }

    private fun applyTab(tab: JobTabType) {
        val tabs = listOf(binding.tabActive, binding.tabPending, binding.tabHistory)
        tabs.forEach { setTabUnselected(it) }

        // Always reset rvJobs to visible first - updateEmployerHistoryUI() will
        // hide it again if needed (empty employer history). Without this reset,
        // once history sets it to GONE it stays GONE when switching to other tabs.
        binding.rvJobs.visibility = View.VISIBLE

        when (tab) {
            JobTabType.ACTIVE  -> setTabSelected(binding.tabActive)
            JobTabType.PENDING -> setTabSelected(binding.tabPending)
            JobTabType.HISTORY -> setTabSelected(binding.tabHistory)
        }

        if (viewModel.isWorkerMode) {
            val items = when (tab) {
                JobTabType.ACTIVE  -> workerActive.toList()
                JobTabType.PENDING -> workerPending.toList()
                JobTabType.HISTORY -> workerHistory.toList()
            }
            workerAdapter.updateItems(items, tab)
            // Worker mode never shows employer-only UI
            binding.cardPostJob.visibility    = View.GONE
            binding.cardEmptyHistory.visibility = View.GONE
        } else {
            val items = when (tab) {
                JobTabType.ACTIVE  -> employerActive.toList()
                JobTabType.PENDING -> employerPending.toList()
                JobTabType.HISTORY -> employerHistory.toList()
            }
            employerAdapter.updateItems(items, tab)
            binding.rvJobs.adapter = employerAdapter

            // Employer history: show either Empty State or PostJob button + list
            if (tab == JobTabType.HISTORY) {
                updateEmployerHistoryUI()
            } else {
                binding.cardPostJob.visibility    = View.GONE
                binding.cardEmptyHistory.visibility = View.GONE
            }
        }
    }

    // Decides whether to show Empty State card or "פרסום עבודה" button in employer history
    private fun updateEmployerHistoryUI() {
        if (employerHistory.isEmpty()) {
            binding.cardEmptyHistory.visibility = View.VISIBLE
            binding.cardPostJob.visibility      = View.GONE
            binding.rvJobs.visibility           = View.GONE
        } else {
            binding.cardEmptyHistory.visibility = View.GONE
            binding.cardPostJob.visibility      = View.VISIBLE
            binding.rvJobs.visibility           = View.VISIBLE
        }
    }

    private fun restoreTab(tab: JobTabType) {
        applyTab(tab)
    }

    private fun setupToggle() {
        binding.toggleWorker.setOnClickListener {
            if (!viewModel.isWorkerMode) {
                viewModel.isWorkerMode = true
                viewModel.currentTab = JobTabType.ACTIVE
                applyWorkerMode(animated = true)
                restoreTab(JobTabType.ACTIVE)
            }
        }
        binding.toggleEmployer.setOnClickListener {
            if (viewModel.isWorkerMode) {
                viewModel.isWorkerMode = false
                viewModel.currentTab = JobTabType.ACTIVE
                applyEmployerMode(animated = true)
                restoreTab(JobTabType.ACTIVE)
            }
        }
    }

    // Apply worker toggle visuals only (no tab/list changes)
    private fun applyWorkerMode(animated: Boolean) {
        appViewModel.setWorkerMode()
        binding.rvJobs.visibility = View.VISIBLE
        binding.cardPostJob.visibility = View.GONE
        binding.cardEmptyHistory.visibility = View.GONE
        binding.toggleWorker.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected)
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleEmployer.background = null
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        updateTabColors(isPink = true)
        binding.rvJobs.adapter = workerAdapter
        updateTabLabels()
    }

    // Apply employer toggle visuals only (no tab/list changes)
    private fun applyEmployerMode(animated: Boolean) {
        appViewModel.setEmployerMode()
        binding.toggleEmployer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected_teal)
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleWorker.background = null
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        updateTabColors(isPink = false)
        binding.rvJobs.adapter = employerAdapter
        updateTabLabels()
    }

    private fun updateTabLabels() {
        if (viewModel.isWorkerMode) {
            binding.tabActive.text  = "פעילות (${workerActive.size})"
            binding.tabPending.text = "בהמתנה (${workerPending.size})"
            binding.tabHistory.text = "היסטוריה"
        } else {
            binding.tabActive.text  = "מאוישות (${employerActive.size})"
            binding.tabPending.text = "בטיפול (${employerPending.size})"
            binding.tabHistory.text = "היסטוריה"
        }
    }

    private fun updateTabColors(isPink: Boolean) {
        listOf(binding.tabActive, binding.tabPending, binding.tabHistory)
            .forEach { setTabUnselected(it) }
        val selectedDrawable = if (isPink) R.drawable.bg_tab_selected else R.drawable.bg_tab_selected_teal
        binding.tabActive.background = ContextCompat.getDrawable(requireContext(), selectedDrawable)
        binding.tabActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setTabSelected(tab: TextView) {
        val selectedDrawable = if (viewModel.isWorkerMode)
            R.drawable.bg_tab_selected else R.drawable.bg_tab_selected_teal
        tab.background = ContextCompat.getDrawable(requireContext(), selectedDrawable)
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        tab.typeface = ResourcesCompat.getFont(requireContext(), R.font.ploni_bold_aaa)
    }

    private fun setTabUnselected(tab: TextView) {
        tab.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_unselected_box)
        tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
        tab.typeface = ResourcesCompat.getFont(requireContext(), R.font.ploni_regular_aaa)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}