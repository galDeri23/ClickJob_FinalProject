package com.example.clickjob_finalproject.ui.myjobs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clickjob_finalproject.AppViewModel
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.EmployerJobItem
import com.example.clickjob_finalproject.adapters.EmployerJobsAdapter
import com.example.clickjob_finalproject.adapters.JobTabType
import com.example.clickjob_finalproject.adapters.MyJobItem
import com.example.clickjob_finalproject.adapters.MyJobsAdapter
import com.example.clickjob_finalproject.adapters.TimerType
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentMyJobsBinding

class MyJobsFragment : Fragment() {

    private var _binding: FragmentMyJobsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyJobsViewModel by viewModels()
    private val appViewModel: AppViewModel by activityViewModels()

    // ===== Worker data =====
    private val workerActive = mutableListOf<MyJobItem>()
    private val workerHistory = mutableListOf<MyJobItem>()

    // ===== Employer data =====
    private val employerActive = mutableListOf<EmployerJobItem>()
    private val employerPending = mutableListOf(
        EmployerJobItem(id = "1", title = "מלצרית לחתונה", company = "שם חברה", workersRegistered = 2, workersNeeded = 6, countdownMillis = 18000000L, category = "מסעדות"),
        EmployerJobItem(id = "2", title = "מלצרית לחתונה", company = "שם חברה", workersRegistered = 3, workersNeeded = 7, countdownMillis = 18000000L, category = "אחזקה"),
        EmployerJobItem(id = "3", title = "מלצרית לחתונה", company = "שם חברה", workersRegistered = 1, workersNeeded = 3, countdownMillis = 18000000L, category = "טכנולוגיה")
    )
    private val employerHistory = mutableListOf<EmployerJobItem>()

    private lateinit var workerAdapter: MyJobsAdapter
    private lateinit var employerAdapter: EmployerJobsAdapter
    private var jobsListener: com.google.firebase.firestore.ListenerRegistration? = null

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

        val openEmployerHistory = arguments?.getBoolean("openEmployerHistory", false) ?: false

        if (openEmployerHistory) {
            binding.toggleContainer.visibility = View.VISIBLE
            viewModel.isWorkerMode = false
            applyEmployerMode(animated = false)
            viewModel.currentTab = JobTabType.HISTORY
            restoreTab(JobTabType.HISTORY)
            loadEmployerJobs()
        } else {
            checkUserMode()
        }
    }

    private fun checkUserMode() {
        UserRepository.getUserProfile(
            onSuccess = { profile ->
                if (profile.hasPostedJob) {
                    binding.toggleContainer.visibility = View.VISIBLE
                    binding.toggleContainer.requestLayout()
                    val isEmployer = getSavedMode()
                    viewModel.isWorkerMode = !isEmployer
                    if (isEmployer) {
                        applyEmployerMode(animated = false)
                        loadEmployerJobs()
                    } else {
                        applyWorkerMode(animated = false)
                    }
                    viewModel.currentTab = JobTabType.ACTIVE
                    restoreTab(JobTabType.ACTIVE)
                } else {
                    binding.toggleContainer.visibility = View.INVISIBLE
                    viewModel.isWorkerMode = true
                    applyWorkerMode(animated = false)
                    viewModel.currentTab = JobTabType.ACTIVE
                    restoreTab(JobTabType.ACTIVE)
                }
            },
            onFailure = {
                binding.toggleContainer.visibility = View.INVISIBLE
                viewModel.isWorkerMode = true
                applyWorkerMode(animated = false)
                restoreTab(JobTabType.ACTIVE)
            }
        )
    }

    // Loads worker applications from Firestore and converts to MyJobItem
    private fun loadWorkerJobs() {
        UserRepository.getWorkerApplications(
            onSuccess = { applications ->
                val now = System.currentTimeMillis()
                val activeItems = mutableListOf<MyJobItem>()
                val historyItems = mutableListOf<MyJobItem>()

                if (applications.isEmpty()) {
                    workerActive.clear()
                    workerHistory.clear()
                    workerAdapter.updateItems(emptyList(), JobTabType.ACTIVE)
                    updateTabLabels()
                    return@getWorkerApplications
                }

                var pending = applications.size

                applications.forEach { application ->
                    UserRepository.getJobById(
                        jobId = application.jobId,
                        onSuccess = { job ->
                            val jobCalendar = java.util.Calendar.getInstance().apply {
                                timeInMillis = job.date
                            }
                            val todayCalendar = java.util.Calendar.getInstance()
                            val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                                add(java.util.Calendar.DAY_OF_YEAR, 1)
                            }
                            val dateLabel = when {
                                jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                                jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                                else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(job.date))
                            }

                            val startHour = job.startTime.split(":")[0].toIntOrNull() ?: 0
                            val endHour = job.endTime.split(":")[0].toIntOrNull() ?: 0

                            val shiftStartMillis = java.util.Calendar.getInstance().apply {
                                timeInMillis = job.date
                                set(java.util.Calendar.HOUR_OF_DAY, startHour)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                            }.timeInMillis

                            val shiftEndMillis = java.util.Calendar.getInstance().apply {
                                timeInMillis = job.date
                                set(java.util.Calendar.HOUR_OF_DAY, endHour)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                            }.timeInMillis

                            val item = MyJobItem(
                                id = application.id,
                                jobId = job.id,
                                applicationId = application.id,
                                title = job.title,
                                company = job.company,
                                price = "₪${job.salary}/לשעה",
                                distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                                day = dateLabel,
                                category = job.category,
                                needsApproval = application.status == "employer_approved",
                                timerType = when (application.status) {
                                    "pending" -> TimerType.PENDING
                                    "confirmed" -> TimerType.SOON
                                    else -> TimerType.NONE
                                },
                                shiftStartMillis = shiftStartMillis,
                                shiftEndMillis = shiftEndMillis
                            )

                            when {
                                // Shift ended - goes to history
                                application.status == "confirmed" && shiftEndMillis < now ->
                                    historyItems.add(item)
                                // Rejected - don't show
                                application.status == "rejected" -> { }
                                // Everything else - active
                                else -> activeItems.add(item)
                            }

                            pending--
                            if (pending == 0) {
                                workerActive.clear()
                                workerActive.addAll(activeItems)
                                workerHistory.clear()
                                workerHistory.addAll(historyItems)
                                if (viewModel.isWorkerMode) {
                                    workerAdapter.updateItems(workerActive.toList(), JobTabType.ACTIVE)
                                }
                                updateTabLabels()
                            }
                        },
                        onFailure = {
                            pending--
                            if (pending == 0) updateTabLabels()
                        }
                    )
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה בטעינת משרות", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadEmployerJobs() {
        jobsListener?.remove()
        jobsListener = UserRepository.listenToEmployerJobs(
            onUpdate = { jobs ->
                val now = System.currentTimeMillis()

                val activeItems = mutableListOf<EmployerJobItem>()
                val historyItems = mutableListOf<EmployerJobItem>()

                jobs.forEach { job ->
                    val jobCalendar = java.util.Calendar.getInstance().apply {
                        timeInMillis = job.date
                    }
                    val todayCalendar = java.util.Calendar.getInstance()
                    val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }

                    val dateLabel = when {
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                        else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(job.date))
                    }

                    val startHour = job.startTime.split(":")[0].toIntOrNull() ?: 0
                    val endHour = job.endTime.split(":")[0].toIntOrNull() ?: 0

                    val shiftStartMillis = java.util.Calendar.getInstance().apply {
                        timeInMillis = job.date
                        set(java.util.Calendar.HOUR_OF_DAY, startHour)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                    }.timeInMillis

                    val shiftEndMillis = java.util.Calendar.getInstance().apply {
                        timeInMillis = job.date
                        set(java.util.Calendar.HOUR_OF_DAY, endHour)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                    }.timeInMillis

                    val countdownMillis = (shiftStartMillis - now).coerceAtLeast(0L)
                    val isCompleted = shiftEndMillis < now

                    val item = EmployerJobItem(
                        id = job.id,
                        title = job.title,
                        company = job.company,
                        workersRegistered = job.workersRegistered,
                        workersNeeded = job.workersNeeded,
                        date = dateLabel,
                        price = job.salary,
                        category = job.category,
                        countdownMillis = countdownMillis
                    )

                    if (isCompleted) historyItems.add(item)
                    else activeItems.add(item)
                }

                employerActive.clear()
                employerActive.addAll(activeItems.sortedBy { it.countdownMillis })
                employerHistory.clear()
                employerHistory.addAll(historyItems)

                employerAdapter.updateItems(employerActive.toList(), JobTabType.ACTIVE)
                updateTabLabels()
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה בטעינת משרות", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveUserMode(isEmployer: Boolean) {
        requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("is_employer_mode", isEmployer).apply()
    }

    private fun getSavedMode(): Boolean {
        return requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_employer_mode", false)
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
            items = emptyList(),
            tabType = JobTabType.ACTIVE,
            onApproveClick = { },
            onItemClick = { item ->
                if (item.needsApproval) {
                    findNavController().navigate(
                        R.id.action_myJobsFragment_to_jobDetailsFragment,
                        bundleOf(
                            "jobId" to item.jobId,
                            "applicationId" to item.applicationId
                        )
                    )
                }
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
                    category = item.category,
                    jobId = item.id
                ).show(childFragmentManager, "QrCodeDialog")
            },
            onDuplicateClick = { item ->
                findNavController().navigate(
                    R.id.action_myJobsFragment_to_postJobFragment,
                    bundleOf(
                        "jobTitle" to item.title,
                        "jobCompany" to item.company,
                        "jobPrice" to item.price,
                        "workersNeeded" to item.workersNeeded,
                        "jobDescription" to "",
                        "jobPhone" to "",
                        "jobAddress" to "",
                        "jobLink" to ""
                    )
                )
            },
            onItemClick = { item ->
                val bundle = bundleOf(
                    "jobId" to item.id,
                    "jobTitle" to item.title,
                    "jobCompany" to item.company,
                    "jobCategory" to item.category,
                    "jobDate" to item.date,
                    "jobPrice" to item.price,
                    "workersNeeded" to item.workersNeeded
                )
                findNavController().navigate(
                    R.id.action_myJobsFragment_to_workerSortingFragment,
                    bundle
                )
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
                saveUserMode(isEmployer = false)
                applyWorkerMode(animated = true)
                restoreTab(JobTabType.ACTIVE)
            }
        }
        binding.toggleEmployer.setOnClickListener {
            if (viewModel.isWorkerMode) {
                viewModel.isWorkerMode = false
                viewModel.currentTab = JobTabType.ACTIVE
                saveUserMode(isEmployer = true)
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
        loadWorkerJobs()
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
            binding.tabActive.text  = "פעילות (${employerActive.size})"
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
        jobsListener?.remove()
        _binding = null
    }
}