
package com.example.eventplanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.R
import com.example.eventplanner.data.entity.Event
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarEventAdapter(
    private var events: List<Event>
) : RecyclerView.Adapter<CalendarEventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val day: TextView = itemView.findViewById(R.id.tvDay)
        val month: TextView = itemView.findViewById(R.id.tvMonth)
        val title: TextView = itemView.findViewById(R.id.tvEventTitle)
        val dateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        val location: TextView = itemView.findViewById(R.id.tvEventLocation)
        val category: TextView = itemView.findViewById(R.id.tvEventCategory)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.title.text = event.title
        holder.dateTime.text = event.time
        holder.location.text = event.location
        holder.category.text = event.category

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            inputFormat.isLenient = false
            val date = inputFormat.parse(event.date)

            if (date != null) {
                holder.day.text = SimpleDateFormat("dd", Locale.getDefault())
                    .format(date)
                holder.month.text = SimpleDateFormat("MMM", Locale.getDefault())
                    .format(date)
            } else {
                holder.day.text = ""
                holder.month.text = ""
            }
        } catch (e: Exception) {
            holder.day.text = ""
            holder.month.text = ""
        }
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
