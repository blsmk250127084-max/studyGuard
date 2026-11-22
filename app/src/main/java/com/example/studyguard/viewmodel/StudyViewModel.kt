package com.example.studyguard.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyguard.data.ScheduleEntity
import com.example.studyguard.data.StudyDatabase
import com.example.studyguard.data.StudyRepository
import com.example.studyguard.data.StudySessionEntity
import com.example.studyguard.sensor.SensorManagerHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class StudySessionState {
    IDLE,
    PREPARING,
    FOCUSING,
    VIOLATION,
    COMPLETED,
    FAILED
}

enum class FocusSoundscape(val title: String, val desc: String) {
    OFF("Hening", "Tanpa suara latar"),
    RAIN("Hujan", "Ritme hujan konstan"),
    NATURE("Alam", "Suasana ketenangan hutan"),
    OCEAN("Ombak", "Deru ombak menenangkan")
}

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val progressText: String
)

data class DailyFocusStat(
    val dayLabel: String,
    val minutes: Int,
    val isToday: Boolean
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StudyDatabase.getDatabase(application)
    private val repository = StudyRepository(database.studyDao())
    private val sensorHelper = SensorManagerHelper(application)

    val allSessions: StateFlow<List<StudySessionEntity>> = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val scheduleDao = database.scheduleDao()

    val allSchedules: StateFlow<List<ScheduleEntity>> = scheduleDao.getAllSchedules().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleDao.insertSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleDao.deleteSchedule(schedule)
        }
    }

    private val _subject = MutableStateFlow("Pemrograman Mobile")
    val subject: StateFlow<String> = _subject.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _selectedDurationMinutes = MutableStateFlow(25)
    val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

    private val _selectedSoundscape = MutableStateFlow(FocusSoundscape.OFF)
    val selectedSoundscape: StateFlow<FocusSoundscape> = _selectedSoundscape.asStateFlow()

    private val _sessionState = MutableStateFlow(StudySessionState.IDLE)
    val sessionState: StateFlow<StudySessionState> = _sessionState.asStateFlow()

    private val _timeLeftSeconds = MutableStateFlow(25 * 60)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

    private val _prepTimeLeft = MutableStateFlow(3)
    val prepTimeLeft: StateFlow<Int> = _prepTimeLeft.asStateFlow()

    private val _graceTimeLeft = MutableStateFlow(5)
    val graceTimeLeft: StateFlow<Int> = _graceTimeLeft.asStateFlow()

    private val _alarmCount = MutableStateFlow(0)
    val alarmCount: StateFlow<Int> = _alarmCount.asStateFlow()

    private val _violationReason = MutableStateFlow("")
    val violationReason: StateFlow<String> = _violationReason.asStateFlow()

    private val _isProximityClose = MutableStateFlow(false)
    val isProximityClose: StateFlow<Boolean> = _isProximityClose.asStateFlow()

    private val _isMotionDetected = MutableStateFlow(false)
    val isMotionDetected: StateFlow<Boolean> = _isMotionDetected.asStateFlow()

    private val _isPositionViolated = MutableStateFlow(false)
    val isPositionViolated: StateFlow<Boolean> = _isPositionViolated.asStateFlow()

    // Calculated Streak Flow
    val currentStreak: StateFlow<Int> = allSessions.map { sessions ->
        calculateStreak(sessions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Calculated Weekly Stats Flow
    val weeklyStats: StateFlow<List<DailyFocusStat>> = allSessions.map { sessions ->
        calculateWeeklyStats(sessions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calculated Achievement Badges Flow
    val achievementBadges: StateFlow<List<AchievementBadge>> = allSessions.map { sessions ->
        val streak = calculateStreak(sessions)
        calculateBadges(sessions, streak)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var timerJob: Job? = null
    private var sensorJob: Job? = null
    private var graceJob: Job? = null

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startContinuousSensorMonitoring()
        checkAndPreloadDefaultSchedule()
    }

    private fun checkAndPreloadDefaultSchedule() {
        viewModelScope.launch {
            try {
                val currentSchedules = scheduleDao.getAllSchedules().first()
                if (currentSchedules.isEmpty()) {
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
                    defaultSchedules.forEach { scheduleDao.insertSchedule(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startContinuousSensorMonitoring() {
        sensorJob?.cancel()
        sensorJob = viewModelScope.launch {
            launch {
                sensorHelper.proximityFlow.collect { isClose ->
                    _isProximityClose.value = isClose
                    if (isClose && _sessionState.value == StudySessionState.FOCUSING) {
                        triggerViolation("Wajah atau tangan mendekati layar HP!")
                    }
                }
            }
            launch {
                sensorHelper.motionFlow.collect { isMoving ->
                    _isMotionDetected.value = isMoving
                    if (isMoving && _sessionState.value == StudySessionState.FOCUSING) {
                        triggerViolation("HP diangkat, digeser, atau disentuh!")
                    }
                }
            }
            launch {
                sensorHelper.positionFlow.collect { isPositionViolated ->
                    _isPositionViolated.value = isPositionViolated
                    if (isPositionViolated && _sessionState.value == StudySessionState.FOCUSING) {
                        triggerViolation("Posisi HP miring atau diangkat dari meja!")
                    }
                }
            }
        }
    }

    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    fun simulateProximityTrigger(isClose: Boolean) {
        _isProximityClose.value = isClose
        if (isClose && _sessionState.value == StudySessionState.FOCUSING) {
            triggerViolation("Wajah atau tangan mendekati layar HP!")
        }
    }

    fun setSubject(newSubject: String) {
        _subject.value = newSubject
    }

    fun setNotes(newNotes: String) {
        _notes.value = newNotes
    }

    fun setDuration(minutes: Int) {
        _selectedDurationMinutes.value = minutes
        _timeLeftSeconds.value = minutes * 60
    }

    fun setSoundscape(soundscape: FocusSoundscape) {
        _selectedSoundscape.value = soundscape
    }

    fun startFocusSession() {
        if (_sessionState.value != StudySessionState.IDLE && _sessionState.value != StudySessionState.COMPLETED && _sessionState.value != StudySessionState.FAILED) return

        _alarmCount.value = 0
        _violationReason.value = ""
        _timeLeftSeconds.value = _selectedDurationMinutes.value * 60
        _sessionState.value = StudySessionState.PREPARING
        _prepTimeLeft.value = 3

        // Start 3-second preparation countdown
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _prepTimeLeft.value = i
                delay(1000L)
            }
            _sessionState.value = StudySessionState.FOCUSING
            startTimerAndSensorMonitoring()
        }
    }

    private fun startTimerAndSensorMonitoring() {
        // Start main study countdown timer
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeftSeconds.value > 0) {
                delay(1000L)
                if (_sessionState.value == StudySessionState.FOCUSING) {
                    _timeLeftSeconds.value -= 1
                }
            }
            if (_sessionState.value == StudySessionState.FOCUSING) {
                completeSession()
            }
        }
    }

    private fun triggerViolation(reason: String) {
        if (_sessionState.value != StudySessionState.FOCUSING) return

        _sessionState.value = StudySessionState.VIOLATION
        _violationReason.value = reason
        _alarmCount.value += 1

        playAlarmAndVibrate()

        // Start 5-second grace period
        graceJob?.cancel()
        graceJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _graceTimeLeft.value = i
                delay(1000L)
                // Check if user has returned phone to normal (all 3 sensors false)
                if (!_isProximityClose.value && !_isMotionDetected.value && !_isPositionViolated.value) {
                    // Grace period passed successfully, resume focusing
                    _sessionState.value = StudySessionState.FOCUSING
                    return@launch
                }
            }
            // If still in violation after 5 seconds, session FAILS
            failSession(reason)
        }
    }

    private fun playAlarmAndVibrate() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1500)
            vibrator?.let { vib ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(1000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun completeSession() {
        stopAllJobs()
        _sessionState.value = StudySessionState.COMPLETED
        val totalSec = _selectedDurationMinutes.value * 60

        viewModelScope.launch {
            repository.insertSession(
                StudySessionEntity(
                    subject = _subject.value.ifBlank { "Sesi Belajar" },
                    targetDurationMinutes = _selectedDurationMinutes.value,
                    achievedDurationSeconds = totalSec,
                    alarmCount = _alarmCount.value,
                    status = "SUCCESS",
                    failureReason = null,
                    notes = _notes.value.ifBlank { null }
                )
            )
        }
    }

    private fun failSession(reason: String) {
        stopAllJobs()
        _sessionState.value = StudySessionState.FAILED
        val totalTargetSec = _selectedDurationMinutes.value * 60
        val achievedSec = totalTargetSec - _timeLeftSeconds.value

        viewModelScope.launch {
            repository.insertSession(
                StudySessionEntity(
                    subject = _subject.value.ifBlank { "Sesi Belajar" },
                    targetDurationMinutes = _selectedDurationMinutes.value,
                    achievedDurationSeconds = maxOf(achievedSec, 0),
                    alarmCount = _alarmCount.value,
                    status = "FAILED",
                    failureReason = reason,
                    notes = _notes.value.ifBlank { null }
                )
            )
        }
    }

    fun resumeFocusFromViolation() {
        if (_sessionState.value == StudySessionState.VIOLATION) {
            graceJob?.cancel()
            _sessionState.value = StudySessionState.FOCUSING
        }
    }

    fun cancelSessionManually() {
        stopAllJobs()
        failSession("Sesi dibatalkan secara manual oleh pengguna.")
    }

    fun resetToIdle() {
        stopAllJobs()
        _sessionState.value = StudySessionState.IDLE
        _timeLeftSeconds.value = _selectedDurationMinutes.value * 60
    }

    fun deleteSession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllSessions()
        }
    }

    private fun stopAllJobs() {
        timerJob?.cancel()
        graceJob?.cancel()
    }

    private fun calculateStreak(sessions: List<StudySessionEntity>): Int {
        val successfulSessions = sessions.filter { it.status == "SUCCESS" }
        if (successfulSessions.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uniqueDays = successfulSessions
            .map { dateFormat.format(Date(it.timestamp)) }
            .distinct()
            .sortedDescending()

        if (uniqueDays.isEmpty()) return 0

        val todayStr = dateFormat.format(Date())
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = dateFormat.format(yesterdayCal.time)

        val lastActiveDay = uniqueDays.first()
        if (lastActiveDay != todayStr && lastActiveDay != yesterdayStr) {
            return 0
        }

        var streak = 0
        val checkCal = Calendar.getInstance()
        if (lastActiveDay == yesterdayStr) {
            checkCal.time = yesterdayCal.time
        }

        for (i in 0..365) {
            val dateStr = dateFormat.format(checkCal.time)
            if (uniqueDays.contains(dateStr)) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateWeeklyStats(sessions: List<StudySessionEntity>): List<DailyFocusStat> {
        val stats = mutableListOf<DailyFocusStat>()
        val dayNames = arrayOf("Ming", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val calendar = Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)

        // Get past 7 days (including today)
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(cal.time)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            val dayLabel = dayNames[dayOfWeek]

            val totalSec = sessions
                .filter { it.status == "SUCCESS" && dateFormat.format(Date(it.timestamp)) == dateStr }
                .sumOf { it.achievedDurationSeconds }

            stats.add(
                DailyFocusStat(
                    dayLabel = dayLabel,
                    minutes = totalSec / 60,
                    isToday = dateStr == todayStr
                )
            )
        }

        return stats
    }

    private fun calculateBadges(sessions: List<StudySessionEntity>, streak: Int): List<AchievementBadge> {
        val successfulSessions = sessions.filter { it.status == "SUCCESS" }
        val totalMinutes = successfulSessions.sumOf { it.achievedDurationSeconds } / 60

        return listOf(
            AchievementBadge(
                id = "first_session",
                title = "Pemula Fokus",
                description = "Selesaikan 1 sesi fokus pertama Anda",
                icon = "🌱",
                isUnlocked = successfulSessions.isNotEmpty(),
                progressText = if (successfulSessions.isNotEmpty()) "Terbuka" else "0/1 Sesi"
            ),
            AchievementBadge(
                id = "streak_3",
                title = "Pejuang Konsisten",
                description = "Capai 3 hari streak belajar berturut-turut",
                icon = "⚡",
                isUnlocked = streak >= 3,
                progressText = if (streak >= 3) "Terbuka" else "$streak/3 Hari"
            ),
            AchievementBadge(
                id = "streak_7",
                title = "Master Pomodoro",
                description = "Capai 7 hari streak belajar berturut-turut",
                icon = "🔥",
                isUnlocked = streak >= 7,
                progressText = if (streak >= 7) "Terbuka" else "$streak/7 Hari"
            ),
            AchievementBadge(
                id = "shield_master",
                title = "Perisai Nir-Peringatan",
                description = "Selesaikan sesi tanpa ada peringatan sensor sama sekali",
                icon = "🛡️",
                isUnlocked = successfulSessions.any { it.alarmCount == 0 },
                progressText = if (successfulSessions.any { it.alarmCount == 0 }) "Terbuka" else "Belum Capai"
            ),
            AchievementBadge(
                id = "time_500",
                title = "Legenda Fokus",
                description = "Akumulasi total 500 menit fokus belajar",
                icon = "👑",
                isUnlocked = totalMinutes >= 500,
                progressText = if (totalMinutes >= 500) "Terbuka" else "$totalMinutes/500 Mnt"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopAllJobs()
        sensorJob?.cancel()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
