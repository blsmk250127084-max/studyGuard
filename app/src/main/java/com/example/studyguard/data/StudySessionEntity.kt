package com.example.studyguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val subject: String,
    val targetDurationMinutes: Int,
    val achievedDurationSeconds: Int,
    val alarmCount: Int,
    val status: String, // "SUCCESS" or "FAILED"
    val failureReason: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
