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
package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.models.MilkInEntry

interface MilkInEntryRepository {
    suspend fun getMilkInEntryById(entryId: String): MilkInEntry?
    suspend fun getAllMilkInEntries(): List<MilkInEntry>

    /**
     * Adds a new milk entry to the database.
     *
     * Note: The implementation will convert this to MilkInRequest internally
     * and ensure entryId is explicitly set to null for the server to generate.
     *
     * @param entry The milk entry to add. The entryId should be null.
     * @return The added milk entry with a server-generated ID.
     */
    suspend fun addMilkInEntry(entry: MilkInEntry): MilkInEntry

    suspend fun deleteMilkInEntry(entryId: String): Boolean
}
