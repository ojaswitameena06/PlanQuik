package com.example.eventplanner.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.eventplanner.R
import com.example.eventplanner.data.entity.Event
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EventUi {

    fun categoryColor(context: Context, category: String): Int {
        val res = when (category) {
            "Birthday" -> R.color.cat_birthday
            "Wedding" -> R.color.cat_wedding
            "Meeting" -> R.color.cat_meeting
            "Party" -> R.color.cat_party
            "Conference" -> R.color.cat_conference
            else -> R.color.cat_other
        }
        return ContextCompat.getColor(context, res)
    }

    fun eventMillis(event: Event): Long? = try {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .parse("${event.date} ${event.time}")?.time
    } catch (e: Exception) {
        null
    }

    fun format(millis: Long, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    fun countdown(targetMillis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = targetMillis - now
        if (diff <= 0) return "Happening now"
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "in under a minute"
            minutes < 60 -> "in $minutes min"
            hours < 24 -> "in $hours hr"
            days < 14 -> "in $days ${if (days == 1L) "day" else "days"}"
            else -> "in ${days / 7} weeks"
        }
    }

    fun greeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun userName(): String {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: ""
        return user?.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
    }
}