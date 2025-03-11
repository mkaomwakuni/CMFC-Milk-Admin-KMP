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
package cnc.coop.milkcreamies.core.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Extension functions for better code reusability following MVVM best practices
 */

// Date Extensions
fun LocalDate.toDisplayString(): String = "${this.dayOfMonth}/${this.monthNumber}/${this.year}"

fun LocalDateTime.toDisplayString(): String {
    val minute = if (this.minute < 10) "0${this.minute}" else "${this.minute}"
    return "${this.dayOfMonth}/${this.monthNumber}/${this.year} ${this.hour}:$minute"
}

// String Extensions
fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
    return this.matches(emailRegex)
}

fun String.capitalize(): String {
    return if (this.isNotEmpty()) {
        this.first().uppercaseChar() + this.substring(1).lowercase()
    } else {
        this
    }
}

fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - suffix.length) + suffix
    }
}

// Number Extensions
fun Double.toCurrency(currencySymbol: String = "KES"): String {
    return "$currencySymbol $this"
}

fun Int.toOrdinal(): String {
    val suffix = when {
        this % 100 in 11..13 -> "th"
        this % 10 == 1 -> "st"
        this % 10 == 2 -> "nd"
        this % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}

// Collection Extensions
fun <T> List<T>.isNotNullOrEmpty(): Boolean {
    return this.isNotEmpty()
}

fun <T> List<T>.safeGet(index: Int): T? {
    return if (index >= 0 && index < this.size) {
        this[index]
    } else {
        null
    }
}

// Boolean Extensions
fun Boolean.toYesNo(): String = if (this) "Yes" else "No"

// Validation Extensions
fun String.isValidCowName(): Boolean {
    return this.isNotBlank() && this.length >= 2 && this.length <= 50
}

fun Double.isValidMilkQuantity(): Boolean {
    return this > 0 && this <= 100.0
}

fun Int.isValidCowAge(): Boolean {
    return this in 1..8
}

fun Double.isValidWeight(): Boolean {
    return this > 0 && this <= 2000.0
}
