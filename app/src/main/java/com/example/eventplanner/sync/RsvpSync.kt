package com.example.eventplanner.sync

import android.content.Context
import com.example.eventplanner.data.database.AppDatabase
import com.example.eventplanner.notifications.NotificationHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Watches the invitation of every guest who has been sent a link.
 * When a guest replies on the web page, the reply is written into the local
 * database, so the guest list updates by itself.
 */
object RsvpSync {

    private class Registration(val ref: DatabaseReference, val listener: ValueEventListener)

    private var scope: CoroutineScope? = null
    private val registrations = mutableMapOf<String, Registration>()

    fun start(context: Context) {
        if (scope != null) return
        val appContext = context.applicationContext
        val dao = AppDatabase.getDatabase(appContext).guestDao()
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = newScope

        newScope.launch {
            dao.getGuestsWithTokens().collect { guests ->
                syncListeners(appContext, guests.map { it.inviteToken }.toSet())
            }
        }
    }

    fun stop() {
        registrations.values.forEach { it.ref.removeEventListener(it.listener) }
        registrations.clear()
        scope?.cancel()
        scope = null
    }

    private fun syncListeners(context: Context, wanted: Set<String>) {
        val iterator = registrations.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in wanted) {
                entry.value.ref.removeEventListener(entry.value.listener)
                iterator.remove()
            }
        }

        wanted.filter { it !in registrations }.forEach { token ->
            val ref = FirebaseDatabase.getInstance()
                .getReference("invites").child(token).child("reply")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handleReply(context, token, snapshot)
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            ref.addValueEventListener(listener)
            registrations[token] = Registration(ref, listener)
        }
    }

    private fun handleReply(context: Context, token: String, snapshot: DataSnapshot) {
        if (!snapshot.exists()) return
        val status = snapshot.child("status").getValue(String::class.java) ?: return
        val partySize = ((snapshot.child("partySize").value as? Number)?.toInt() ?: 1).coerceAtLeast(1)
        val note = snapshot.child("note").getValue(String::class.java) ?: ""
        val respondedAt = (snapshot.child("respondedAt").value as? Number)?.toLong() ?: 0L

        scope?.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val guest = db.guestDao().getGuestByToken(token) ?: return@launch

            val changed = guest.rsvpStatus != status || guest.partySize != partySize ||
                    guest.note != note || guest.respondedAt != respondedAt
            if (changed) {
                db.guestDao().applyRsvp(token, status, partySize, note, respondedAt)
            }

            // Notify only for a genuinely new reply from the guest
            if (respondedAt > guest.respondedAt && status != "Pending") {
                val event = db.eventDao().getEventById(guest.eventId)
                val verb = if (status == "Accepted") "accepted" else "declined"
                val party = if (status == "Accepted" && partySize > 1) " (${partySize} people)" else ""
                NotificationHelper.showNotification(
                    context,
                    token.hashCode(),
                    "${guest.name} $verb your invite",
                    "${event?.title ?: "Your event"}$party",
                    guest.eventId
                )
            }
        }
    }
}