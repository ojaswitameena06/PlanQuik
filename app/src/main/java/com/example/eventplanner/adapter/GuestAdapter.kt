package com.example.eventplanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventplanner.R
import com.example.eventplanner.data.entity.Guest

class GuestAdapter(
    private var guests: List<Guest>,
    private val onRsvpClick: (Guest) -> Unit,
    private val onInviteClick: (Guest) -> Unit,
    private val onDeleteClick: (Guest) -> Unit
) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvGuestName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvGuestPhone)
        val tvRsvp: TextView = itemView.findViewById(R.id.tvGuestRsvp)
        val btnInvite: ImageButton = itemView.findViewById(R.id.btnInvite)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteGuest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guest, parent, false)
        return GuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val guest = guests[position]
        holder.tvName.text = guest.name
        holder.tvPhone.text = guest.phone
        holder.tvRsvp.text = guest.rsvpStatus

        val color = when (guest.rsvpStatus) {
            "Accepted" -> "#2E7D32"
            "Declined" -> "#C62828"
            else -> "#F9A825"
        }
        holder.tvRsvp.setTextColor(android.graphics.Color.parseColor(color))

        holder.tvRsvp.setOnClickListener { onRsvpClick(guest) }
        holder.btnInvite.setOnClickListener { onInviteClick(guest) }
        holder.btnDelete.setOnClickListener { onDeleteClick(guest) }
    }

    override fun getItemCount(): Int = guests.size

    fun updateGuests(newGuests: List<Guest>) {
        guests = newGuests
        notifyDataSetChanged()
    }
}