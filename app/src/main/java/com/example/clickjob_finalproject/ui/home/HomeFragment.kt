package com.example.clickjob_finalproject.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.JobAdapter
import com.example.clickjob_finalproject.adapters.JobItem
import com.example.clickjob_finalproject.adapters.ShiftAdapter
import com.example.clickjob_finalproject.adapters.ShiftItem
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var loadedCount = 0
    private val totalToLoad = 3   // The three sections: upcoming shifts, best match, urgent
    private var screenShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.INVISIBLE
        loadedCount = 0
        screenShown = false

        setupSearchBar()
        setupJobPosting()
        setupEmptyShiftCard()

        loadUserLocation()
        setupUpcomingShifts()
        setupBestMatchList()
        setupUrgentList()
    }

    private fun onSectionLoaded() {
        loadedCount++
        Log.d("HOME_DEBUG", "onSectionLoaded called, count = $loadedCount")

        if (loadedCount >= totalToLoad && !screenShown) {
            screenShown = true
            _binding?.let {
                it.progressBar.visibility = View.GONE
                it.scrollView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupJobPosting() {
        binding.btnPostJob.setOnClickListener {
            val args = bundleOf("openEmployerHistory" to true)
            findNavController().navigate(R.id.action_homeFragment_to_myJobsFragment, args)
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.isFocusable = false
        binding.etSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setupEmptyShiftCard() {
        binding.cardNoUpcomingShifts.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun loadUserLocation() {
        UserRepository.getUserProfile(
            onSuccess = { profile ->
                // The binding may be null if the user left the screen in the meantime
                if (profile.address.isNotEmpty()) {
                    _binding?.tvLocation?.text = profile.address
                }
            },
            onFailure = { }   // The address is not critical — do nothing if it fails
        )
    }

    private fun setupUpcomingShifts() {
        UserRepository.getUpcomingShifts(
            onSuccess = { jobs ->
                if (jobs.isEmpty()) {
                    binding.tvSectionUpcoming.visibility = View.GONE
                    binding.vpUpcoming.visibility = View.GONE
                    binding.layoutDots.visibility = View.GONE
                    binding.cardNoUpcomingShifts.visibility = View.VISIBLE
                    onSectionLoaded()
                    return@getUpcomingShifts
                }

                val dateFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                val todayCalendar = java.util.Calendar.getInstance()
                val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }

                val items = jobs.map { job ->
                    val jobCalendar = java.util.Calendar.getInstance().apply {
                        timeInMillis = job.date
                    }
                    val dateLabel = when {
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                        else -> dateFormat.format(java.util.Date(job.date))
                    }
                    ShiftItem(
                        id = job.id,
                        title = job.title,
                        company = job.company,
                        time = "${job.startTime} - ${job.endTime}",
                        date = dateLabel,
                        address = job.address,
                        category = job.category
                    )
                }

                binding.cardNoUpcomingShifts.visibility = View.GONE
                binding.tvSectionUpcoming.visibility = View.VISIBLE
                binding.vpUpcoming.visibility = View.VISIBLE
                binding.layoutDots.visibility = View.VISIBLE

                binding.vpUpcoming.adapter = ShiftAdapter(items) { shift ->
                    val args = bundleOf("jobId" to shift.id, "isViewOnly" to true)
                    findNavController().navigate(
                        R.id.action_homeFragment_to_jobDetailsFragment,
                        args
                    )
                }

                setupDots(items.size)
                binding.vpUpcoming.registerOnPageChangeCallback(
                    object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            updateDots(position)
                        }
                    }
                )
                onSectionLoaded()
            },
            onFailure = { onSectionLoaded() }
        )
    }

    private fun setupDots(count: Int) {
        binding.layoutDots.removeAllViews()
        val size = (8 * resources.displayMetrics.density).toInt()
        val margin = (4 * resources.displayMetrics.density).toInt()
        for (i in 0 until count) {
            val dot = ImageView(requireContext())
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            dot.layoutParams = params
            dot.setImageResource(if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive)
            binding.layoutDots.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        for (i in 0 until binding.layoutDots.childCount) {
            val dot = binding.layoutDots.getChildAt(i) as ImageView
            dot.setImageResource(if (i == selected) R.drawable.dot_active else R.drawable.dot_inactive)
        }
    }

    private fun setupBestMatchList() {
        UserRepository.getUserProfile(
            onSuccess = { profile ->

                Log.d("HOME_DEBUG", "BestMatch: profile loaded")
                Log.d("HOME_DEBUG", "BestMatch: jobMatches size = ${profile.jobMatches.size}")

                if (profile.jobMatches.isEmpty()) {
                    binding.tvSectionBestMatch.visibility = View.GONE
                    binding.rvBestMatch.visibility = View.GONE
                    onSectionLoaded()
                    return@getUserProfile
                }

                UserRepository.getBestMatchJobs(
                    jobMatches = profile.jobMatches,
                    onSuccess = { jobsWithMatch ->

                        if (jobsWithMatch.isEmpty()) {
                            binding.tvSectionBestMatch.visibility = View.GONE
                            binding.rvBestMatch.visibility = View.GONE
                            onSectionLoaded()
                            return@getBestMatchJobs
                        }

                        val todayCalendar = java.util.Calendar.getInstance()
                        val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                            add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }

                        val employerIds = jobsWithMatch.map { it.first.employerId }.distinct()

                        if (employerIds.isEmpty()) {
                            val items = jobsWithMatch.map { (job, matchPercent) ->
                                val jobCalendar = java.util.Calendar.getInstance().apply {
                                    timeInMillis = job.date
                                }
                                val dateLabel = when {
                                    jobCalendar.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
                                            jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                                    jobCalendar.get(java.util.Calendar.YEAR) == tomorrowCalendar.get(java.util.Calendar.YEAR) &&
                                            jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                                    else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(job.date))
                                }
                                JobItem(
                                    title = job.title,
                                    company = job.company,
                                    price = "₪${job.salary}",
                                    rating = "",
                                    distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                                    date = dateLabel,
                                    matchPercent = "$matchPercent%",
                                    isUrgent = job.isUrgent,
                                    category = job.category,
                                    id = job.id
                                )
                            }

                            binding.tvSectionBestMatch.visibility = View.VISIBLE
                            binding.rvBestMatch.visibility = View.VISIBLE
                            binding.rvBestMatch.layoutManager = LinearLayoutManager(
                                requireContext(), LinearLayoutManager.HORIZONTAL, false
                            )
                            binding.rvBestMatch.adapter = JobAdapter(items) { job -> openJobDetails(job) }

                            onSectionLoaded()
                            return@getBestMatchJobs
                        }

                        val employerRatings = mutableMapOf<String, String>()
                        var loadedEmployers = 0

                        employerIds.forEach { employerId ->
                            UserRepository.getUserProfileById(
                                userId = employerId,
                                onSuccess = { empProfile ->
                                    if (empProfile.rating > 0) {
                                        employerRatings[employerId] = String.format("%.1f", empProfile.rating)
                                    }
                                    loadedEmployers++
                                    if (loadedEmployers == employerIds.size) {
                                        showBestMatch(jobsWithMatch, employerRatings, todayCalendar, tomorrowCalendar)
                                    }
                                },
                                onFailure = { e ->
                                    Log.e("HOME_DEBUG", "BestMatch: failed loading employer $employerId: ${e.message}")
                                    loadedEmployers++
                                    if (loadedEmployers == employerIds.size) {
                                        showBestMatch(jobsWithMatch, employerRatings, todayCalendar, tomorrowCalendar)
                                    }
                                }
                            )
                        }
                    },
                    onFailure = { e ->
                        Log.e("HOME_DEBUG", "BestMatch: getBestMatchJobs failed: ${e.message}")
                        binding.tvSectionBestMatch.visibility = View.GONE
                        binding.rvBestMatch.visibility = View.GONE
                        onSectionLoaded()
                    }
                )
            },
            onFailure = { e ->
                Log.e("HOME_DEBUG", "BestMatch: getUserProfile failed: ${e.message}")
                binding.tvSectionBestMatch.visibility = View.GONE
                binding.rvBestMatch.visibility = View.GONE
                onSectionLoaded()
            }
        )
    }


    private fun showBestMatch(
        jobsWithMatch: List<Pair<com.example.clickjob_finalproject.data.model.JobPost, Int>>,
        employerRatings: Map<String, String>,
        todayCalendar: java.util.Calendar,
        tomorrowCalendar: java.util.Calendar
    ) {
        if (_binding == null) return

        val items = jobsWithMatch.map { (job, matchPercent) ->
            val jobCalendar = java.util.Calendar.getInstance().apply { timeInMillis = job.date }
            val dateLabel = when {
                jobCalendar.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                jobCalendar.get(java.util.Calendar.YEAR) == tomorrowCalendar.get(java.util.Calendar.YEAR) &&
                        jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(job.date))
            }
            JobItem(
                title = job.title,
                company = job.company,
                price = "₪${job.salary}",
                rating = employerRatings[job.employerId] ?: "",
                distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                date = dateLabel,
                matchPercent = "$matchPercent%",
                isUrgent = job.isUrgent,
                category = job.category,
                id = job.id
            )
        }

        binding.tvSectionBestMatch.visibility = View.VISIBLE
        binding.rvBestMatch.visibility = View.VISIBLE
        binding.rvBestMatch.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvBestMatch.adapter = JobAdapter(items) { job -> openJobDetails(job) }

        onSectionLoaded()
    }

    private fun setupUrgentList() {
        UserRepository.getUrgentJobs(
            onSuccess = { jobs ->
                val now = System.currentTimeMillis()

                // Show only urgent jobs whose shift hasn't started yet.
                // The calculation combines the work day (job.date) with the start time (startTime).
                val futureUrgentJobs = jobs
                    .filter { job ->
                        job.isUrgent && getJobStartMillis(job) > now
                    }
                    .sortedBy { job -> getJobStartMillis(job) }

                if (futureUrgentJobs.isEmpty()) {
                    binding.tvSectionUrgent.visibility = View.GONE
                    binding.rvUrgent.visibility = View.GONE
                    onSectionLoaded()
                    return@getUrgentJobs
                }

                val todayCalendar = java.util.Calendar.getInstance()
                val tomorrowCalendar = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }

                val employerIds = futureUrgentJobs.map { it.employerId }.distinct()
                val employerRatings = mutableMapOf<String, String>()
                var loadedEmployers = 0

                employerIds.forEach { employerId ->
                    UserRepository.getUserProfileById(
                        userId = employerId,
                        onSuccess = { profile ->
                            if (profile.rating > 0) {
                                employerRatings[employerId] = String.format("%.1f", profile.rating)
                            }
                            loadedEmployers++
                            if (loadedEmployers == employerIds.size) {
                                showUrgent(
                                    futureUrgentJobs,
                                    employerRatings,
                                    todayCalendar,
                                    tomorrowCalendar
                                )
                            }
                        },
                        onFailure = { e ->
                            Log.e("DEBUG", "Failed to load employer $employerId: ${e.message}")
                            loadedEmployers++
                            if (loadedEmployers == employerIds.size) {
                                showUrgent(
                                    futureUrgentJobs,
                                    employerRatings,
                                    todayCalendar,
                                    tomorrowCalendar
                                )
                            }
                        }
                    )
                }
            },
            onFailure = { onSectionLoaded() }
        )
    }

    /**
     * Returns the shift start time in milliseconds.
     * job.date holds the work day and startTime holds the time in HH:mm format.
     */
    private fun getJobStartMillis(
        job: com.example.clickjob_finalproject.data.model.JobPost
    ): Long {
        val timeParts = job.startTime.trim().split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return job.date
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: return job.date

        return java.util.Calendar.getInstance().apply {
            timeInMillis = job.date
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun showUrgent(
        jobs: List<com.example.clickjob_finalproject.data.model.JobPost>,
        employerRatings: Map<String, String>,
        todayCalendar: java.util.Calendar,
        tomorrowCalendar: java.util.Calendar
    ) {
        if (_binding == null) return

        val items = jobs
            .sortedBy { getJobStartMillis(it) }
            .map { job ->
                val jobCalendar = java.util.Calendar.getInstance().apply { timeInMillis = job.date }
                val dateLabel = when {
                    jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "היום"
                    jobCalendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "מחר"
                    else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(java.util.Date(job.date))
                }
                JobItem(
                    title = job.title,
                    company = job.company,
                    price = "₪${job.salary}",
                    rating = employerRatings[job.employerId] ?: "",
                    distance = job.address.split(",").lastOrNull()?.trim() ?: job.address,
                    date = dateLabel,
                    matchPercent = null,
                    isUrgent = true,
                    category = job.category,
                    id = job.id
                )
            }

        binding.tvSectionUrgent.visibility = View.VISIBLE
        binding.rvUrgent.visibility = View.VISIBLE
        binding.rvUrgent.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvUrgent.adapter = JobAdapter(items) { job -> openJobDetails(job) }

        onSectionLoaded()
    }

    private fun openJobDetails(job: JobItem) {
        val args = bundleOf("jobId" to job.id)
        findNavController().navigate(R.id.action_homeFragment_to_jobDetailsFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}