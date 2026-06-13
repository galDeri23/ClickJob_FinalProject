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

data class MyJobItem(
    val title: String,
    val company: String,
    val price: String,
    val distance: String,
    val day: String,
    val category: String,
    val needsApproval: Boolean = false,
    // Pending tab: countdown in milliseconds
    val countdownMillis: Long = 0L
)

class MyJobsAdapter(
    private var items: List<MyJobItem>,
    private var tabType: JobTabType = JobTabType.ACTIVE,
    private val onApproveClick: (MyJobItem) -> Unit = {}
) : RecyclerView.Adapter<MyJobsAdapter.MyJobViewHolder>() {

    inner class MyJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot    = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRoot)
        val imgCategory = itemView.findViewById<ImageView>(R.id.imgCategory)
        val tvJobTitle  = itemView.findViewById<TextView>(R.id.tvJobTitle)
        val tvCompany   = itemView.findViewById<TextView>(R.id.tvCompanyName)
        val tvDistance  = itemView.findViewById<TextView>(R.id.tvDistance)
        val tvDay       = itemView.findViewById<TextView>(R.id.tvDay)
        val tvPrice     = itemView.findViewById<TextView>(R.id.tvPrice)
        val btnApprove  = itemView.findViewById<TextView>(R.id.btnApprove)
        val tvTimer     = itemView.findViewById<TextView>(R.id.tvTimer)
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

        when (tabType) {
            JobTabType.ACTIVE -> {
                // White card background
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)

                // Show approve button only for items that need it
                if (item.needsApproval) {
                    holder.btnApprove.visibility = View.VISIBLE
                    holder.btnApprove.setOnClickListener { onApproveClick(item) }
                } else {
                    holder.btnApprove.visibility = View.GONE
                }
                holder.tvTimer.visibility = View.GONE
            }

            JobTabType.PENDING -> {
                // White card background
                holder.cardRoot.setCardBackgroundColor(Color.WHITE)
                holder.btnApprove.visibility = View.GONE

                // Show countdown timer
                if (item.countdownMillis > 0) {
                    holder.tvTimer.visibility = View.VISIBLE
                    startTimer(holder, item.countdownMillis)
                } else {
                    holder.tvTimer.visibility = View.GONE
                }
            }

            JobTabType.HISTORY -> {
                // Beige/cream card background for history
                holder.cardRoot.setCardBackgroundColor(Color.parseColor("#FFF8F0"))
                holder.btnApprove.visibility = View.GONE
                holder.tvTimer.visibility = View.GONE
            }
        }
    }

    // Countdown timer displayed as HH:MM:SS:mm
    private fun startTimer(holder: MyJobViewHolder, millis: Long) {
        holder.countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours   = millisUntilFinished / 3600000
                val minutes = (millisUntilFinished % 3600000) / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                holder.tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
            override fun onFinish() {
                holder.tvTimer.text = "00:00:00"
            }
        }.start()
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