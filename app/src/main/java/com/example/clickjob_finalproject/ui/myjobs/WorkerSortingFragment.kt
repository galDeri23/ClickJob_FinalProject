package com.example.clickjob_finalproject.ui.myjobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.EmployerJobsAdapter
import com.example.clickjob_finalproject.adapters.WorkerItem
import com.example.clickjob_finalproject.adapters.WorkerSortingAdapter
import com.google.android.material.imageview.ShapeableImageView

class WorkerSortingFragment : Fragment() {

    private val appliedWorkers = listOf(
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com"),
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com"),
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com"),
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com"),
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com")
    )

    private val acceptedWorkers = listOf(
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com", isAccepted = true),
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com", isAccepted = true),
        WorkerItem("שם עובד", "פרטים על התפקיד", "0525381648", "mail@mail.com", isAccepted = true)
    )

    private lateinit var rvWorkers: RecyclerView
    private lateinit var tabApplied: TextView
    private lateinit var tabAccepted: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_worker_sorting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvWorkers = view.findViewById(R.id.rvWorkers)
        tabApplied = view.findViewById(R.id.tabApplied)
        tabAccepted = view.findViewById(R.id.tabAccepted)

        rvWorkers.layoutManager = LinearLayoutManager(requireContext())

        // Receive job data from MyJobsFragment
        val jobTitle = arguments?.getString("jobTitle") ?: ""
        val jobCompany = arguments?.getString("jobCompany") ?: ""
        val jobCategory = arguments?.getString("jobCategory") ?: "מסעדות"
        val workersNeeded = arguments?.getInt("workersNeeded") ?: 0
        val workersRegistered = arguments?.getInt("workersRegistered") ?: 0

        // Set stats
        view.findViewById<TextView>(R.id.tvAppliedCount).text = "כמה הגישו: $workersRegistered"
        view.findViewById<TextView>(R.id.tvNeededCount).text = "כמה אני צריך: $workersNeeded"

        // Set job card data and hide progress bar
        val jobCard = view.findViewById<View>(R.id.jobCard)
        jobCard.post {
            jobCard.findViewById<TextView>(R.id.tvJobTitle)?.text = jobTitle
            jobCard.findViewById<TextView>(R.id.tvDatePrice)?.text = jobCompany
            jobCard.findViewById<View>(R.id.bottomRow)?.visibility = View.GONE
            jobCard.findViewById<View>(R.id.progressWorkers)?.visibility = View.GONE
            jobCard.findViewById<ShapeableImageView>(R.id.imgCategory)
                ?.setImageResource(EmployerJobsAdapter.getCategoryImage(jobCategory))
        }

        // Default tab
        showApplied()

        tabApplied.setOnClickListener {
            tabApplied.setBackgroundResource(R.drawable.bg_tab_selected_teal)
            tabApplied.setTextColor(resources.getColor(R.color.white, null))
            tabAccepted.setBackgroundResource(R.drawable.bg_tab_unselected_sorting)
            tabAccepted.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            showApplied()
        }

        tabAccepted.setOnClickListener {
            tabAccepted.setBackgroundResource(R.drawable.bg_tab_selected_teal)
            tabAccepted.setTextColor(resources.getColor(R.color.white, null))
            tabApplied.setBackgroundResource(R.drawable.bg_tab_unselected_sorting)
            tabApplied.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            showAccepted()
        }

        view.findViewById<View>(R.id.ivBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showApplied() {
        rvWorkers.adapter = WorkerSortingAdapter(
            workers = appliedWorkers,
            showCancelButton = false,
            onWorkerClick = { showWorkerDialog(it) },
            onCancelClick = {}
        )
    }

    private fun showAccepted() {
        rvWorkers.adapter = WorkerSortingAdapter(
            workers = acceptedWorkers,
            showCancelButton = true,
            onWorkerClick = { showWorkerDialog(it) },
            onCancelClick = { /* TODO: cancel worker */ }
        )
    }

    private fun showWorkerDialog(worker: WorkerItem) {
        val dialog = WorkerDetailsDialog(worker)
        dialog.show(parentFragmentManager, "worker_details")
    }
}