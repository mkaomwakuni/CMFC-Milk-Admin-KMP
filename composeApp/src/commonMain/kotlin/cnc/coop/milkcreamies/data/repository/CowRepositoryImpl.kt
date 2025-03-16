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
import cnc.coop.milkcreamies.domain.repository.CowRepository
import cnc.coop.milkcreamies.models.BulkEligibilityResponse
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.CowEligibilityResponse
import cnc.coop.milkcreamies.models.CowHealthDetailsResponse
import cnc.coop.milkcreamies.models.EarningsSummary
import cnc.coop.milkcreamies.models.MilkAnalytics
import kotlinx.datetime.LocalDate

class CowRepositoryImpl(
    private val milkClient: MilkClient
) : CowRepository {

    override suspend fun getCowById(cowId: String): Cow? {
        return try {
            milkClient.getCowById(cowId).getOrNull()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllCows(): List<Cow> {
        return try {
            milkClient.getCows().getOrNull() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addCow(cow: Cow): Cow {
        return try {
            val result = milkClient.addCow(cow)
            if (result.isSuccess) {
                result.getOrThrow()
            } else {
                throw Exception("Failed to save cow to server: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            throw Exception("Unable to add cow '${cow.name}': ${e.message}", e)
        }
    }

    override suspend fun updateCow(cow: Cow): Boolean {
        return try {
            val result = milkClient.updateCow(cow)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteCow(cowId: String): Boolean {
        return try {
            val result = milkClient.deleteCow(cowId)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getEarningsSummary(date: LocalDate): Result<EarningsSummary> {
        return milkClient.getEarningsSummary(date)
    }

    override suspend fun getMilkAnalytics(date: LocalDate): Result<MilkAnalytics> {
        return milkClient.getMilkAnalytics(date)
    }

    override suspend fun archiveCow(
        cowId: String,
        reason: String,
        archiveDate: LocalDate
    ): Boolean {
        return try {
            val result = milkClient.archiveCow(cowId, reason, archiveDate)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    // New cow eligibility methods implementation
    override suspend fun getCowEligibility(cowId: String): Result<CowEligibilityResponse> {
        return milkClient.getCowEligibility(cowId)
    }

    override suspend fun getBulkCowEligibility(
        ownerId: String?,
        activeOnly: Boolean
    ): Result<BulkEligibilityResponse> {
        return milkClient.getBulkCowEligibility(ownerId, activeOnly)
    }

    override suspend fun getCowHealthDetails(cowId: String): Result<CowHealthDetailsResponse> {
        return milkClient.getCowHealthDetails(cowId)
    }

    override suspend fun validateMilkCollection(
        cowId: String,
        date: LocalDate
    ): Result<CowEligibilityResponse> {
        return milkClient.validateMilkCollection(cowId, date)
    }
}
