package com.example.timemanagerforjob.utils.events

import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.model.WorkSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEventBus {
    private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    fun emitEvent(event: SessionEvent) {
        _sessionEvents.tryEmit(event)
    }
}

sealed class SessionEvent {
    data class SessionStopped(val timeReport: TimeReport) : SessionEvent()
    data class SessionPausedResumed(val session: WorkSession, val isPaused: Boolean) : SessionEvent()
    data class SessionUpdated(val session: WorkSession, val workTime: Long, val isPaused: Boolean) : SessionEvent()
}