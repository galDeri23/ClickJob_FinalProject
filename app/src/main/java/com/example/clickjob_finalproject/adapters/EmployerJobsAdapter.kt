package com.example.clickjob_finalproject.adapters

import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R

data class EmployerJobItem(
    val id: String = "",
    val title: String,
    val company: String,
    val workersRegistered: Int,
    val workersNeeded: Int,
    val date: String = "תאריך",
    val price: String = "88.88",
    val category: String = "מסעדות",
    val countdownMillis: Long = 0L
)

class EmployerJobsAdapter(
    private var items: List<EmployerJobItem>,
    private var tabType: JobTabType = JobTabType.ACTIVE,
    private val onQrClick: (EmployerJobItem) -> Unit = {},
    private val onDuplicateClick: (EmployerJobItem) -> Unit = {},
    private val onItemClick: (EmployerJobItem) -> Unit = {}
) : RecyclerView.Adapter<EmployerJobsAdapter.EmployerViewHolder>() {

    inner class EmployerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot        = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRoot)
        val imgCategory     = itemView.findViewById<ImageView>(R.id.imgCategory)
        val tvJobTitle      = itemView.findViewById<TextView>(R.id.tvJobTitle)
        val tvCompanyName   = itemView.findViewById<TextView>(R.id.tvCompanyName)
        val tvPrice         = itemView.findViewById<TextView>(R.id.tvPrice)
        val tvDate          = itemView.findViewById<TextView>(R.id.tvDate)
        val tvWorkersCount  = itemView.findViewById<TextView>(R.id.tvWorkersCount)
        val progressWorkers = itemView.findViewById<ProgressBar>(R.id.progressWorkers)
        val tvTimer         = itemView.findViewById<TextView>(R.id.tvTimer)
        val btnQr           = itemView.findViewById<LinearLayout>(R.id.btnQr)
        val btnDuplicate    = itemView.findViewById<LinearLayout>(R.id.btnDuplicate)
        var countDownTimer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employer_job_card, parent, false)
        return EmployerViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployerViewHolder, position: Int) {
        val item = items[position]

        holder.tvJobTitle.text     = item.title
        holder.tvCompanyName.text  = item.company
        holder.tvPrice.text        = "₪${item.price}/לשעה"
        holder.tvDate.text         = item.date
        holder.tvWorkersCount.text = "${item.workersRegistered}/${item.workersNeeded}"
        holder.imgCategory.setImageResource(getCategoryImage(item.category))

        val progress = if (item.workersNeeded > 0)
            (item.workersRegistered * 100) / item.workersNeeded else 0
        holder.progressWorkers.progress = progress

        // Cancel any running timer
        holder.countDownTimer?.cancel()

        // Hide all badges by default
        holder.btnQr.visibility        = View.GONE
        holder.tvTimer.visibility      = View.GONE
        holder.btnDuplicate.visibility = View.GONE
        holder.itemView.setOnClickListener(null)

        when (tabType) {
            JobTabType.ACTIVE -> {
                holder.cardRoot.alpha = 1f
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)
                holder.progressWorkers.progressDrawable =
                    androidx.core.content.ContextCompat.getDrawable(
                        holder.itemView.context, R.drawable.progress_bar_teal)

                if (item.workersRegistered == item.workersNeeded) {
                    // Filled - show QR button
                    holder.btnQr.visibility = View.VISIBLE
                    holder.tvTimer.visibility = View.GONE
                    holder.btnQr.setOnClickListener { onQrClick(item) }
                } else {
                    // Open - show countdown timer
                    holder.btnQr.visibility = View.GONE
                    if (item.countdownMillis > 0) {
                        holder.tvTimer.visibility = View.VISIBLE
                        startTimer(holder, item.countdownMillis)
                    }
                    holder.itemView.setOnClickListener { onItemClick(item) }
                }
            }

            JobTabType.PENDING -> {
                holder.cardRoot.alpha = 1f
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)
                holder.progressWorkers.progressDrawable =
                    androidx.core.content.ContextCompat.getDrawable(
                        holder.itemView.context, R.drawable.progress_bar_teal)
                if (item.countdownMillis > 0) {
                    holder.tvTimer.visibility = View.VISIBLE
                    startTimer(holder, item.countdownMillis)
                }
                holder.itemView.setOnClickListener { onItemClick(item) }
            }

            JobTabType.HISTORY -> {
                // Dimmed appearance for history
                holder.cardRoot.alpha = 1f
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)
                holder.btnDuplicate.visibility = View.VISIBLE
                holder.progressWorkers.progressDrawable =
                    androidx.core.content.ContextCompat.getDrawable(
                        holder.itemView.context, R.drawable.progress_bar_gray)
                holder.btnDuplicate.setOnClickListener { onDuplicateClick(item) }
            }
        }
    }

    private fun startTimer(holder: EmployerViewHolder, millis: Long) {
        holder.countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / 3600000
                val minutes = (millisUntilFinished % 3600000) / 60000
                val seconds = (millisUntilFinished % 60000) / 1000

                if (hours >= 1) {
                    // More than 1 hour left - show "X שעות" in gray
                    val hoursDisplay = if (minutes >= 30) hours + 0.5 else hours.toDouble()
                    holder.tvTimer.text = "${hoursDisplay.let {
                        if (it == it.toLong().toDouble()) it.toLong().toString()
                        else it.toString()
                    }} שעות"
                    holder.tvTimer.setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                            holder.itemView.context, R.color.text_dark))
                    holder.tvTimer.setBackgroundResource(R.drawable.bg_badge_gray)
                    holder.tvTimer.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_clock, 0)
                } else {
                    // Less than 1 hour - show "MM:SS" in red with warning icon
                    holder.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                    holder.tvTimer.setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                            holder.itemView.context, android.R.color.holo_red_light))
                    holder.tvTimer.setBackgroundResource(R.drawable.bg_badge_pink)
                    holder.tvTimer.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_warning, 0)
                }
            }

            override fun onFinish() {
                holder.tvTimer.text = "00:00"
                holder.tvTimer.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        holder.itemView.context, android.R.color.holo_red_light))
                holder.tvTimer.setBackgroundResource(R.drawable.bg_badge_pink)
            }
        }.start()
    }

    companion object {
        fun getCategoryImage(category: String): Int {
            return when (category) {
                "אבטחה וביטחון"      -> R.drawable.img_cat_circle_security
                "משלוחים ותחבורה"    -> R.drawable.img_cat_circle_delivery
                "בניין וייצור"        -> R.drawable.img_cat_circle_construction
                "חינוך והוראה"        -> R.drawable.img_cat_circle_education
                "בעלי חיים"          -> R.drawable.img_cat_circle_pets
                "אפסנאות ולוגיסטיקה" -> R.drawable.img_cat_circle_logistics
                "מסעדות"             -> R.drawable.img_cat_circle_hospitality
                "אחזקה"              -> R.drawable.img_cat_circle_maintenance
                "רפואה ובריאות"      -> R.drawable.img_cat_circle_health
                "הפקה ואירועים"      -> R.drawable.img_cat_circle_events
                "טכנולוגיה"          -> R.drawable.img_cat_circle_tech
                "שירות לקוחות"       -> R.drawable.img_cat_circle_service
                "מכירות ואופנה"      -> R.drawable.img_cat_circle_sales
                "עיצוב וקריאייטיב"   -> R.drawable.img_cat_circle_creative
                else                 -> R.drawable.img_cat_circle_hospitality
            }
        }
    }

    fun updateItems(newItems: List<EmployerJobItem>, newTabType: JobTabType) {
        items = newItems
        tabType = newTabType
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}