package com.example.clickjob_finalproject.ui.home

import android.os.Bundle
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
import com.example.clickjob_finalproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        setupSearchBar()
        setupUpcomingShifts()
        setupBestMatchList()
        setupUrgentList()
    }

    private fun setupSearchBar() {
        binding.etSearch.isFocusable = false
        binding.etSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setupUpcomingShifts() {
        val items = listOf(
            ShiftItem("שם משרה", "שם חברה", "07:00–15:00", "dd/mm/yy"),
            ShiftItem("שם משרה", "שם חברה", "15:00–23:00", "dd/mm/yy"),
            ShiftItem("שם משרה", "שם חברה", "08:00–16:00", "dd/mm/yy")
        )

        // No open shifts -> hide section title, carousel and dots
        if (items.isEmpty()) {
            binding.tvSectionUpcoming.visibility = View.GONE
            binding.vpUpcoming.visibility = View.GONE
            binding.layoutDots.visibility = View.GONE
            return
        }

        binding.vpUpcoming.adapter = ShiftAdapter(items)

        // Build the dots manually
        setupDots(items.size)
        binding.vpUpcoming.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateDots(position)
                }
            }
        )
    }

    // Create one dot ImageView per shift
    private fun setupDots(count: Int) {
        binding.layoutDots.removeAllViews()
        val size = (8 * resources.displayMetrics.density).toInt()
        val margin = (4 * resources.displayMetrics.density).toInt()
        for (i in 0 until count) {
            val dot = ImageView(requireContext())
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(margin, 0, margin, 0)
            dot.layoutParams = params
            dot.setImageResource(
                if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive
            )
            binding.layoutDots.addView(dot)
        }
    }

    // Highlight the dot for the current page
    private fun updateDots(selected: Int) {
        for (i in 0 until binding.layoutDots.childCount) {
            val dot = binding.layoutDots.getChildAt(i) as ImageView
            dot.setImageResource(
                if (i == selected) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }

    private fun setupBestMatchList() {
        val items = listOf(
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "מחר", "90%", id = "best_1"),
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "מחר", null, id = "best_2"),
            JobItem("שם משרה", "EventPro הפקות", "₪45", "4.2", "1.5 ק״מ", "היום", "87%", id = "best_3")
        )

        binding.rvBestMatch.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvBestMatch.adapter = JobAdapter(items) { job ->
            openJobDetails(job)
        }
    }

    private fun setupUrgentList() {
        val items = listOf(
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "היום", null, id = "urgent_1"),
            JobItem("שם משרה", "EventPro הפקות", "₪50", "4.7", "2.2 ק״מ", "מחר", null, id = "urgent_2"),
            JobItem("שם משרה", "EventPro הפקות", "₪55", "4.5", "0.8 ק״מ", "היום", null, id = "urgent_3")
        )

        binding.rvUrgent.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvUrgent.adapter = JobAdapter(items) { job ->
            openJobDetails(job)
        }
    }

    // Navigates to the job details screen, passing the clicked job's id as an argument.
    // Requires action_homeFragment_to_jobDetailsFragment to exist in the nav graph.
    private fun openJobDetails(job: JobItem) {
        val args = bundleOf("jobId" to job.id)
        findNavController().navigate(R.id.action_homeFragment_to_jobDetailsFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}