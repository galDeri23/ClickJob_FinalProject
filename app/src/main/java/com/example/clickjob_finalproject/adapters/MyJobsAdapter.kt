package com.example.clickjob_finalproject.adapters

import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R

enum class JobTabType { ACTIVE, PENDING, HISTORY }

// Timer type for worker active tab
enum class TimerType { NONE, SOON, PENDING }

data class MyJobItem(
    val title: String,
    val company: String,
    val price: String,
    val distance: String,
    val day: String,
    val category: String,
    val needsApproval: Boolean = false,
    val timerType: TimerType = TimerType.NONE,
    val countdownMillis: Long = 0L
)

class MyJobsAdapter(
    private var items: List<MyJobItem>,
    private var tabType: JobTabType = JobTabType.ACTIVE,
    private val onApproveClick: (MyJobItem) -> Unit = {}
) : RecyclerView.Adapter<MyJobsAdapter.MyJobViewHolder>() {

    inner class MyJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot      = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRoot)
        val imgCategory   = itemView.findViewById<ImageView>(R.id.imgCategory)
        val tvJobTitle    = itemView.findViewById<TextView>(R.id.tvJobTitle)
        val tvCompany     = itemView.findViewById<TextView>(R.id.tvCompanyName)
        val tvDistance    = itemView.findViewById<TextView>(R.id.tvDistance)
        val tvDay         = itemView.findViewById<TextView>(R.id.tvDay)
        val tvPrice       = itemView.findViewById<TextView>(R.id.tvPrice)
        val btnApprove    = itemView.findViewById<TextView>(R.id.btnApprove)
        val tvTimerSoon   = itemView.findViewById<TextView>(R.id.tvTimerSoon)
        val tvTimerPending = itemView.findViewById<TextView>(R.id.tvTimerPending)
        var countDownTimer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyJobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_job_card, parent, false)
        return MyJobViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyJobViewHolder, position: Int) {
        val item = items[position]

        holder.tvJobTitle.text = item.title
        holder.tvCompany.text  = item.company
        holder.tvDistance.text = item.distance
        holder.tvDay.text      = item.day
        holder.tvPrice.text    = item.price
        holder.imgCategory.setImageResource(getCategoryImage(item.category))

        // Cancel any running timer before rebinding
        holder.countDownTimer?.cancel()

        // Hide all badges by default
        holder.btnApprove.visibility     = View.GONE
        holder.tvTimerSoon.visibility    = View.GONE
        holder.tvTimerPending.visibility = View.GONE

        when (tabType) {
            JobTabType.ACTIVE -> {
                holder.cardRoot.alpha = 1f
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)

                when {
                    // Show approve button
                    item.needsApproval -> {
                        holder.btnApprove.visibility = View.VISIBLE
                        holder.btnApprove.setOnClickListener { onApproveClick(item) }
                    }
                    // Show "בעוד יום" gray badge
                    item.timerType == TimerType.SOON -> {
                        holder.tvTimerSoon.visibility = View.VISIBLE
                    }
                    // Show "בהמתנה" pink badge
                    item.timerType == TimerType.PENDING -> {
                        holder.tvTimerPending.visibility = View.VISIBLE
                    }
                }
            }

            JobTabType.HISTORY -> {
                // Dimmed appearance for history
                holder.cardRoot.alpha = 0.5f
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)
            }

            else -> {
                holder.cardRoot.alpha = 1f
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)
            }
        }
    }

    fun updateItems(newItems: List<MyJobItem>, newTabType: JobTabType) {
        items = newItems
        tabType = newTabType
        notifyDataSetChanged()
    }

    private fun getCategoryImage(category: String): Int {
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
            else                 -> R.drawable.img_cat_circle_service
        }
    }

    override fun getItemCount() = items.size
}