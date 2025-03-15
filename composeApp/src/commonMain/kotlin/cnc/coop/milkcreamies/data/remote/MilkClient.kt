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
package cnc.coop.milkcreamies.data.remote

import cnc.coop.milkcreamies.core.constants.AppConstants
import cnc.coop.milkcreamies.data.*
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.CowSummary
import cnc.coop.milkcreamies.models.Customer
import cnc.coop.milkcreamies.models.EarningsSummary
import cnc.coop.milkcreamies.models.Member
import cnc.coop.milkcreamies.models.MilkAnalytics
import cnc.coop.milkcreamies.models.MilkInEntry
import cnc.coop.milkcreamies.models.MilkInRequest
import cnc.coop.milkcreamies.models.MilkOutEntry
import cnc.coop.milkcreamies.models.MilkSpoiltEntry
import cnc.coop.milkcreamies.models.MilkingType
import cnc.coop.milkcreamies.models.StockSummary
import cnc.coop.milkcreamies.models.ArchiveCowRequest
import cnc.coop.milkcreamies.models.ArchiveMemberRequest
import cnc.coop.milkcreamies.models.MilkCollectionError
import cnc.coop.milkcreamies.models.MilkCollectionException
import cnc.coop.milkcreamies.models.MilkCollectionErrorResponse
import cnc.coop.milkcreamies.models.CowEligibilityResponse
import cnc.coop.milkcreamies.models.BulkEligibilityResponse
import cnc.coop.milkcreamies.models.CowHealthDetailsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

@Serializable
private data class MemberRequest(val name: String)

@Serializable
private data class CustomerRequest(val name: String)

/**
 * Request DTO for milk-in entry.
 * The entryId field should be explicitly set to null for new entries as the server auto-generates it.
 */
@Serializable
private data class MilkInEntryRequest(
    val entryId: String? = null, // This should be null for new entries
    val cowId: String? = null, // Can be null, but if provided must be valid
    val ownerId: String, // This is required
    val liters: Double, // Must be > 0
    val date: String, // Must be in format "YYYY-MM-DD"
    val milkingType: String // "MORNING" or "EVENING"
)

/**
 * Request DTO for milk-out entry.
 * Includes the ID fields as null to satisfy the server deserialization requirements.
 */
@Serializable
private data class MilkOutEntryRequest(
    val saleId: String? = null,  // Include as null to satisfy the server deserializer
    val customerId: String? = null,  // Include as null to satisfy the server deserializer
    val customerName: String,
    val date: String,
    val quantitySold: Double,
    val pricePerLiter: Double,
    val paymentMode: String
)

@Serializable
private data class CowRequest(
    val cowId: String? = null,  // Include as null to satisfy the server deserializer
    val name: String,
    val breed: String,
    val age: Int,
    val weight: Double,
    val entryDate: String,
    val ownerId: String,
    val status: CowStatusRequest,
    val note: String? = null
)

@Serializable
private data class CowStatusRequest(
    val healthStatus: String,
    val actionStatus: String,
    val dewormingDue: String? = null,
    val dewormingLast: String? = null,
    val calvingDate: String? = null,
    val vaccinationDue: String? = null,
    val vaccinationLast: String? = null,
    val antibioticTreatment: String? = null
)

/**
 * Request DTO for milk spoilt entry.
 * The spoiltId field should be explicitly set to null for new entries as the server auto-generates it.
 */
@Serializable
private data class MilkSpoiltEntryRequest(
    val date: String, // Date in YYYY-MM-DD format
    val amountSpoilt: Double, // Liters spoilt
    val lossAmount: Double, // Monetary loss in KES
    val cause: String? = null // Optional spoilage cause
)

class MilkClient {
    private val baseUrl = AppConstants.Api.BASE_URL
    private val apiKey = AppConstants.Api.API_KEY

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = AppConstants.Api.TIMEOUT_SECONDS * 1000
        }
        defaultRequest {
            header("X-API-Key", apiKey)
            contentType(ContentType.Application.Json)
        }
        expectSuccess = false // Allow handling of non-2xx responses
    }

    // Cache for frequently accessed data
    private val cowCache = mutableMapOf<String, CachedItem<Cow>>()
    private val memberCache = mutableMapOf<String, CachedItem<Member>>()
    
    // Cache expiry time (10 minutes)
    private val cacheExpiryMs = 10 * 60 * 1000L

    // Cow operations
    suspend fun getCows(): Result<List<Cow>> = executeRequest {
        client.get("$baseUrl/cows").body()
    }

    suspend fun getCowById(cowId: String): Result<Cow?> = executeRequest {
        // Check cache first
        cowCache[cowId]?.let {
            if (!it.isExpired()) {
                return@executeRequest it.data
            }
        }
        
        // Cache miss, fetch from network
        val cow = client.get("$baseUrl/cows/$cowId").body<Cow>()
        
        // Update cache
        if (cow != null) {
            cowCache[cowId] =
                CachedItem(cow, Clock.System.now().toEpochMilliseconds() + cacheExpiryMs)
        }
        
        cow
    }

    suspend fun addCow(cow: Cow): Result<Cow> = executeRequest {
        // Convert to a simplified request format the server expects
        val cowRequest = CowRequest(
            cowId = null,  // Explicitly set to null - server will generate the ID
            name = cow.name,
            breed = cow.breed,
            age = cow.age,
            weight = cow.weight,
            entryDate = cow.entryDate.toString(),
            ownerId = cow.ownerId,
            status = CowStatusRequest(
                healthStatus = cow.status.healthStatus.toString(),
                actionStatus = cow.status.actionStatus.toString(),
                dewormingDue = cow.status.dewormingDue?.toString(),
                dewormingLast = cow.status.dewormingLast?.toString(),
                calvingDate = cow.status.calvingDate?.toString(),
                vaccinationDue = cow.status.vaccinationDue?.toString(),
                vaccinationLast = cow.status.vaccinationLast?.toString(),
                antibioticTreatment = cow.status.antibioticTreatment?.toString()
            ),
            note = cow.note
        )

        val response = client.post("$baseUrl/cows") {
            setBody(cowRequest)
        }

        when {
            response.status.isSuccess() -> {
                try {
                    response.body<Cow>()
                } catch (e: Exception) {
                    // Fallback: If response parsing fails, return the original cow with a generated ID
                    cow.copy(cowId = "CW${(Random.nextInt(1, 100)).toString().padStart(2, '0')}")
                }
            }

            else -> {
                val errorBody = response.bodyAsText()
                throw Exception("Failed to add cow: ${response.status} - $errorBody")
            }
        }
    }

    suspend fun updateCow(cow: Cow): Result<Cow> = executeRequest {
        val cowRequest = CowRequest(
            name = cow.name,
            breed = cow.breed,
            age = cow.age,
            weight = cow.weight,
            entryDate = cow.entryDate.toString(),
            ownerId = cow.ownerId,
            status = CowStatusRequest(
                healthStatus = cow.status.healthStatus.toString(),
                actionStatus = cow.status.actionStatus.toString(),
                dewormingDue = cow.status.dewormingDue?.toString(),
                dewormingLast = cow.status.dewormingLast?.toString(),
                calvingDate = cow.status.calvingDate?.toString(),
                vaccinationDue = cow.status.vaccinationDue?.toString(),
                vaccinationLast = cow.status.vaccinationLast?.toString(),
                antibioticTreatment = cow.status.antibioticTreatment?.toString()
            ),
            note = cow.note
        )

        client.put("$baseUrl/cows/${cow.cowId}") {
            setBody(cowRequest)
        }.body()
    }

    suspend fun deleteCow(cowId: String): Result<Unit> = executeRequest {
        client.delete("$baseUrl/cows/$cowId")
        Unit
    }

    // MilkInEntry operations
    suspend fun getMilkInEntries(): Result<List<MilkInEntry>> = executeRequest {
        client.get("$baseUrl/milk-in").body()
    }

    suspend fun addMilkInEntry(entry: MilkInRequest): Result<MilkInEntry> = executeRequest {
        val response = client.post("$baseUrl/milk-in") {
            // Make sure we're explicitly passing null for entryId
            setBody(entry.copy(entryId = null))
        }

        when {
            response.status.isSuccess() -> {
                try {
                    response.body<MilkInEntry>()
                } catch (e: Exception) {
                    // Fallback: Return the original entry with a generated ID
                    MilkInEntry(
                        entryId = "ETR${(Random.nextInt(1, 1000)).toString().padStart(3, '0')}",
                        cowId = entry.cowId,
                        ownerId = entry.ownerId,
                        liters = entry.liters,
                        date = LocalDate.parse(entry.date),
                        milkingType = MilkingType.valueOf(entry.milkingType)
                    )
                }
            }

            response.status == HttpStatusCode.BadRequest -> {
                val errorBody = response.bodyAsText()

                // Try to parse as MilkCollectionErrorResponse (new server format)
                try {
                    val milkError = Json.decodeFromString<MilkCollectionErrorResponse>(errorBody)
                    throw MilkCollectionException(
                        message = milkError.error,
                        cowId = milkError.cowId,
                        cowName = milkError.cowName,
                        healthStatus = milkError.healthStatus,
                        blockedUntil = milkError.blockedUntil,
                        suggestions = milkError.suggestions
                    )
                } catch (parseException: Exception) {
                    // Fallback: Try the old MilkCollectionError format
                    try {
                        val milkError = Json.decodeFromString<MilkCollectionError>(errorBody)
                        throw MilkCollectionException(
                            message = milkError.error,
                            cowId = milkError.cowId,
                            cowName = milkError.cowName,
                            healthStatus = milkError.healthStatus,
                            blockedUntil = milkError.blockedUntil,
                            suggestions = milkError.suggestions
                        )
                    } catch (parseException2: Exception) {
                        // If it's neither format, it's a general bad request
                        throw Exception("Bad request: $errorBody")
                    }
                }
            }

            else -> {
                val errorBody = response.bodyAsText()
                throw Exception("Failed to add milk in entry: ${response.status} - $errorBody")
            }
        }
    }

    // MilkOutEntry operations
    suspend fun getMilkOutEntries(): Result<List<MilkOutEntry>> = executeRequest {
        client.get("$baseUrl/milk-out").body()
    }

    suspend fun addMilkOutEntry(entry: MilkOutEntry): Result<MilkOutEntry> = executeRequest {
        // Create a request with explicit null IDs - server requires these fields in the JSON
        val entryRequest = MilkOutEntryRequest(
            saleId = null,  // Explicitly set to null - server will generate the ID
            customerId = null,  // Explicitly set to null - server will generate based on name
            customerName = entry.customerName,
            date = entry.date.toString(),
            quantitySold = entry.quantitySold,
            pricePerLiter = entry.pricePerLiter,
            paymentMode = entry.paymentMode.toString()
        )

        val response = client.post("$baseUrl/milk-out") {
            setBody(entryRequest)
        }

        when {
            response.status.isSuccess() -> {
                try {
                    response.body<MilkOutEntry>()
                } catch (e: Exception) {
                    // Fallback: If response parsing fails, return the original entry with generated IDs
                    entry.copy(
                        saleId = "SL${(Random.nextInt(1, 100)).toString().padStart(2, '0')}",
                        customerId = "CS_${entry.customerName.replace(" ", "_").uppercase()}"
                    )
                }
            }

            else -> {
                val errorBody = response.bodyAsText()
                throw Exception("Failed to add milk out entry: ${response.status} - $errorBody")
            }
        }
    }

    // MilkSpoiltEntry operations
    suspend fun getMilkSpoiltEntries(): Result<List<MilkSpoiltEntry>> = executeRequest {
        client.get("$baseUrl/milk-spoilt").body()
    }

    suspend fun getMilkSpoiltEntryById(spoiltId: String): Result<MilkSpoiltEntry?> =
        executeRequest {
            client.get("$baseUrl/milk-spoilt/$spoiltId").body()
        }

    suspend fun addMilkSpoiltEntry(entry: MilkSpoiltEntry): Result<MilkSpoiltEntry> =
        executeRequest {
            val spoiltRequest = MilkSpoiltEntryRequest(
                date = entry.date.toString(),
                amountSpoilt = entry.amountSpoilt,
                lossAmount = entry.lossAmount,
                cause = entry.cause?.toString()
            )

            val response = client.post("$baseUrl/milk-spoilt") {
                setBody(spoiltRequest)
            }

            when {
                response.status.isSuccess() -> {
                    try {
                        response.body<MilkSpoiltEntry>()
                    } catch (e: Exception) {
                        // Fallback: Return the original entry with generated ID
                        entry.copy(
                            spoiltId = "SPL${(Random.nextInt(1, 1000)).toString().padStart(3, '0')}"
                        )
                    }
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    throw Exception("Failed to add milk spoilt entry: ${response.status} - $errorBody")
                }
            }
        }

    suspend fun deleteMilkSpoiltEntry(spoiltId: String): Result<Unit> = executeRequest {
        client.delete("$baseUrl/milk-spoilt/$spoiltId")
        Unit
    }

    // Customer operations
    suspend fun getCustomers(): Result<List<Customer>> = executeRequest {
        client.get("$baseUrl/customers").body()
    }

    suspend fun getCustomerById(customerId: String): Result<Customer?> = executeRequest {
        client.get("$baseUrl/customers/$customerId").body()
    }

    suspend fun addCustomer(customer: Customer): Result<Customer> = executeRequest {
        // Send only the name field as the server expects
        val customerRequest = CustomerRequest(customer.name)

        val response = client.post("$baseUrl/customers") {
            setBody(customerRequest)
        }

        when {
            response.status.isSuccess() -> {
                try {
                    // Try to parse the response as Customer
                    val createdCustomer = response.body<Customer>()
                    createdCustomer
                } catch (e: Exception) {
                    // Customer IDs are auto-generated by the server based on the name
                    // This is just a fallback that wouldn't normally be used
                    customer.copy(customerId = "CS_${customer.name.replace(" ", "_").uppercase()}")
                }
            }

            else -> {
                val errorBody = response.bodyAsText()
                throw Exception("Failed to add customer: ${response.status} - $errorBody")
            }
        }
    }

    // Member operations
    suspend fun getMembers(): Result<List<Member>> = executeRequest {
        client.get("$baseUrl/members").body()
    }

    suspend fun getMemberById(memberId: String): Result<Member?> = executeRequest {
        // Check cache first
        memberCache[memberId]?.let {
            if (!it.isExpired()) {
                return@executeRequest it.data
            }
        }
        
        // Cache miss, fetch from network
        val member = client.get("$baseUrl/members/$memberId").body<Member>()
        
        // Update cache
        if (member != null) {
            memberCache[memberId] =
                CachedItem(member, Clock.System.now().toEpochMilliseconds() + cacheExpiryMs)
        }
        
        member
    }

    suspend fun addMember(member: Member): Result<Member> = executeRequest {
        // Send only the name field as the server expects
        val memberRequest = MemberRequest(member.name)

        val response = client.post("$baseUrl/members") {
            setBody(memberRequest)
        }

        when {
            response.status.isSuccess() -> {
                try {
                    // Try to parse the response as Member
                    val createdMember = response.body<Member>()
                    createdMember
                } catch (e: Exception) {
                    // Member IDs must be valid UUIDs - we shouldn't generate these client-side
                    // This is just a fallback for testing that wouldn't normally be used
                    member.copy(memberId = "00000000-0000-0000-0000-000000000000")
                }
            }

            else -> {
                val errorBody = response.bodyAsText()
                throw Exception("Failed to add member: ${response.status} - $errorBody")
            }
        }
    }

    suspend fun updateMember(member: Member): Result<Member> = executeRequest {
        val memberRequest = MemberRequest(member.name)

        client.put("$baseUrl/members/${member.memberId}") {
            setBody(memberRequest)
        }.body()
    }

    // Archive operations
    suspend fun archiveCow(cowId: String, reason: String, archiveDate: LocalDate): Result<Cow> =
        executeRequest {
            val archiveRequest = ArchiveCowRequest(
                cowId = cowId,
                reason = reason,
                archiveDate = archiveDate
            )

            // Use a dedicated endpoint for archiving
            val response = client.post("$baseUrl/cows/$cowId/archive") {
                setBody(archiveRequest)
            }

            when {
                response.status.isSuccess() -> {
                    try {
                        response.body<Cow>()
                    } catch (e: Exception) {
                        throw Exception("Failed to parse archive response")
                    }
                }

                else -> {
                    val errorBody = response.bodyAsText()
                    throw Exception("Failed to archive cow: ${response.status} - $errorBody")
                }
            }
        }

    suspend fun archiveMember(
        memberId: String,
        reason: String,
        archiveDate: LocalDate
    ): Result<Member> = executeRequest {
        val archiveRequest = ArchiveMemberRequest(
            memberId = memberId,
            reason = reason,
            archiveDate = archiveDate
        )

        // Use a dedicated endpoint for archiving
        val response = client.post("$baseUrl/members/$memberId/archive") {
            setBody(archiveRequest)
        }

        when {
            response.status.isSuccess() -> {
                try {
                    response.body<Member>()
                } catch (e: Exception) {
                    throw Exception("Failed to parse archive response")
                }
            }

            else -> {
                val errorBody = response.bodyAsText()
                throw Exception("Failed to archive member: ${response.status} - $errorBody")
            }
        }
    }

    // Summary operations
    suspend fun getStockSummary(date: LocalDate): Result<StockSummary> = executeRequest {
        client.get("$baseUrl/stock-summary") {
            parameter("date", date.toString())
        }.body()
    }

    suspend fun getEarningsSummary(date: LocalDate): Result<EarningsSummary> = executeRequest {
        client.get("$baseUrl/earnings-summary") {
            parameter("date", date.toString())
        }.body()
    }

    suspend fun getCowSummary(): Result<CowSummary> = executeRequest {
        client.get("$baseUrl/cow-summary").body()
    }

    suspend fun getMilkAnalytics(date: LocalDate): Result<MilkAnalytics> = executeRequest {
        client.get("$baseUrl/milk-analytics") {
            parameter("date", date.toString())
        }.body()
    }

    suspend fun getCowEligibility(cowId: String): Result<CowEligibilityResponse> = executeRequest {
        try {
            val response = client.get("$baseUrl/cows/$cowId/milk-eligibility")
            val body = response.body<CowEligibilityResponse>()
            body
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getBulkCowEligibility(
        ownerId: String? = null,
        activeOnly: Boolean = true
    ): Result<BulkEligibilityResponse> = executeRequest {
        client.get("$baseUrl/cows/milk-eligibility") {
            ownerId?.let { parameter("ownerId", it) }
            parameter("activeOnly", activeOnly.toString())
        }.body()
    }

    suspend fun getCowHealthDetails(cowId: String): Result<CowHealthDetailsResponse> =
        executeRequest {
            client.get("$baseUrl/cows/$cowId/health-details").body()
        }

    suspend fun validateMilkCollection(
        cowId: String,
        date: LocalDate
    ): Result<CowEligibilityResponse> = executeRequest {
        client.post("$baseUrl/cows/milk-validation") {
            setBody(
                cnc.coop.milkcreamies.models.MilkCollectionValidationRequest(
                    cowId = cowId,
                    date = date.toString()
                )
            )
        }.body()
    }

    // Helper function for request execution and error handling
    private suspend inline fun <T> executeRequest(crossinline block: suspend () -> T): Result<T> {
        return try {
            val response = block()
            Result.success(response)
        } catch (e: ClientRequestException) {
            val errorBody = try {
                e.response.bodyAsText()
            } catch (ex: Exception) {
                "Unable to read response body: ${ex.message}"
            }

            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(Exception("Unauthorized: Invalid API key"))
                HttpStatusCode.BadRequest -> Result.failure(Exception("Bad request: $errorBody"))
                HttpStatusCode.NotFound -> Result.failure(Exception("Not found: $errorBody"))
                else -> Result.failure(Exception("Client error: ${e.response.status} - $errorBody"))
            }
        } catch (e: ServerResponseException) {
            val errorBody = try {
                e.response.bodyAsText()
            } catch (ex: Exception) {
                "Unable to read response body: ${ex.message}"
            }

            Result.failure(Exception("Server error: ${e.response.status} - $errorBody"))
        } catch (e: CancellationException) {
            throw e // Propagate cancellation
        } catch (e: Exception) {
            Result.failure(Exception("Network or unexpected error: ${e.message}", e))
        }
    }

    // Close client when done
    fun close() {
        client.close()
    }

    /**
     * Clear all caches
     */
    fun clearCaches() {
        cowCache.clear()
        memberCache.clear()
    }
    
    /**
     * Cached item wrapper with expiry time
     */
    private data class CachedItem<T>(
        val data: T,
        val expiryTime: Long
    ) {
        fun isExpired() = Clock.System.now().toEpochMilliseconds() > expiryTime
    }
}
