package com.example.studyguard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM class_schedules ORDER BY day, time")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE day = :day ORDER BY time")
    fun getSchedulesByDay(day: String): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM class_schedules")
    suspend fun deleteAllSchedules()
}
