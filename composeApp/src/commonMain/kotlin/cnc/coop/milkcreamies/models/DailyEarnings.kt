package cnc.coop.milkcreamies.models

import kotlinx.datetime.LocalDate

/**
 * Data class for daily earnings information
 */
data class DailyEarnings(
    val date: LocalDate,
    val earnings: Double,
    val transactionCount: Int,
    val averagePerTransaction: Double
)

/**
 * Data class for monthly earnings comparison
 */
data class MonthlyEarnings(
    val month: LocalDate,
    val earnings: Double,
    val transactionCount: Int,
    val averagePerDay: Double
)

/**
 * Data class for customer earnings analysis
 */
data class CustomerEarnings(
    val customerId: String,
    val totalEarnings: Double,
    val transactionCount: Int,
    val lastTransactionDate: LocalDate?
)