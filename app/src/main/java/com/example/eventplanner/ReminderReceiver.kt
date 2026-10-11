package com.example.eventplanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.eventplanner.notifications.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Event Reminder"
        val eventLocation = intent.getStringExtra("EVENT_LOCATION") ?: ""
        val eventId = intent.getLongExtra("EVENT_ID", 0L)

        val message = if (eventLocation.isNotEmpty()) {
            "Coming up at $eventLocation"
        } else {
            "Your event is coming up soon"
        }

        NotificationHelper.showNotification(
            context,
            eventId.toInt(),
            eventTitle,
            message
        )
    }
}