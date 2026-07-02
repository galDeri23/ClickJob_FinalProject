package com.example.clickjob_finalproject.ui.myjobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.EmployerJobsAdapter
import com.example.clickjob_finalproject.adapters.WorkerItem
import com.example.clickjob_finalproject.adapters.WorkerSortingAdapter
import com.example.clickjob_finalproject.data.model.Application
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.google.android.material.imageview.ShapeableImageView

class WorkerSortingFragment : Fragment() {

    private lateinit var rvWorkers: RecyclerView
    private lateinit var tabApplied: TextView
    private lateinit var tabAccepted: TextView

    private var jobId: String = ""
    private var jobTitle: String = ""
    private var jobCompany: String = ""
    private var jobCategory: String = "מסעדות"
    private var jobDate: String = ""
    private var jobPrice: String = ""
    private var workersNeeded: Int = 0

    private var appliedWorkers = listOf<WorkerItem>()
    private var acceptedWorkers = listOf<WorkerItem>()
    private var allApplications = listOf<Application>()
    private var bottomNav: View? = null

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

        jobId = arguments?.getString("jobId") ?: ""
        jobTitle = arguments?.getString("jobTitle") ?: ""
        jobCompany = arguments?.getString("jobCompany") ?: ""
        jobCategory = arguments?.getString("jobCategory") ?: "מסעדות"
        jobDate = arguments?.getString("jobDate") ?: ""
        jobPrice = arguments?.getString("jobPrice") ?: ""
        workersNeeded = arguments?.getInt("workersNeeded") ?: 0

        setupJobCard(view)
        setupTabs()
        setupBackButton(view)
        loadApplications()
    }

    private fun setupJobCard(view: View) {
        view.findViewById<TextView>(R.id.tvNeededCount).text = "כמה אני צריך: $workersNeeded"

        val jobCard = view.findViewById<View>(R.id.jobCard)
        jobCard.post {
            jobCard.findViewById<TextView>(R.id.tvJobTitle)?.text = jobTitle
            jobCard.findViewById<TextView>(R.id.tvCompanyName)?.text = jobCompany
            jobCard.findViewById<TextView>(R.id.tvDate)?.text = jobDate
            jobCard.findViewById<TextView>(R.id.tvPrice)?.text = "₪$jobPrice"
            jobCard.findViewById<View>(R.id.bottomRow)?.visibility = View.GONE
            jobCard.findViewById<View>(R.id.progressWorkers)?.visibility = View.GONE
            jobCard.findViewById<ShapeableImageView>(R.id.imgCategory)
                ?.setImageResource(EmployerJobsAdapter.getCategoryImage(jobCategory))
        }
    }

    private fun setupBackButton(view: View) {
        view.findViewById<View>(R.id.ivBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadApplications() {
        if (jobId.isEmpty()) return

        UserRepository.getJobApplications(
            jobId = jobId,
            onSuccess = { applications ->
                allApplications = applications

                // Applied tab: pending + employer_approved (waiting for worker's double-check)
                appliedWorkers = applications
                    .filter { it.status == "pending" || it.status == "employer_approved" }
                    .map { it.toWorkerItem(isPending = it.status == "employer_approved") }

                // Accepted tab: only workers who confirmed the job themselves
                acceptedWorkers = applications
                    .filter { it.status == "confirmed" }
                    .map { it.toWorkerItem(isAccepted = true) }

                // Update tab labels with real counts
                tabApplied.text = "הגישו (${appliedWorkers.size})"
                tabAccepted.text = "התקבלו (${acceptedWorkers.size})"

                view?.findViewById<TextView>(R.id.tvAppliedCount)?.text =
                    "כמה הגישו: ${applications.size}"

                showApplied()
            },
            onFailure = {
                Toast.makeText(requireContext(), "שגיאה בטעינת מועמדים", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun Application.toWorkerItem(
        isAccepted: Boolean = false,
        isPending: Boolean = false
    ): WorkerItem {
        return WorkerItem(
            applicationId = id,
            workerId = workerId,
            name = workerName,
            role = "",
            phone = workerPhone,
            email = "",
            bio = workerBio,
            profileImageUrl = workerProfileImageUrl,
            isAccepted = isAccepted,
            isPending = isPending
        )
    }

    private fun setupTabs() {
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
    }

    private fun showApplied() {
        tabApplied.setBackgroundResource(R.drawable.bg_tab_selected_teal)
        tabApplied.setTextColor(resources.getColor(R.color.white, null))
        tabAccepted.setBackgroundResource(R.drawable.bg_tab_unselected_sorting)
        tabAccepted.setTextColor(resources.getColor(android.R.color.darker_gray, null))

        rvWorkers.adapter = WorkerSortingAdapter(
            workers = appliedWorkers,
            showCancelButton = false,
            onWorkerClick = { worker -> showWorkerDialog(worker) },
            onCancelClick = {}
        )
    }

    private fun showAccepted() {
        rvWorkers.adapter = WorkerSortingAdapter(
            workers = acceptedWorkers,
            showCancelButton = true,
            onWorkerClick = { worker -> showWorkerDialog(worker) },
            onCancelClick = { worker -> cancelWorker(worker) }
        )
    }

    private fun showWorkerDialog(worker: WorkerItem) {
        val application = allApplications.find { it.id == worker.applicationId } ?: return

        UserRepository.getJobById(
            jobId = jobId,
            onSuccess = { job ->
                // Fetch the worker's live profile for accurate rating, role and image
                UserRepository.getUserProfileById(
                    userId = worker.workerId,
                    onSuccess = { profile ->
                        val enrichedWorker = worker.copy(
                            name = profile.name.ifEmpty { worker.name },
                            role = profile.jobCategories.firstOrNull() ?: worker.bio,
                            rating = profile.rating.toFloat(),
                            profileImageUrl = profile.profileImageUrl.ifEmpty { worker.profileImageUrl }
                        )
                        openWorkerDialog(enrichedWorker, application, job)
                    },
                    onFailure = {
                        // Fallback: show dialog with the data we already have
                        openWorkerDialog(worker.copy(role = worker.bio), application, job)
                    }
                )
            },
            onFailure = { }
        )
    }

    private fun openWorkerDialog(
        worker: WorkerItem,
        application: Application,
        job: com.example.clickjob_finalproject.data.model.JobPost
    ) {
        WorkerDetailsDialog.newInstance(
            worker = worker,
            onApprove = {
                UserRepository.approveApplicant(
                    application = application,
                    job = job,
                    onSuccess = {
                        Toast.makeText(requireContext(), "המועמד אושר!", Toast.LENGTH_SHORT).show()
                        loadApplications()
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), "שגיאה באישור", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onMoreDetails = {
                val bundle = Bundle().apply {
                    putString("applicationId", application.id)
                    putString("workerId", worker.workerId)
                    putString("jobId", jobId)
                }
                findNavController().navigate(
                    R.id.action_workerSortingFragment_to_workerProfileFragment,
                    bundle
                )
            }
        ).show(parentFragmentManager, "worker_details")
    }

    private fun cancelWorker(worker: WorkerItem) {
        val application = allApplications.find { it.id == worker.applicationId } ?: return

        UserRepository.getJobById(
            jobId = jobId,
            onSuccess = { job ->
                UserRepository.cancelWorkerByEmployer(
                    application = application,
                    job = job,
                    onSuccess = {
                        Toast.makeText(requireContext(), "המועמד בוטל", Toast.LENGTH_SHORT).show()
                        loadApplications()
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), "שגיאה בביטול", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onFailure = { }
        )
    }

    override fun onResume() {
        super.onResume()
        bottomNav = requireActivity().findViewById(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        bottomNav?.visibility = View.VISIBLE
    }
}