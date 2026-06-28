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
import com.example.clickjob_finalproject.adapters.TimerType
import com.example.clickjob_finalproject.databinding.FragmentMyJobsBinding

class MyJobsFragment : Fragment() {

    private var _binding: FragmentMyJobsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyJobsViewModel by viewModels()
    private val appViewModel: AppViewModel by activityViewModels()

    // ===== Worker data =====
    private val workerActive = mutableListOf(
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "מחר", "הפקה ואירועים", needsApproval = true),
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "מחר", "מסעדות", timerType = TimerType.SOON),
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "מחר", "מכירות ואופנה", timerType = TimerType.PENDING)
    )
    private val workerHistory = mutableListOf(
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "אתמול", "אחזקה"),
        MyJobItem("שם משרה", "שם חברה", "50 ש h/00", "2.2 ק״מ", "אתמול", "בניין וייצור")
    )

    // ===== Employer data =====
    private val employerActive = mutableListOf(
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 7, 7, category = "מסעדות"),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 5, 5, category = "הפקה ואירועים"),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 4, 4, category = "חינוך והוראה")
    )
    private val employerPending = mutableListOf(
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 2, 6, countdownMillis = 18000000L, category = "מסעדות"),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 3, 7, countdownMillis = 18000000L, category = "אחזקה"),
        EmployerJobItem("מלצרית לחתונה", "שם חברה", 1, 3, countdownMillis = 18000000L, category = "טכנולוגיה")
    )

    // TEMP: employer history with dummy data for testing
    private val employerHistory = mutableListOf<EmployerJobItem>()
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

        // TEMP: always show toggle in employer mode for testing
        binding.toggleContainer.visibility = View.VISIBLE
        viewModel.isWorkerMode = false
        viewModel.currentTab = JobTabType.ACTIVE

        applyEmployerMode(animated = false)
        restoreTab(JobTabType.ACTIVE)
    }

    private fun setupKeyboard() {
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
            onQrClick = { item ->
                QrCodeDialog.newInstance(
                    jobTitle = item.title,
                    jobCompany = item.company,
                    category = item.category
                ).show(childFragmentManager, "QrCodeDialog")
            },
            onDuplicateClick = { item ->
                val bundle = Bundle().apply {
                    putString("jobTitle", item.title)
                    putString("jobCompany", item.company)
                    putString("jobPrice", item.price)
                    putInt("workersNeeded", item.workersNeeded)
                    putString("jobDescription", "")
                    putString("jobPhone", "")
                    putString("jobAddress", "")
                    putString("jobLink", "")
                }
                findNavController().navigate(
                    R.id.action_myJobsFragment_to_postJobFragment,
                    bundle
                )
            },
            onItemClick = { item ->
                if (viewModel.currentTab == JobTabType.PENDING) {
                    val bundle = Bundle().apply {
                        putString("jobTitle", item.title)
                        putString("jobCompany", item.company)
                        putInt("workersNeeded", item.workersNeeded)
                        putInt("workersRegistered", item.workersRegistered)
                    }
                    findNavController().navigate(
                        R.id.action_myJobsFragment_to_workerSortingFragment,
                        bundle
                    )
                }
            }
        )
    }

    private fun setupPostJobButtons() {
        binding.cardEmptyHistory.setOnClickListener {
            findNavController().navigate(R.id.action_myJobsFragment_to_postJobFragment)
        }
        binding.cardPostJob.setOnClickListener {
            findNavController().navigate(R.id.action_myJobsFragment_to_postJobFragment)
        }
    }

    private fun setupTabs() {
        binding.tabActive.setOnClickListener {
            viewModel.currentTab = JobTabType.ACTIVE
            applyTab(JobTabType.ACTIVE)
        }
        binding.tabHistory.setOnClickListener {
            viewModel.currentTab = JobTabType.HISTORY
            applyTab(JobTabType.HISTORY)
        }
    }

    private fun applyTab(tab: JobTabType) {
        listOf(binding.tabActive, binding.tabHistory).forEach { setTabUnselected(it) }

        binding.rvJobs.visibility = View.VISIBLE

        when (tab) {
            JobTabType.ACTIVE  -> setTabSelected(binding.tabActive)
            JobTabType.HISTORY -> setTabSelected(binding.tabHistory)
            else -> {}
        }

        if (viewModel.isWorkerMode) {
            val items = when (tab) {
                JobTabType.ACTIVE  -> workerActive.toList()
                JobTabType.HISTORY -> workerHistory.toList()
                else -> workerActive.toList()
            }
            workerAdapter.updateItems(items, tab)
            binding.cardPostJob.visibility      = View.GONE
            binding.cardEmptyHistory.visibility = View.GONE
        } else {
            val items = when (tab) {
                JobTabType.ACTIVE  -> employerActive.toList()
                JobTabType.PENDING -> employerPending.toList()
                JobTabType.HISTORY -> employerHistory.toList()
            }
            employerAdapter.updateItems(items, tab)
            binding.rvJobs.adapter = employerAdapter

            if (tab == JobTabType.HISTORY) {
                updateEmployerHistoryUI()
            } else {
                binding.cardPostJob.visibility      = View.GONE
                binding.cardEmptyHistory.visibility = View.GONE
            }
        }
    }

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

    private fun applyWorkerMode(animated: Boolean) {
        appViewModel.setWorkerMode()
        binding.rvJobs.visibility           = View.VISIBLE
        binding.cardPostJob.visibility      = View.GONE
        binding.cardEmptyHistory.visibility = View.GONE
        binding.toggleWorker.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected)
        binding.toggleWorker.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.toggleEmployer.background = null
        binding.toggleEmployer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
        updateTabColors(isPink = true)
        binding.rvJobs.adapter = workerAdapter
        updateTabLabels()
    }

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
            binding.tabHistory.text = "היסטוריה"
        } else {
            binding.tabActive.text  = "מאוישות (${employerActive.size})"
            binding.tabHistory.text = "היסטוריה"
        }
    }

    private fun updateTabColors(isPink: Boolean) {
        listOf(binding.tabActive, binding.tabHistory).forEach { setTabUnselected(it) }
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