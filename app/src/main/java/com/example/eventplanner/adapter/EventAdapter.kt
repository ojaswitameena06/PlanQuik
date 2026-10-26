package com.example.eventplanner.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.R
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.util.EventUi

class EventAdapter(
    private var events: List<Event>,
    private val onItemClick: (Event) -> Unit,
    private val onItemLongClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateBadge: View = itemView.findViewById(R.id.dateBadge)
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
        val tvTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvEventDateTime)
        val tvLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        val tvCategory: TextView = itemView.findViewById(R.id.tvEventCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val context = holder.itemView.context
        val color = EventUi.categoryColor(context, event.category)
        val soft = ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 36))
        val millis = EventUi.eventMillis(event)

        holder.tvDay.text = millis?.let { EventUi.format(it, "d") } ?: "-"
        holder.tvMonth.text = millis?.let { EventUi.format(it, "MMM") } ?: ""
        holder.tvTitle.text = event.title
        holder.tvDateTime.text = millis?.let { EventUi.format(it, "EEE, h:mm a") }
            ?: "${event.date} • ${event.time}"
        holder.tvLocation.text = event.location
        holder.tvCategory.text = event.category

        holder.dateBadge.backgroundTintList = soft
        holder.tvDay.setTextColor(color)
        holder.tvMonth.setTextColor(color)
        holder.tvCategory.backgroundTintList = soft
        holder.tvCategory.setTextColor(color)

        // Past events are dimmed
        holder.itemView.alpha =
            if (millis != null && millis < System.currentTimeMillis()) 0.6f else 1f

        holder.itemView.setOnClickListener { onItemClick(event) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(event)
            true
        }
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}