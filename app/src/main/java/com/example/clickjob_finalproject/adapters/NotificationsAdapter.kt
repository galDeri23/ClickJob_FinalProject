package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R

enum class NotificationStatus {
    ALERT,
    CONFIRMED,
    PENDING,
    CANCELLED,
    PEOPLE,
    RATING
}

data class NotificationItem(
    val id: String = "",
    val title: String,
    val dateTime: String,
    val timeAgo: String,
    val status: NotificationStatus,
    val jobId: String = "",
    val applicationId: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val workerImageUrl: String = "",
    val actionRequired: Boolean = false,
    val isRated: Boolean = false
)

class NotificationsAdapter(
    private var items: List<NotificationItem>,
    private val isEmployerMode: Boolean = false,
    private val onApprove: (NotificationItem) -> Unit = {},
    private val onCancel: (NotificationItem) -> Unit = {},
    private val onRate: (NotificationItem) -> Unit = {},
    private val onJobPage: (NotificationItem) -> Unit = {},
    private val onItemClick: (NotificationItem) -> Unit = {}
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot =
            itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRoot)

        val imgStatus = itemView.findViewById<ImageView>(R.id.imgStatus)
        val tvTitle = itemView.findViewById<TextView>(R.id.tvNotificationTitle)
        val tvDateTime = itemView.findViewById<TextView>(R.id.tvDateTime)
        val tvTimeAgo = itemView.findViewById<TextView>(R.id.tvTimeAgo)

        val actionsRow = itemView.findViewById<LinearLayout>(R.id.actionsRow)
        val btnApprove = itemView.findViewById<TextView>(R.id.btnApprove)
        val btnCancel = itemView.findViewById<TextView>(R.id.btnCancel)
        val btnSingle = itemView.findViewById<TextView>(R.id.btnSingleAction)
        val btnJobPage = itemView.findViewById<TextView>(R.id.btnJobPage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)

        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tvTitle.text = item.title
        holder.tvDateTime.text = item.dateTime
        holder.tvTimeAgo.text = item.timeAgo

        // Reset all dynamic views
        holder.actionsRow.visibility = View.GONE
        holder.btnSingle.visibility = View.GONE
        holder.btnJobPage.visibility = View.GONE

        holder.cardRoot.setCardBackgroundColor(
            ContextCompat.getColor(context, R.color.white)
        )

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        when (item.status) {

            NotificationStatus.ALERT -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_alert)

                if (isEmployerMode) {
                    holder.cardRoot.setCardBackgroundColor("#E8F5F6".toColorInt())

                    holder.btnJobPage.visibility = View.VISIBLE
                    holder.btnJobPage.text = "דף המשרה"
                    holder.btnJobPage.setOnClickListener {
                        onJobPage(item)
                    }
                }
            }

            NotificationStatus.CONFIRMED -> {
                holder.imgStatus.setImageResource(R.drawable.ic_status_check)

                // Green icon WITH buttons only when the notification requires action
                // (double-check). Informational "accepted" notifications have no buttons.
                if (!isEmployerMode && item.actionRequired) {
                    holder.actionsRow.visibility = View.VISIBLE
                    holder.cardRoot.setCardBackgroundColor("#FDF5F9".toColorInt())

                    holder.btnApprove.setOnClickListener {
                        onApprove(item)
                    }

                    holder.btnCancel.setOnClickListener {
                        onCancel(item)
                    }
                }
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

                if (!item.isRated) {
                    holder.btnSingle.visibility = View.VISIBLE
                    holder.btnSingle.text = "דירוג"

                    if (isEmployerMode) {
                        holder.btnSingle.setTextColor(ContextCompat.getColor(context, R.color.employer_primary))
                        holder.btnSingle.background =
                            ContextCompat.getDrawable(context, R.drawable.bg_outline_button_teal)
                    } else {
                        holder.btnSingle.setTextColor(
                            ContextCompat.getColor(context, R.color.brand_pink)
                        )
                        holder.btnSingle.background =
                            ContextCompat.getDrawable(context, R.drawable.bg_outline_button_pink)
                    }

                    holder.btnSingle.setOnClickListener {
                        onRate(item)
                    }
                }
            }
        }
    }

    fun updateItems(newItems: List<NotificationItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}