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
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.EmployerJobsAdapter
import com.example.clickjob_finalproject.adapters.WorkerItem
import com.example.clickjob_finalproject.adapters.WorkerSortingAdapter
import com.example.clickjob_finalproject.data.model.Application
import com.example.clickjob_finalproject.data.model.JobPost
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.example.clickjob_finalproject.databinding.FragmentWorkerSortingBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ListenerRegistration

class WorkerSortingFragment : Fragment() {

    private var _binding: FragmentWorkerSortingBinding? = null
    private val binding get() = _binding!!

    private var applicationsListener: ListenerRegistration? = null

    private var jobId: String = ""
    private var jobTitle: String = ""
    private var jobCompany: String = ""
    private var jobCategory: String = "מסעדנות"
    private var jobDate: String = ""
    private var jobPrice: String = ""
    private var workersNeeded: Int = 0

    private var appliedWorkers = listOf<WorkerItem>()
    private var acceptedWorkers = listOf<WorkerItem>()
    private var allApplications = listOf<Application>()
    private var bottomNav: View? = null
    private var isAcceptedTabSelected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerSortingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvWorkers.layoutManager = LinearLayoutManager(requireContext())

        jobId = arguments?.getString("jobId") ?: ""
        jobTitle = arguments?.getString("jobTitle") ?: ""
        jobCompany = arguments?.getString("jobCompany") ?: ""
        jobCategory = arguments?.getString("jobCategory") ?: "מסעדות"
        jobDate = arguments?.getString("jobDate") ?: ""
        jobPrice = arguments?.getString("jobPrice") ?: ""
        workersNeeded = arguments?.getInt("workersNeeded") ?: 0

        setupJobCard(view)
        setupTabs()
        setupBackButton()
        loadApplications()
    }

    private fun setupJobCard(view: View) {
        binding.tvNeededCount.text = "כמה אני צריך: $workersNeeded"

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

    private fun setupBackButton() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadApplications() {
        if (jobId.isEmpty()) return

        applicationsListener?.remove()

        applicationsListener = UserRepository.listenToJobApplications(
            jobId = jobId,
            onUpdate = { applications ->
                allApplications = applications

                appliedWorkers = applications
                    .filter { it.status == "pending" || it.status == "employer_approved" }
                    .map { it.toWorkerItem(isPending = it.status == "employer_approved") }

                acceptedWorkers = applications
                    .filter { it.status == "confirmed" || it.status == "arrived" }
                    .map { it.toWorkerItem(isAccepted = true) }

                binding.tabApplied.text = "הגישו (${appliedWorkers.size})"
                binding.tabAccepted.text = "התקבלו (${acceptedWorkers.size})"
                binding.tvAppliedCount.text = "כמה הגישו: ${applications.size}"

                if (isAcceptedTabSelected) {
                    showAccepted()
                } else {
                    showApplied()
                }
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
        binding.tabApplied.setOnClickListener {
            isAcceptedTabSelected = false
            showApplied()
        }

        binding.tabAccepted.setOnClickListener {
            isAcceptedTabSelected = true
            showAccepted()
        }
    }

    private fun showApplied() {
        binding.tabApplied.setBackgroundResource(R.drawable.bg_tab_selected_teal)
        binding.tabApplied.setTextColor(resources.getColor(R.color.white, null))

        binding.tabAccepted.setBackgroundResource(R.drawable.bg_tab_unselected_sorting)
        binding.tabAccepted.setTextColor(resources.getColor(android.R.color.darker_gray, null))

        binding.rvWorkers.adapter = WorkerSortingAdapter(
            workers = appliedWorkers,
            showCancelButton = false,
            onWorkerClick = { worker -> showWorkerDialog(worker) },
            onCancelClick = {}
        )
    }

    private fun showAccepted() {
        binding.tabAccepted.setBackgroundResource(R.drawable.bg_tab_selected_teal)
        binding.tabAccepted.setTextColor(resources.getColor(R.color.white, null))

        binding.tabApplied.setBackgroundResource(R.drawable.bg_tab_unselected_sorting)
        binding.tabApplied.setTextColor(resources.getColor(android.R.color.darker_gray, null))

        binding.rvWorkers.adapter = WorkerSortingAdapter(
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
        job: JobPost
    ) {
        WorkerDetailsDialog.newInstance(
            worker = worker,
            onApprove = {
                UserRepository.approveApplicant(
                    application = application,
                    job = job,
                    onSuccess = {
                        Toast.makeText(requireContext(), "המועמד אושר!", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        applicationsListener?.remove()
        applicationsListener = null
        _binding = null
        super.onDestroyView()
    }
}