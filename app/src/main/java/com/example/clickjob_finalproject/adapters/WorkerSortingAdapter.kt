package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clickjob_finalproject.R
import com.google.android.material.imageview.ShapeableImageView

data class WorkerItem(
    val applicationId: String = "",
    val workerId: String = "",
    val name: String,
    val role: String,
    val phone: String,
    val email: String,
    val bio: String = "",
    val profileImageUrl: String = "",
    val rating: Float = 0f,
    val isAccepted: Boolean = false,
    val isPending: Boolean = false // Employer approved, waiting for worker confirmation
)

class WorkerSortingAdapter(
    private val workers: List<WorkerItem>,
    private val showCancelButton: Boolean,
    private val onWorkerClick: (WorkerItem) -> Unit,
    private val onCancelClick: (WorkerItem) -> Unit
) : RecyclerView.Adapter<WorkerSortingAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgWorker: ShapeableImageView = view.findViewById(R.id.imgWorker)
        val tvName: TextView    = view.findViewById(R.id.tvWorkerName)
        val tvRole: TextView    = view.findViewById(R.id.tvWorkerRole)
        val tvPhone: TextView   = view.findViewById(R.id.tvPhone)
        val btnCancel: TextView = view.findViewById(R.id.btnCancel)
        val tvTimerPending: TextView = view.findViewById(R.id.tvTimerPending)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val worker = workers[position]
        holder.tvName.text  = worker.name
        holder.tvRole.text  = worker.bio.ifEmpty { worker.role }
        holder.tvPhone.text = worker.phone

        // Load profile image with Glide
        if (worker.profileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(worker.profileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.user)
                .into(holder.imgWorker)
        } else {
            holder.imgWorker.setImageResource(R.drawable.user)
        }

        // "Pending" badge: employer approved, waiting for worker's confirmation
        holder.tvTimerPending.visibility = if (worker.isPending) View.VISIBLE else View.GONE

        // Cancel button hidden when the pending badge occupies the same spot
        holder.btnCancel.visibility =
            if (showCancelButton && !worker.isPending) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onWorkerClick(worker) }
        holder.btnCancel.setOnClickListener { onCancelClick(worker) }
    }

    override fun getItemCount() = workers.size
}