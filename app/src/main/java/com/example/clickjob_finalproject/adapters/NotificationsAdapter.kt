package com.example.clickjob_finalproject.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R
import androidx.core.graphics.toColorInt

// 6 notification types based on Figma icons
enum class NotificationStatus {
    ALERT,       // ❗ red/pink - requires attention/approval
    CONFIRMED,   // ✓ green - approved/completed
    PENDING,     // 🕐 clock - waiting/reminder
    CANCELLED,   // ✗ red - cancelled
    PEOPLE,      // 👥 blue - people/acceptance related
    RATING       // ⭐ yellow - rating request
}

data class NotificationItem(
    val id: String = "",
    val title: String,
    val dateTime: String,
    val timeAgo: String,
    val status: NotificationStatus,
    val jobId: String = "",
    val applicationId: String = "",
    val isRated: Boolean = false
)

class NotificationsAdapter(
    private var items: List<NotificationItem>,
    private val onApprove: (NotificationItem) -> Unit = {},
    private val onCancel: (NotificationItem) -> Unit = {},
    private val onRate: (NotificationItem) -> Unit = {},
    private val onItemClick: (NotificationItem) -> Unit = {}
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot   = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRoot)
        val imgStatus  = itemView.findViewById<ImageView>(R.id.imgStatus)
        val tvTitle    = itemView.findViewById<TextView>(R.id.tvNotificationTitle)
        val tvDateTime = itemView.findViewById<TextView>(R.id.tvDateTime)
        val tvTimeAgo  = itemView.findViewById<TextView>(R.id.tvTimeAgo)
        val actionsRow = itemView.findViewById<LinearLayout>(R.id.actionsRow)
        val btnApprove = itemView.findViewById<TextView>(R.id.btnApprove)
        val btnCancel  = itemView.findViewById<TextView>(R.id.btnCancel)
        val btnSingle  = itemView.findViewById<TextView>(R.id.btnSingleAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text    = item.title
        holder.tvDateTime.text = item.dateTime
        holder.tvTimeAgo.text  = item.timeAgo

        holder.actionsRow.visibility = View.GONE
        holder.btnSingle.visibility  = View.GONE
        holder.cardRoot.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, R.color.white)
        )

        // Card click - navigate to job details
        holder.itemView.setOnClickListener { onItemClick(item) }

        when (item.status) {
            NotificationStatus.ALERT -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_alert)
                holder.actionsRow.visibility = View.VISIBLE
                holder.cardRoot.setCardBackgroundColor("#FDF5F9".toColorInt())
                holder.btnApprove.setOnClickListener { onApprove(item) }
                holder.btnCancel.setOnClickListener { onCancel(item) }
            }

            NotificationStatus.CONFIRMED -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_check)
            }

            NotificationStatus.PENDING -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_clock)
            }

            NotificationStatus.CANCELLED -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_x)
            }

            NotificationStatus.PEOPLE -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_people)
            }

            NotificationStatus.RATING -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_star)
                // Hide rating button if already rated
                if (!item.isRated) {
                    holder.btnSingle.visibility = View.VISIBLE
                    holder.btnSingle.text = "דירוג"
                    holder.btnSingle.setOnClickListener { onRate(item) }
                }
            }
        }
    }

    fun updateItems(newItems: List<NotificationItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}