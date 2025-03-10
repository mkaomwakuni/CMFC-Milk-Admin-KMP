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
package cnc.coop.milkcreamies.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

// Updated Cow model - removed averageLitersPerDay, added archiving fields
@Serializable
data class Cow(
    val cowId: String? = null,
    val entryDate: LocalDate,
    val ownerId: String,
    val name: String,
    val breed: String,
    val age: Int,
    val weight: Double,
    val status: CowStatus,
    val isActive: Boolean = true, // New: Whether the cow is active or archived
    val archiveReason: String? = null, // New: Reason for archiving
    val archiveDate: LocalDate? = null, // New: Date when archived
    val note: String? = null
)

@Serializable
data class CowStatus(
    val healthStatus: HealthStatus,
    val actionStatus: ActionStatus,
    val dewormingDue: LocalDate? = null,
    val dewormingLast: LocalDate? = null,
    val calvingDate: LocalDate? = null,
    val vaccinationDue: LocalDate? = null,
    val vaccinationLast: LocalDate? = null,
    val antibioticTreatment: LocalDate? = null
)

@Serializable
enum class HealthStatus {
    HEALTHY,
    NEEDS_ATTENTION,
    UNDER_TREATMENT,  // Replaced SICK with UNDER_TREATMENT
    SICK,             // Added back for backward compatibility with existing database records
    GESTATION,        // Added for pregnant cows in gestation period
    VACCINATED,       // Recently vaccinated
    ANTIBIOTICS       // Under antibiotic treatment
}

@Serializable
enum class ActionStatus {
    ACTIVE, SOLD, WORMED, VACCINATED, DECEASED
}

@Serializable
enum class CowBreed(val displayName: String) {
    HOLSTEIN("Holstein"),
    JERSEY("Jersey"),
    GUERNSEY("Guernsey"),
    AYRSHIRE("Ayrshire"),
    BROWN_SWISS("Brown Swiss"),
    FRIESIAN("Friesian"),
    SAHIWAL("Sahiwal"),
    RED_SINDHI("Red Sindhi"),
    CROSSBRED("Crossbred"),
    OTHER("Other")
}

// Updated Member model - added archiving fields
@Serializable
data class Member(
    val memberId: String? = null,
    val name: String,
    val isActive: Boolean = true, // New: Whether the member is active or archived
    val archiveDate: LocalDate? = null, // New: Date when archived
    val archiveReason: String? = null // New: Reason for archiving
)

// New: Enhanced member with cow statistics
@Serializable
data class MemberWithStats(
    val member: Member,
    val cows: List<CowWithStats>,
    val averageDailyMilkProduction: Double // Based on recent milk entries
)

// New: Cow with calculated statistics
@Serializable
data class CowWithStats(
    val cow: Cow,
    val averageDailyMilkProduction: Double, // Calculated from morning + evening entries
    val lastMilkingDate: LocalDate? = null
)

@Serializable
data class Customer(
    val customerId: String? = null,
    val name: String
)

@Serializable
data class MilkInEntry(
    val entryId: String? = null,
    val cowId: String?,
    val ownerId: String,
    val liters: Double,
    val date: LocalDate,
    val milkingType: MilkingType
)

@Serializable
enum class MilkingType {
    MORNING, EVENING
}

@Serializable
data class MilkOutEntry(
    val saleId: String? = null,
    val customerId: String? = null,
    val customerName: String,
    val date: LocalDate,
    val quantitySold: Double,
    val pricePerLiter: Double,
    val paymentMode: PaymentMode
)

@Serializable
enum class PaymentMode {
    CASH, MPESA
}

// Updated MilkSpoiltEntry - added lossAmount field
@Serializable
data class MilkSpoiltEntry(
    val spoiltId: String? = null,
    val date: LocalDate,
    val amountSpoilt: Double,
    val lossAmount: Double, // New: Monetary loss in KES
    val cause: SpoilageCause? = null // New: Reason for spoilage
)

// New: Spoilage causes enum
@Serializable
enum class SpoilageCause {
    CONTAMINATION,
    TEMPERATURE,
    SOUR,
    IMPROPER_STORAGE,
    EQUIPMENT_FAILURE,
    EXPIRED,
    OTHER
}

// New: Real-time inventory tracking
@Serializable
data class MilkInventory(
    val currentStock: Double, // Current stock in liters
    val lastUpdated: LocalDate // Last update date
)

// Updated StockSummary - added currentStock and dailyTotalLitersSold
@Serializable
data class StockSummary(
    val currentStock: Double, // New: Real-time current stock in liters
    val dailyProduce: Double,
    val dailyTotalLitersSold: Double, // New: Total liters sold today (replaces cash sales)
    val weeklySold: Double,
    val weeklySpoilt: Double,
    val monthlySold: Double
)

@Serializable
data class EarningsSummary(
    val todayEarnings: Double,
    val weeklyEarnings: Double,
    val monthlyEarnings: Double
)

// Updated CowSummary - separated active/archived counts
@Serializable
data class CowSummary(
    val totalActiveCows: Int, // New: Total number of active cows
    val totalArchivedCows: Int, // Total number of archived cows
    val healthyCows: Int, // Number of active cows with healthStatus = HEALTHY
    val needsAttention: Int // Number of active cows with healthStatus = NEEDS_ATTENTION or SICK
)

// New: Member summary with active/archived filtering
@Serializable
data class MemberSummary(
    val totalActiveMembers: Int, // Total number of active members
    val totalArchivedMembers: Int, // Total number of archived members
    val membersWithActiveCows: Int // Active members who have active cows
)

// Updated DashboardMetrics - added currentStock
@Serializable
data class DashboardMetrics(
    val milkIn: Double = 0.0,
    val milkOut: Double = 0.0,
    val currentStock: Double = 0.0, // New: Real-time stock instead of generic stock
    val earnings: Double = 0.0
)

// Updated MilkSpoiltRequest - added lossAmount and cause
@Serializable
data class MilkSpoiltRequest(
    val date: LocalDate,
    val amountSpoilt: Double,
    val lossAmount: Double, // New: Monetary loss
    val cause: SpoilageCause? = null // New: Optional cause
)

@Serializable
data class MilkInRequest(
    val entryId: String? = null,
    val cowId: String,
    val ownerId: String,
    val liters: Double,
    val date: String,  // Must be in format "YYYY-MM-DD"
    val milkingType: String  // "MORNING" or "EVENING"
)

// New: Archive request models
@Serializable
data class ArchiveCowRequest(
    val cowId: String,
    val reason: String, // sold, deceased, off milking period
    val archiveDate: LocalDate
)

@Serializable
data class ArchiveMemberRequest(
    val memberId: String,
    val reason: String,
    val archiveDate: LocalDate
)

// Server error response models for milk collection validation
@Serializable
data class MilkCollectionError(
    val error: String,
    val cowId: String?,
    val cowName: String?,
    val healthStatus: String,
    val blockedUntil: String? = null,
    val suggestions: List<String> = emptyList()
)

// New server response models to match server domain models
@Serializable
data class MilkCollectionErrorResponse(
    val error: String,
    val cowId: String?,
    val cowName: String?,
    val healthStatus: String,
    val blockedUntil: String? = null,
    val suggestions: List<String> = emptyList()
)

@Serializable
data class CowEligibilityResponse(
    val cowId: String?,
    val cowName: String,
    val healthStatus: String,
    val isEligible: Boolean,
    val reason: String?,
    val blockedUntil: String?,
    val isActive: Boolean
)

@Serializable
data class CowHealthDetailsResponse(
    val cowId: String?,
    val name: String,
    val healthStatus: String,
    val vaccinationLast: String?,
    val vaccinationWaitingPeriodEnd: String?,
    val antibioticTreatment: String?,
    val antibioticWaitingPeriodEnd: String?,
    val canCollectMilk: Boolean,
    val blockedReason: String?
)

@Serializable
data class BulkEligibilityResponse(
    val cows: List<CowEligibilityResponse>,
    val totalCows: Int,
    val eligibleCows: Int,
    val blockedCows: Int
)

@Serializable
data class MilkCollectionValidationRequest(
    val cowId: String,
    val date: String // In YYYY-MM-DD format
)

@Serializable
data class CowHealthUpdateRequest(
    val cowId: String,
    val healthStatus: String,
    val vaccinationDate: String? = null,
    val antibioticTreatmentDate: String? = null,
    val note: String? = null
)

// Exception for handling milk collection validation errors
class MilkCollectionException(
    message: String,
    val cowId: String?,
    val cowName: String?,
    val healthStatus: String,
    val blockedUntil: String?,
    val suggestions: List<String>
) : Exception(message)

// Enhanced MilkAnalytics with new features
@Serializable
data class MilkAnalytics(
    val totalQuantity: Double = 0.0,
    val totalEntries: Int = 0,
    val uniqueCows: Int = 0,
    val avgQuantityPerEntry: Double = 0.0,
    val currentStock: Double = 0.0, // New: Current inventory level
    val dailyData: List<DailyProduction> = emptyList(),
    val cowData: List<CowProduction> = emptyList(),
    val memberData: List<MemberProduction> = emptyList() // New: Member production data
)

@Serializable
data class DailyProduction(
    val date: String,
    val quantity: Double,
    val entries: Int
)

@Serializable
data class CowProduction(
    val cowId: String,
    val cowName: String? = null, // New: Include cow name for better UX
    val quantity: Double,
    val entries: Int,
    val averageDaily: Double = 0.0 // New: Average daily production
)

// New: Member production analytics
@Serializable
data class MemberProduction(
    val memberId: String,
    val memberName: String,
    val totalQuantity: Double,
    val entries: Int,
    val activeCows: Int,
    val averageDaily: Double
)
