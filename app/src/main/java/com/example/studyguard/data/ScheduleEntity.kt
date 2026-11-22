package com.example.studyguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val day: String, // "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
    val time: String, // "08:00 - 09:40"
    val subject: String, // "Pemrograman Mobile"
    val room: String, // "Lab Komputer 3"
    val lecturer: String? = null // "Dr. Budi Santoso"
)
