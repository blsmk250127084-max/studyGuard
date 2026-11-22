package com.example.studyguard.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {
    val allSessions: Flow<List<StudySessionEntity>> = studyDao.getAllSessions()

    suspend fun insertSession(session: StudySessionEntity) {
        studyDao.insertSession(session)
    }

    suspend fun deleteSession(session: StudySessionEntity) {
        studyDao.deleteSession(session)
    }

    suspend fun deleteAllSessions() {
        studyDao.deleteAllSessions()
    }
}
