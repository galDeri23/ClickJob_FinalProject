package com.example.clickjob_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clickjob_finalproject.R
import android.widget.ImageView

data class ResultItem(
    val title: String,
    val company: String,
    val price: String,
    val rating: String,
    val distance: String,
    val day: String,
    val category: String = "מסעדנות"
)

class SearchResultsAdapter(
    private var items: List<ResultItem>
) : RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobTitle: TextView   = itemView.findViewById(R.id.tvJobTitle)
        val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        val tvPrice: TextView      = itemView.findViewById(R.id.tvPrice)
        val tvRating: TextView     = itemView.findViewById(R.id.tvRating)
        val tvDistance: TextView   = itemView.findViewById(R.id.tvDistance)
        val tvDay: TextView        = itemView.findViewById(R.id.tvDay)

        val imgCompany: ImageView = itemView.findViewById(R.id.tvCategory)    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result_card, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = items[position]
        holder.tvJobTitle.text    = item.title
        holder.tvCompanyName.text = item.company
        holder.tvPrice.text       = item.price
        holder.tvRating.text      = item.rating
        holder.tvDistance.text    = item.distance
        holder.tvDay.text         = item.day
        holder.imgCompany.setImageResource(getCategoryCircleImage(item.category))
    }

    private fun getCategoryCircleImage(category: String): Int {
        return when (category) {
            "אבטחה וביטחון" -> R.drawable.img_cat_circle_security
            "משלוחים ותחבורה" -> R.drawable.img_cat_circle_delivery
            "בניין, תעשייה וייצור" -> R.drawable.img_cat_circle_construction
            "חינוך והוראה" -> R.drawable.img_cat_circle_education
            "בעלי חיים" -> R.drawable.img_cat_circle_pets
            "אפסנאות ולוגיסטיקה" -> R.drawable.img_cat_circle_logistics
            "מסעדנות" -> R.drawable.img_cat_circle_hospitality
            "אחזקה" -> R.drawable.img_cat_circle_maintenance
            "בריאות ורווחה" -> R.drawable.img_cat_circle_health
            "הפקה ואירועים" -> R.drawable.img_cat_circle_events
            "טכנולוגיה ותוכנה" -> R.drawable.img_cat_circle_tech
            "שירות לקוחות ותמיכה" -> R.drawable.img_cat_circle_service
            "מכירות ואופנה" -> R.drawable.img_cat_circle_sales
            "קריאייטיב, עיצוב ומדיה" -> R.drawable.img_cat_circle_creative
            else -> R.drawable.img_cat_circle_service
        }
    }
    // Update list when tab changes
    fun updateItems(newItems: List<ResultItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}