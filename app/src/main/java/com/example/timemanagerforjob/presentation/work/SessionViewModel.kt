package com.example.timemanagerforjob.presentation.work

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.domain.model.WorkSession
import com.example.timemanagerforjob.domain.usecases.ManageTimeReportUseCase
import com.example.timemanagerforjob.services.WorkSessionService
import com.example.timemanagerforjob.utils.ErrorHandler
import com.example.timemanagerforjob.utils.events.SessionEvent
import com.example.timemanagerforjob.utils.events.SessionEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class SessionViewModel @Inject constructor(
    private val manageTimeReportUseCase: ManageTimeReportUseCase,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SessionEventBus.sessionEvents.collect { event ->
                when (event) {
                    is SessionEvent.SessionStopped -> {
                        Log.d("SessionViewModel", "Received stop event: ${event.timeReport}")
                        _uiState.update {
                            it.copy(
                                sessionState = null,
                                workedTime = event.timeReport.workTime,
                                isPaused = false,
                                errorMessage = null
                            )
                        }
                    }
                    is SessionEvent.SessionPausedResumed -> {
                        Log.d("SessionViewModel", "Received pause/resume event: ${event.session}, isPaused=${event.isPaused}")
                        _uiState.update {
                            it.copy(
                                sessionState = event.session,
                                isPaused = event.isPaused,
                                workedTime = event.session.calculateWorkTime(),
                                errorMessage = null
                            )
                        }
                    }
                    is SessionEvent.SessionUpdated -> {
                        Log.d("SessionViewModel", "Received update event: workTime=${event.workTime}, isPaused=${event.isPaused}")
                        _uiState.update {
                            it.copy(
                                sessionState = event.session,
                                workedTime = event.workTime,
                                isPaused = event.isPaused,
                                errorMessage = null
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun startTimeReport() {
        viewModelScope.launch {
            Log.d("SessionViewModel", "Attempting to start session for date: ${LocalDate.now()}")
            when (val result = manageTimeReportUseCase.startSession(LocalDate.now())) {
                is Result.Success -> {
                    Log.d("SessionViewModel", "Session started: ${result.value}")
                    _uiState.update {
                        it.copy(
                            sessionState = result.value,
                            isPaused = result.value.pauses.any { it.second == null },
                            workedTime = result.value.calculateWorkTime(),
                            errorMessage = null
                        )
                    }
                    WorkSessionService.startService(context, result.value)
                }
                is Result.Failure -> {
                    Log.e("SessionViewModel", "Failed to start session: ${result.exception}")
                    val errorMessage = when (result.exception.message) {
                        "Active session already exists for this date" -> "Сеанс уже активен. Завершите текущий сеанс."
                        "Session already completed for this date" -> "Сеанс за ${LocalDate.now()} уже завершён. Вы можете начать новый сеанс завтра."
                        else -> "Не удалось начать сеанс: ${result.exception.message}"
                    }
                    ErrorHandler.emitError(errorMessage)
                }
            }
        }
    }

    fun stopTimeReport() {
        WorkSessionService.stopService(context)
    }

    fun pauseTimeReport() {
        val pauseResumeIntent = Intent(context, WorkSessionService::class.java).apply {
            action = WorkSessionService.ACTION_PAUSE_RESUME
            putExtra("isPaused", false)
        }
        ContextCompat.startForegroundService(context, pauseResumeIntent)
    }

    fun resumeTimeReport() {
        val pauseResumeIntent = Intent(context, WorkSessionService::class.java).apply {
            action = WorkSessionService.ACTION_PAUSE_RESUME
            putExtra("isPaused", true)
        }
        ContextCompat.startForegroundService(context, pauseResumeIntent)
    }
}

data class SessionUiState(
    val sessionState: WorkSession? = null,
    val workedTime: Long = 0L,
    val isPaused: Boolean = false,
    val errorMessage: String? = null
)