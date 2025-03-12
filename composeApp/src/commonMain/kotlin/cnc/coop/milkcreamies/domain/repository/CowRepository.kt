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

import cnc.coop.milkcreamies.models.BulkEligibilityResponse
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.CowEligibilityResponse
import cnc.coop.milkcreamies.models.CowHealthDetailsResponse
import cnc.coop.milkcreamies.models.EarningsSummary
import cnc.coop.milkcreamies.models.MilkAnalytics
import kotlinx.datetime.LocalDate

interface CowRepository {
    suspend fun getCowById(cowId: String): Cow?
    suspend fun getAllCows(): List<Cow>
    suspend fun addCow(cow: Cow): Cow
    suspend fun updateCow(cow: Cow): Boolean
    suspend fun deleteCow(cowId: String): Boolean
    suspend fun getEarningsSummary(date: LocalDate): Result<EarningsSummary>
    suspend fun getMilkAnalytics(date: LocalDate): Result<MilkAnalytics>

    /**
     * Archives a cow by marking it as inactive but preserves its data.
     * This is implemented using the updateCow method with modified fields.
     *
     * @param cowId The ID of the cow to archive
     * @param reason The reason for archiving (e.g. "SOLD", "DECEASED", etc.)
     * @param archiveDate The date when the cow was archived
     * @return true if archiving was successful, false otherwise
     */
    suspend fun archiveCow(cowId: String, reason: String, archiveDate: LocalDate): Boolean

    // New cow eligibility methods
    suspend fun getCowEligibility(cowId: String): Result<CowEligibilityResponse>
    suspend fun getBulkCowEligibility(
        ownerId: String? = null,
        activeOnly: Boolean = true
    ): Result<BulkEligibilityResponse>

    suspend fun getCowHealthDetails(cowId: String): Result<CowHealthDetailsResponse>
    suspend fun validateMilkCollection(
        cowId: String,
        date: LocalDate
    ): Result<CowEligibilityResponse>
}
