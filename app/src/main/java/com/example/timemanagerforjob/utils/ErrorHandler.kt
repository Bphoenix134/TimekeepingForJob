package com.example.timemanagerforjob.utils

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object ErrorHandler {
    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow

    fun emitError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _errorFlow.emit(message)
        }
    }

    suspend fun handleErrors(snackbarHostState: SnackbarHostState) {
        errorFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
}

