package com.example.eventplanner.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.eventplanner.ReminderReceiver

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        eventId: Long,
        eventTitle: String,
        eventLocation: String,
        triggerAtMillis: Long
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("EVENT_ID", eventId)
            putExtra("EVENT_TITLE", eventTitle)
            putExtra("EVENT_LOCATION", eventLocation)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}