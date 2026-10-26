package com.example.eventplanner

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class PlanQuikApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val dark = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}