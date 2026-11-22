package com.example.studyguard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [StudySessionEntity::class, ScheduleEntity::class], version = 3, exportSchema = false)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext ?: context
                val instance = Room.databaseBuilder(
                    appContext,
                    StudyDatabase::class.java,
                    "study_guard_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = INSTANCE?.scheduleDao() ?: return@launch
                                val defaultSchedules = listOf(
                                    // Senin
                                    ScheduleEntity(day = "Senin", time = "07:00 - 08:30", subject = "B. INGGRIS", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Senin", time = "08:30 - 10:00", subject = "PENJAS", room = "Lapangan Sekolah"),
                                    ScheduleEntity(day = "Senin", time = "10:30 - 12:00", subject = "ANDROID", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Senin", time = "12:00 - 12:45", subject = "BASIS DATA", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Senin", time = "13:30 - 14:50", subject = "BASIS DATA", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Senin", time = "14:50 - 15:30", subject = "Public Speaking", room = "Ruang Kelas XI - RPL"),

                                    // Selasa
                                    ScheduleEntity(day = "Selasa", time = "07:00 - 08:30", subject = "B. JEPANG", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Selasa", time = "08:30 - 09:15", subject = "PBO", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Selasa", time = "09:15 - 10:00", subject = "ANDROID", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Selasa", time = "10:30 - 12:00", subject = "MTK", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Selasa", time = "12:00 - 13:30", subject = "PBO", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Selasa", time = "14:10 - 15:30", subject = "B. INDO", room = "Ruang Kelas XI - RPL"),

                                    // Rabu
                                    ScheduleEntity(day = "Rabu", time = "07:00 - 08:30", subject = "PBO", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Rabu", time = "08:30 - 10:00", subject = "PGL", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Rabu", time = "10:30 - 11:15", subject = "PGA", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Rabu", time = "11:15 - 14:10", subject = "FRAMEWORK", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Rabu", time = "14:10 - 15:30", subject = "KIK", room = "Ruang Kelas XI - RPL"),

                                    // Kamis
                                    ScheduleEntity(day = "Kamis", time = "07:00 - 08:30", subject = "KIK", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Kamis", time = "08:30 - 09:15", subject = "B. INDO", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Kamis", time = "09:15 - 11:15", subject = "MTK", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Kamis", time = "11:15 - 14:10", subject = "PKN", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Kamis", time = "14:10 - 14:50", subject = "KEB-BK", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Kamis", time = "14:50 - 15:30", subject = "BISNIS RETAIL", room = "Ruang Kelas XI - RPL"),

                                    // Jumat
                                    ScheduleEntity(day = "Jumat", time = "07:00 - 07:45", subject = "B. INGGRIS", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Jumat", time = "07:45 - 08:30", subject = "ETIKA PROFESI", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Jumat", time = "08:30 - 10:00", subject = "AGAMA", room = "Ruang Kelas XI - RPL"),
                                    ScheduleEntity(day = "Jumat", time = "10:25 - 11:45", subject = "FRAMEWORK", room = "Lab Komputer RPL"),
                                    ScheduleEntity(day = "Jumat", time = "13:00 - 14:30", subject = "SEJARAH", room = "Ruang Kelas XI - RPL")
                                )
                                defaultSchedules.forEach { dao.insertSchedule(it) }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
