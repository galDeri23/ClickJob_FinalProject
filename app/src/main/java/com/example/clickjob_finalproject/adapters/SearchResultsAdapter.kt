package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R

data class ResultItem(
    val id: String = "",
    val title: String,
    val company: String = "",
    val price: String,
    val salary: Int = 0,
    val day: String,
    val distance: String,
    val category: String = "מסעדות",
    val isUrgent: Boolean = false,
    val date: Long = 0L
)
class SearchResultsAdapter(
    private var items: List<ResultItem>,
    private val onItemClick: (ResultItem) -> Unit = {}
) : RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCategory: ImageView = itemView.findViewById(R.id.tvCategory)
        val tvPrice: TextView      = itemView.findViewById(R.id.tvPrice)
        val tvPerHour: TextView    = itemView.findViewById(R.id.tvPerHour)
        val tvJobTitle: TextView   = itemView.findViewById(R.id.tvJobTitle)
        val tvDay: TextView        = itemView.findViewById(R.id.tvDay)
        val tvCompanyName : TextView        = itemView.findViewById(R.id.tvCompanyName)
        val tvDistance: TextView   = itemView.findViewById(R.id.tvDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result_card, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = items[position]
        holder.tvPrice.text    = item.price
        holder.tvPerHour.text  = "לשעה"
        holder.tvJobTitle.text = item.title
        holder.tvCompanyName.text = item.company
        holder.tvDay.text      = item.day
        holder.tvDistance.text = item.distance
        holder.imgCategory.setImageResource(getCategoryCircleImage(item.category))
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    private fun getCategoryCircleImage(category: String): Int {
        return when (category) {
            "אבטחה וביטחון"      -> R.drawable.img_cat_circle_security
            "משלוחים ותחבורה"    -> R.drawable.img_cat_circle_delivery
            "בניין וייצור"        -> R.drawable.img_cat_circle_construction
            "חינוך והוראה"        -> R.drawable.img_cat_circle_education
            "בעלי חיים"          -> R.drawable.img_cat_circle_pets
            "אפסנאות ולוגיסטיקה" -> R.drawable.img_cat_circle_logistics
            "מסעדנות"             -> R.drawable.img_cat_circle_hospitality
            "אחזקה"              -> R.drawable.img_cat_circle_maintenance
            "רפואה ובריאות"      -> R.drawable.img_cat_circle_health
            "הפקה ואירועים"      -> R.drawable.img_cat_circle_events
            "טכנולוגיה ותוכנה"          -> R.drawable.img_cat_circle_tech
            "שירות לקוחות"       -> R.drawable.img_cat_circle_service
            "מכירות ואופנה"      -> R.drawable.img_cat_circle_sales
            "עיצוב וקריאייטיב"   -> R.drawable.img_cat_circle_creative
            else                 -> R.drawable.img_cat_circle_service
        }
    }

    fun updateItems(newItems: List<ResultItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}