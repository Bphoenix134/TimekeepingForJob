package com.example.timemanagerforjob.domain.model

sealed class Result<out T> {

    data class Success<out T>(val value: T) : Result<T>()

    data class Failure(val exception: Throwable) : Result<Nothing>()

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }
}