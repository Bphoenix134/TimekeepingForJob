package com.example.timemanagerforjob.presentation.work

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.repository.CalendarRepository
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
    private val getDaysOfMonthUseCase: GetDaysOfMonthUseCase,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDaysPerMonth = mutableMapOf<YearMonth, MutableSet<Int>>()
    private val _selectedDays = MutableStateFlow<Set<Int>>(emptySet())
    val selectedDays: StateFlow<Set<Int>> = _selectedDays.asStateFlow()

    private val _daysOfMonth = MutableStateFlow<List<Int>>(emptyList())
    val daysOfMonth: StateFlow<List<Int>> = _daysOfMonth.asStateFlow()

    init {
        loadDaysOfMonth()
    }

    private fun loadDaysOfMonth() {
        viewModelScope.launch {
            val month = _currentYearMonth.value
            _daysOfMonth.value = getDaysOfMonthUseCase(month)
            updateSelectedDays()
        }
    }

    private fun updateSelectedDays() {
        val month = _currentYearMonth.value

        viewModelScope.launch {
            val savedDays = calendarRepository.getSelectedDays(month.year, month.monthValue)
            val defaultDays = getDefaultWeekendAndHolidays(month)

            val selectedDaysSet = (defaultDays + savedDays).toSet()

            _selectedDaysPerMonth[month] = selectedDaysSet.toMutableSet()
            _selectedDays.value = selectedDaysSet
        }
    }

    private fun getDefaultWeekendAndHolidays(month: YearMonth): Set<Int> {
        val weekends = (1..month.lengthOfMonth()).filter { day ->
            val date = LocalDate.of(month.year, month.month, day)
            date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        }

        return (weekends).toSet()
    }

    fun toggleDaySelection(day: Int) {
        val month = _currentYearMonth.value
        val days = _selectedDaysPerMonth.getOrPut(month) { mutableSetOf() }

        val updated = days.toMutableSet()

        viewModelScope.launch {
            if (updated.contains(day)) {
                updated.remove(day)
                calendarRepository.removeSelectedDay(day, month.monthValue, month.year)
            } else {
                updated.add(day)
                calendarRepository.saveSelectedDay(day, month.monthValue, month.year)
            }

            _selectedDaysPerMonth[month] = updated
            _selectedDays.value = updated.toSet()
        }
    }

    fun goToNextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
        loadDaysOfMonth()
    }

    fun goToPreviousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
        loadDaysOfMonth()
    }
}