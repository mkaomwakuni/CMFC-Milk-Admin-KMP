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

import cnc.coop.milkcreamies.domain.repository.MilkSpoiltEntryRepository
import cnc.coop.milkcreamies.models.MilkSpoiltEntry
import cnc.coop.milkcreamies.data.remote.MilkClient

class MilkSpoiltEntryRepositoryImpl(
    private val milkClient: MilkClient
) : MilkSpoiltEntryRepository {

    override suspend fun getMilkSpoiltEntryById(spoiltId: String): MilkSpoiltEntry? {
        return milkClient.getMilkSpoiltEntryById(spoiltId).getOrNull()
    }

    override suspend fun getAllMilkSpoiltEntries(): List<MilkSpoiltEntry> {
        return milkClient.getMilkSpoiltEntries().getOrNull() ?: emptyList()
    }

    override suspend fun addMilkSpoiltEntry(entry: MilkSpoiltEntry): MilkSpoiltEntry {
        val result = milkClient.addMilkSpoiltEntry(entry)
        return result.getOrThrow()
    }

    override suspend fun deleteMilkSpoiltEntry(spoiltId: String): Boolean {
        return milkClient.deleteMilkSpoiltEntry(spoiltId).isSuccess
    }
}
