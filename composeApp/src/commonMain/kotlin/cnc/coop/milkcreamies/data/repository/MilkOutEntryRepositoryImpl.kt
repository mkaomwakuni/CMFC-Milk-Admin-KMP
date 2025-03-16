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

import cnc.coop.milkcreamies.domain.repository.MilkOutEntryRepository
import cnc.coop.milkcreamies.models.MilkOutEntry
import cnc.coop.milkcreamies.data.remote.MilkClient

class MilkOutEntryRepositoryImpl(
    private val milkClient: MilkClient
) : MilkOutEntryRepository {

    override suspend fun getMilkOutEntryById(saleId: String): MilkOutEntry? {
        val entries = milkClient.getMilkOutEntries().getOrNull()
        return entries?.find { it.saleId == saleId }
    }

    override suspend fun getAllMilkOutEntries(): List<MilkOutEntry> {
        return milkClient.getMilkOutEntries().getOrNull() ?: emptyList()
    }

    override suspend fun addMilkOutEntry(entry: MilkOutEntry): MilkOutEntry {
        val result = milkClient.addMilkOutEntry(entry)
        return result.getOrThrow()
    }

    override suspend fun deleteMilkOutEntry(saleId: String): Boolean {
        // Since MilkClient doesn't have a delete method for milk out entries yet,
        // we'll implement this when the API is available
        throw NotImplementedError("Delete MilkOutEntry operation not yet implemented on the server")
    }
}
