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
package cnc.coop.milkcreamies.presentation.ui.common.state

/**
 * Represents the state of a UI component that can be in loading, success, or error state
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
}

/**
 * Represents the state of data loading operations
 */
sealed class LoadingState {
    data object Idle : LoadingState()
    data object Loading : LoadingState()
    data object Success : LoadingState()
    data class Error(val message: String) : LoadingState()
}

/**
 * Represents different types of UI events that can occur
 */
sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    data class ShowError(val error: String) : UiEvent()
    data object NavigateBack : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

/**
 * Extension functions for UiState
 */
val <T> UiState<T>.isLoading: Boolean
    get() = this is UiState.Loading

val <T> UiState<T>.isSuccess: Boolean
    get() = this is UiState.Success

val <T> UiState<T>.isError: Boolean
    get() = this is UiState.Error

val <T> UiState<T>.data: T?
    get() = if (this is UiState.Success) this.data else null

val <T> UiState<T>.errorMessage: String?
    get() = if (this is UiState.Error) this.exception.message else null

/**
 * Extension functions for LoadingState
 */
val LoadingState.isLoading: Boolean
    get() = this is LoadingState.Loading

val LoadingState.isSuccess: Boolean
    get() = this is LoadingState.Success

val LoadingState.isError: Boolean
    get() = this is LoadingState.Error

val LoadingState.errorMessage: String?
    get() = if (this is LoadingState.Error) this.message else null