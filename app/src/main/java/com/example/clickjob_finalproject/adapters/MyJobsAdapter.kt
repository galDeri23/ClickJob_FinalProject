package com.example.clickjob_finalproject.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.databinding.ItemMyJobCardBinding

enum class JobTabType { ACTIVE, PENDING, HISTORY }
enum class TimerType { NONE, SOON, PENDING }

data class MyJobItem(
    val id: String = "",
    val jobId: String = "",
    val applicationId: String = "",
    val title: String,
    val company: String,
    val price: String,
    val distance: String,
    val day: String,
    val category: String,
    val needsApproval: Boolean = false,
    val timerType: TimerType = TimerType.NONE,
    val shiftStartMillis: Long = 0L,
    val shiftEndMillis: Long = 0L
)

class MyJobsAdapter(
    private var items: List<MyJobItem>,
    private var tabType: JobTabType = JobTabType.ACTIVE,
    private val onApproveClick: (MyJobItem) -> Unit = {},
    private val onItemClick: (MyJobItem) -> Unit = {}
) : RecyclerView.Adapter<MyJobsAdapter.MyJobViewHolder>() {

    inner class MyJobViewHolder(val binding: ItemMyJobCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyJobViewHolder {
        val binding = ItemMyJobCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyJobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyJobViewHolder, position: Int) {
        val binding = holder.binding
        val item = items[position]

        binding.tvJobTitle.text = item.title
        binding.tvCompanyName.text = item.company
        binding.tvDistance.text = item.distance
        binding.tvDay.text = item.day
        binding.tvPrice.text = item.price
        binding.imgCategory.setImageResource(getCategoryImage(item.category))

        // Hide all badges by default
        binding.btnApprove.visibility     = android.view.View.GONE
        binding.tvTimerSoon.visibility    = android.view.View.GONE
        binding.tvTimerPending.visibility = android.view.View.GONE
        binding.root.setOnClickListener(null)

        when (tabType) {
            JobTabType.ACTIVE -> {
                binding.cardRoot.alpha = 1f
                binding.cardRoot.setCardBackgroundColor(Color.WHITE)

                when {
                    item.needsApproval -> {
                        binding.btnApprove.visibility = android.view.View.VISIBLE
                        binding.btnApprove.setOnClickListener { onApproveClick(item) }
                        binding.root.setOnClickListener { onItemClick(item) }
                    }
                    item.timerType == TimerType.SOON -> {
                        binding.tvTimerSoon.visibility = android.view.View.VISIBLE
                        val now = System.currentTimeMillis()
                        val diff = item.shiftStartMillis - now
                        val hours = diff / 3600000
                        val days = hours / 24
                        binding.tvTimerSoon.text = when {
                            days >= 1 -> "בעוד $days ימים"
                            hours >= 1 -> "בעוד $hours שעות"
                            else -> "בעוד ${diff / 60000} דקות"
                        }
                    }
                    item.timerType == TimerType.PENDING -> {
                        binding.tvTimerPending.visibility = android.view.View.VISIBLE
                    }
                }
            }

            JobTabType.HISTORY -> {
                binding.cardRoot.alpha = 0.5f
                binding.cardRoot.setCardBackgroundColor(Color.WHITE)
            }

            else -> {
                binding.cardRoot.alpha = 1f
                binding.cardRoot.setCardBackgroundColor(Color.WHITE)
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