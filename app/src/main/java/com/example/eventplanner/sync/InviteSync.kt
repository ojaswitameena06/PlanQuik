package com.example.eventplanner.sync

import android.content.Context
import android.util.Log
import com.example.eventplanner.data.database.AppDatabase
import com.example.eventplanner.data.entity.Event
import com.example.eventplanner.data.entity.Guest
import com.example.eventplanner.util.EventUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

/** Publishes invitations to Firebase so guests can reply from the RSVP web page. */
object InviteSync {

    // Address of the RSVP page (GitHub Pages). Change if your username or repo name differs.
    const val RSVP_BASE_URL = "https://ojaswitameena06.github.io/planquik-rsvp/"

    private const val TAG = "InviteSync"

    private fun invites() = FirebaseDatabase.getInstance().getReference("invites")

    /** Unguessable per-guest secret (random UUID without dashes). */
    fun newToken(): String = UUID.randomUUID().toString().replace("-", "")

    fun inviteLink(token: String): String = "$RSVP_BASE_URL?t=$token"

    private fun details(event: Event, guestName: String, hostName: String): Map<String, Any> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val millis = EventUi.eventMillis(event)
        val whenText = millis?.let { EventUi.format(it, "EEEE, d MMMM yyyy 'at' h:mm a") }
            ?: "${event.date} ${event.time}"
        return mapOf(
            "ownerUid" to uid,
            "hostName" to hostName,
            "guestName" to guestName,
            "eventTitle" to event.title,
            "eventCategory" to event.category,
            "date" to event.date,
            "time" to event.time,
            "whenText" to whenText,
            "location" to event.location,
            "description" to event.description
        )
    }

    /** Creates the invitation (first send) or refreshes its details (re-send). */
    fun publishInvite(event: Event, guest: Guest, hostName: String, isNew: Boolean) {
        val ref = invites().child(guest.inviteToken)
        val data = details(event, guest.name, hostName)
        if (isNew) {
            val reply: Map<String, Any> = mapOf(
                "status" to "Pending",
                "partySize" to 1,
                "note" to "",
                "respondedAt" to 0
            )
            ref.setValue(data + ("reply" to reply))
                .addOnFailureListener { Log.w(TAG, "publish failed", it) }
        } else {
            // Never touch "reply" on a re-send, so an existing answer is kept
            ref.updateChildren(data)
                .addOnFailureListener { Log.w(TAG, "refresh failed", it) }
        }
    }

    /** Host changed a guest's RSVP by hand, so keep the cloud copy in step. */
    fun setStatus(token: String, status: String) {
        invites().child(token).child("reply").updateChildren(mapOf("status" to status))
            .addOnFailureListener { Log.w(TAG, "status update failed", it) }
    }

    fun deleteInvite(token: String) {
        invites().child(token).removeValue()
            .addOnFailureListener { Log.w(TAG, "delete failed", it) }
    }

    /** The event was edited: update what guests see on their invitation page. */
    suspend fun refreshEventInvites(context: Context, event: Event) {
        val guests = AppDatabase.getDatabase(context).guestDao().getGuestsForEventOnce(event.id)
        val host = EventUi.userName()
        guests.filter { it.inviteToken.isNotBlank() }.forEach { guest ->
            invites().child(guest.inviteToken).updateChildren(details(event, guest.name, host))
        }
    }

    /** The event was deleted: its invitation links stop working. */
    suspend fun deleteInvitesForEvent(context: Context, eventId: Long) {
        val guests = AppDatabase.getDatabase(context).guestDao().getGuestsForEventOnce(eventId)
        guests.filter { it.inviteToken.isNotBlank() }.forEach { deleteInvite(it.inviteToken) }
    }
}