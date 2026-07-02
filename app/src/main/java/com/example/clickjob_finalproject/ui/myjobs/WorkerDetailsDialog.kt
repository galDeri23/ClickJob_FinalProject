package com.example.clickjob_finalproject.ui.myjobs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.WorkerItem
import com.google.android.material.button.MaterialButton

class WorkerDetailsDialog : DialogFragment() {

    private var onApprove: (() -> Unit)? = null
    private var onMoreDetails: (() -> Unit)? = null

    companion object {
        fun newInstance(
            worker: WorkerItem,
            onApprove: () -> Unit,
            onMoreDetails: () -> Unit
        ): WorkerDetailsDialog {
            return WorkerDetailsDialog().apply {
                this.onApprove = onApprove
                this.onMoreDetails = onMoreDetails
                arguments = Bundle().apply {
                    putString("name", worker.name)
                    putString("role", worker.role)
                    putFloat("rating", worker.rating)
                    putString("profileImageUrl", worker.profileImageUrl)
                    putString("bio", worker.bio)
                    putBoolean("isPending", worker.isPending)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_worker_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("name") ?: ""
        val role = arguments?.getString("role") ?: ""
        val rating = arguments?.getFloat("rating") ?: 0f
        val profileImageUrl = arguments?.getString("profileImageUrl") ?: ""
        val isPending = arguments?.getBoolean("isPending") ?: false

        view.findViewById<TextView>(R.id.tvWorkerName).text = name
        view.findViewById<TextView>(R.id.tvWorkerRole).text = role
        view.findViewById<TextView>(R.id.tvRating).text = String.format("%.1f", rating)

        val imgProfile = view.findViewById<ImageView>(R.id.imgWorkerProfile)
        if (profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.bari)
                .error(R.drawable.bari)
                .into(imgProfile)
        }

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        // Approve button: disabled when already approved and waiting for the worker
        val btnApprove = view.findViewById<MaterialButton>(R.id.btnApprove)
        if (isPending) {
            btnApprove.isEnabled = false
            btnApprove.text = "ממתין לאישור העובד"
        }
        btnApprove.setOnClickListener {
            onApprove?.invoke()
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnMoreDetails).setOnClickListener {
            dismiss()
            onMoreDetails?.invoke()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes?.blurBehindRadius = 60
                setDimAmount(0.2f)
            } else {
                setDimAmount(0.5f)
            }
        }
    }
}