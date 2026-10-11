package com.example.eventplanner.sync

import com.example.eventplanner.data.entity.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseSyncHelper {

    private fun getUserEventsRef() = FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
        FirebaseDatabase.getInstance().getReference("events").child(uid)
    }

    fun pushEvent(event: Event) {
        val ref = getUserEventsRef() ?: return
        ref.child(event.id.toString()).setValue(event)
    }

    fun deleteEvent(eventId: Long) {
        val ref = getUserEventsRef() ?: return
        ref.child(eventId.toString()).removeValue()
    }
}