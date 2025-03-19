/*
 * Copyright 2025  MkaoCodes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cnc.coop.milkcreamies.presentation.viewmodel.base

import cnc.coop.milkcreamies.presentation.ui.common.state.UiEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Base ViewModel that provides common functionality for all ViewModels
 */
open class BaseViewModel(
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : CoroutineScope {

    // Job that will be canceled when ViewModel is destroyed
    private val job = SupervisorJob()

    // CoroutineContext using the job and dispatcher
    override val coroutineContext: CoroutineContext
        get() = job + mainDispatcher

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // UI Events channel
    private val _uiEvents = Channel<UiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    protected fun setError(message: String?) {
        _errorMessage.value = message
    }

    protected fun sendEvent(event: UiEvent) {
        launch {
            _uiEvents.send(event)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Handle exceptions in a standardized way
     */
    protected fun handleException(
        exception: Throwable,
        customMessage: String? = null
    ) {
        val message = customMessage ?: when {
            exception.message?.contains("Connection") == true ||
                    exception.message?.contains("connect") == true ->
                "Failed to connect to server. Please check your connection."

            exception.message?.contains("timeout") == true ->
                "Request timed out. Please try again."
            else -> exception.message ?: "An unexpected error occurred"
        }
        setError(message)
        setLoading(false)
    }

    /**
     * Execute a suspending operation with loading and error handling
     */
    protected fun executeWithLoading(
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        launch {
            try {
                if (showLoading) {
                    _isLoading.value = true
                }
                block()
            } catch (e: Exception) {
                setError("Operation failed: ${e.message}")
            } finally {
                if (showLoading) {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Execute a suspending operation with loading, error handling, and specific error handler
     */
    protected fun executeWithErrorHandling(
        showLoading: Boolean = true,
        errorHandler: (Exception) -> String = { "Operation failed: ${it.message}" },
        operation: suspend () -> Unit
    ) {
        launch {
            try {
                if (showLoading) setLoading(true)
                operation()
                if (showLoading) setLoading(false)
            } catch (e: Exception) {
                if (showLoading) setLoading(false)
                setError(errorHandler(e))
            }
        }
    }

    /**
     * Release resources when ViewModel is no longer used
     */
    fun onCleared() {
        job.cancel()
    }
}
