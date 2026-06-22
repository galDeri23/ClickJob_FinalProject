package com.example.clickjob_finalproject.ui.myjobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.adapters.WorkerItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class WorkerDetailsDialog(private val worker: WorkerItem) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_worker_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvWorkerName).text = worker.name
        view.findViewById<TextView>(R.id.tvWorkerRole).text = worker.role
        view.findViewById<TextView>(R.id.tvRating).text = "${worker.rating}"

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnApprove).setOnClickListener {
            // TODO: approve worker
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnMoreDetails).setOnClickListener {
            // TODO: navigate to worker profile
            dismiss()
        }
    }
}