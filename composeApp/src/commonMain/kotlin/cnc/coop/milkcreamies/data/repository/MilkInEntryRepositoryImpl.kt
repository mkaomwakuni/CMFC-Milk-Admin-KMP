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
package cnc.coop.milkcreamies.data.repository

import cnc.coop.milkcreamies.data.remote.MilkClient
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import cnc.coop.milkcreamies.models.MilkInEntry
import cnc.coop.milkcreamies.models.MilkInRequest
import cnc.coop.milkcreamies.models.MilkCollectionException

class MilkInEntryRepositoryImpl(
    private val milkClient: MilkClient
) : MilkInEntryRepository {

    override suspend fun getMilkInEntryById(milkEntryId: String): MilkInEntry? {
        val entries = milkClient.getMilkInEntries().getOrNull()
        return entries?.find { it.entryId == milkEntryId }
    }

    override suspend fun getAllMilkInEntries(): List<MilkInEntry> {
        return milkClient.getMilkInEntries().getOrNull() ?: emptyList()
    }

    override suspend fun addMilkInEntry(entry: MilkInEntry): MilkInEntry {
        try {
            // Basic validation
            if (entry.milkingType.toString().isBlank()) {
                throw IllegalArgumentException("Milk type cannot be blank")
            }
            if (entry.liters <= 0) {
                throw IllegalArgumentException("Quantity must be greater than zero")
            }
            if (entry.ownerId.isBlank()) {
                throw IllegalArgumentException("Owner ID cannot be blank")
            }

            // Create a request model with ID field explicitly set to null - similar to Member approach
            val milkInRequest = MilkInRequest(
                entryId = null,  // Explicitly set to null - server will generate the ID
                cowId = entry.cowId ?: "",
                ownerId = entry.ownerId,
                liters = entry.liters,
                date = entry.date.toString(), // Format date as YYYY-MM-DD string
                milkingType = entry.milkingType.toString() // Format milkingType as string
            )

            val result = milkClient.addMilkInEntry(milkInRequest)
            if (result.isSuccess) {
                return result.getOrThrow()
            } else {
                val exception = result.exceptionOrNull()
                // If it's a MilkCollectionException, throw it directly to preserve the specific error information
                if (exception is MilkCollectionException) {
                    throw exception
                } else {
                    throw Exception("Failed to save milk entry to server: ${exception?.message}")
                }
            }
        } catch (e: MilkCollectionException) {
            // Re-throw MilkCollectionException directly to preserve error details
            throw e
        } catch (e: Exception) {
            throw Exception("Unable to add milk entry: ${e.message}", e)
        }
    }

    override suspend fun deleteMilkInEntry(entryId: String): Boolean {
        // Since MilkClient doesn't have a delete method for milk entries yet,
        // we'll implement this when the API is available
        throw NotImplementedError("Delete MilkInEntry operation not yet implemented on the server")
    }
}
