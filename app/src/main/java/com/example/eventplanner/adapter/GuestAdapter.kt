package com.example.eventplanner.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
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
        val tvAvatar: TextView = itemView.findViewById(R.id.tvGuestAvatar)
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
        val context = holder.itemView.context

        val colorRes = when (guest.rsvpStatus) {
            "Accepted" -> R.color.rsvp_accepted
            "Declined" -> R.color.rsvp_declined
            else -> R.color.rsvp_pending
        }
        val color = ContextCompat.getColor(context, colorRes)

        holder.tvAvatar.text = guest.name.firstOrNull()?.uppercase() ?: "?"
        holder.tvName.text = guest.name
        holder.tvPhone.text = when {
            guest.note.isNotBlank() -> "“${guest.note}”"
            else -> guest.phone.ifBlank { guest.email.ifBlank { "No contact details" } }
        }

        holder.tvRsvp.text =
            if (guest.rsvpStatus == "Accepted" && guest.partySize > 1) "Accepted · ${guest.partySize}"
            else guest.rsvpStatus
        holder.tvRsvp.setTextColor(color)
        holder.tvRsvp.backgroundTintList =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 40))

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