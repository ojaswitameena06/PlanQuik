package com.example.eventplanner

data class CalendarDay(
    val day: Int,
    val month: Int,
    val year: Int,
    val isCurrentMonth: Boolean
)