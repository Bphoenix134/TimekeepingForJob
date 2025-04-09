package com.example.timemanagerforjob.presentation.work

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.usecase.GetDaysOfMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class WorkViewModel @Inject constructor(
    private val getDaysOfMonthUseCase: GetDaysOfMonthUseCase
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDays = MutableStateFlow(setOf<Int>())
    val selectedDays: StateFlow<Set<Int>> = _selectedDays.asStateFlow()

    private val _daysOfMonth = MutableStateFlow<List<Int>>(emptyList())
    val daysOfMonth: StateFlow<List<Int>> = _daysOfMonth.asStateFlow()

    init {
        loadDaysOfMonth()
        autoSelectWeekends()
    }

    private fun loadDaysOfMonth() {
        viewModelScope.launch {
            _daysOfMonth.value = getDaysOfMonthUseCase(_currentMonth.value)
        }
    }

    private fun autoSelectWeekends() {
        val weekends = _daysOfMonth.value.filter { day ->
            val date = LocalDate.of(_currentMonth.value.year, _currentMonth.value.month, day)
            date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        }
        _selectedDays.value += weekends
    }

    fun toggleDaySelection(day: Int) {
        _selectedDays.value = if (_selectedDays.value.contains(day)) {
            _selectedDays.value - day
        } else {
            _selectedDays.value + day
        }
    }

    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        loadDaysOfMonth()
        autoSelectWeekends()
    }

    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
        loadDaysOfMonth()
        autoSelectWeekends()
    }
}