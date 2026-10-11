package com.example.eventplanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.R
import com.example.eventplanner.data.entity.Event

class EventAdapter(
    private var events: List<Event>,
    private val onItemClick: (Event) -> Unit,
    private val onItemLongClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        holder.tvTitle.text = event.title
        holder.tvDateTime.text = "${event.date} • ${event.time}"
        holder.tvLocation.text = event.location
        holder.tvCategory.text = event.category

        holder.itemView.setOnClickListener {
            onItemClick(event)
        }

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