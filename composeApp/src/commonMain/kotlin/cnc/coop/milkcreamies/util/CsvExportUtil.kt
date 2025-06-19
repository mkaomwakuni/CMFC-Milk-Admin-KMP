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
package cnc.coop.milkcreamies.util

import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.Customer
import cnc.coop.milkcreamies.models.HealthStatus
import cnc.coop.milkcreamies.models.Member
import cnc.coop.milkcreamies.models.MilkInEntry
import cnc.coop.milkcreamies.models.MilkOutEntry
import cnc.coop.milkcreamies.models.MilkSpoiltEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * Utility class for exporting data to CSV format
 */
object CsvExportUtil {

    fun exportCowsToCSV(cows: List<Cow>): String {
        val header =
            "ID,Name,Breed,Age,Weight,Owner ID,Health Status,Active,Entry Date,Archive Date,Archive Reason\n"
        val rows = cows.joinToString("\n") { cow ->
            "${cow.cowId ?: ""}," +
                    "\"${cow.name}\"," +
                    "\"${cow.breed}\"," +
                    "${cow.age}," +
                    "${cow.weight}," +
                    "\"${cow.ownerId}\"," +
                    "\"${cow.status.healthStatus.name}\"," +
                    "${cow.isActive}," +
                    "\"${cow.entryDate}\"," +
                    "\"${cow.archiveDate ?: ""}\"," +
                    "\"${cow.archiveReason ?: ""}\""
        }
        return header + rows
    }

    fun exportMembersToCSV(members: List<Member>): String {
        val header = "ID,Name,Active,Archive Date,Archive Reason\n"
        val rows = members.joinToString("\n") { member ->
            "\"${member.memberId ?: ""}\"," +
                    "\"${member.name}\"," +
                    "${member.isActive}," +
                    "\"${member.archiveDate ?: ""}\"," +
                    "\"${member.archiveReason ?: ""}\""
        }
        return header + rows
    }

    fun exportMilkInEntriesToCSV(entries: List<MilkInEntry>): String {
        val header = "Entry ID,Cow ID,Owner ID,Date,Quantity (L),Milking Type\n"
        val rows = entries.joinToString("\n") { entry ->
            "\"${entry.entryId ?: ""}\"," +
                    "\"${entry.cowId ?: ""}\"," +
                    "\"${entry.ownerId}\"," +
                    "\"${entry.date}\"," +
                    "${entry.liters}," +
                    "\"${entry.milkingType.name}\""
        }
        return header + rows
    }

    fun exportMilkOutEntriesToCSV(entries: List<MilkOutEntry>): String {
        val header =
            "Sale ID,Customer ID,Customer Name,Date,Quantity Sold (L),Price Per Liter,Total Amount,Payment Mode\n"
        val rows = entries.joinToString("\n") { entry ->
            "\"${entry.saleId ?: ""}\"," +
                    "\"${entry.customerId ?: ""}\"," +
                    "\"${entry.customerName}\"," +
                    "\"${entry.date}\"," +
                    "${entry.quantitySold}," +
                    "${entry.pricePerLiter}," +
                    "${entry.quantitySold * entry.pricePerLiter}," +
                    "\"${entry.paymentMode.name}\""
        }
        return header + rows
    }

    fun exportSpoiltMilkToCSV(entries: List<MilkSpoiltEntry>): String {
        val header = "Date,Amount Spoilt (L),Loss Amount (KES),Cause\n"
        val rows = entries.joinToString("\n") { entry ->
            "\"${entry.date}\"," +
                    "${entry.amountSpoilt}," +
                    "${entry.lossAmount}," +
                    "\"${entry.cause ?: "Unknown"}\""
        }
        return header + rows
    }

    fun exportComprehensiveReport(
        cows: List<Cow>,
        members: List<Member>,
        milkInEntries: List<MilkInEntry>,
        milkOutEntries: List<MilkOutEntry>,
        spoiltEntries: List<MilkSpoiltEntry>
    ): String {
        val date = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val report = StringBuilder()

        report.appendLine("CMFC MILK ADMIN - COMPREHENSIVE REPORT")
        report.appendLine("Generated on: $date")
        report.appendLine("=".repeat(50))
        report.appendLine()

        // Summary Statistics
        report.appendLine("SUMMARY STATISTICS")
        report.appendLine("-".repeat(20))
        report.appendLine("Total Cows: ${cows.size}")
        report.appendLine("Active Cows: ${cows.count { it.isActive }}")
        report.appendLine("Healthy Cows: ${cows.count { it.status.healthStatus == HealthStatus.HEALTHY }}")
        report.appendLine("Total Members: ${members.size}")
        report.appendLine("Active Members: ${members.count { it.isActive }}")
        report.appendLine("Total Milk In Entries: ${milkInEntries.size}")
        report.appendLine("Total Milk Out Entries: ${milkOutEntries.size}")
        report.appendLine("Total Spoilt Entries: ${spoiltEntries.size}")
        report.appendLine("Total Milk Produced: ${milkInEntries.sumOf { it.liters }} L")
        report.appendLine("Total Milk Sold: ${milkOutEntries.sumOf { it.quantitySold }} L")
        report.appendLine("Total Milk Spoilt: ${spoiltEntries.sumOf { it.amountSpoilt }} L")
        report.appendLine("Total Revenue: ${milkOutEntries.sumOf { it.quantitySold * it.pricePerLiter }} KES")
        report.appendLine("Total Losses: ${spoiltEntries.sumOf { it.lossAmount }} KES")
        report.appendLine()

        // Cows Data
        report.appendLine("COWS DATA")
        report.appendLine("-".repeat(15))
        report.appendLine(exportCowsToCSV(cows))
        report.appendLine()

        // Members Data
        report.appendLine("MEMBERS DATA")
        report.appendLine("-".repeat(15))
        report.appendLine(exportMembersToCSV(members))
        report.appendLine()

        // Milk In Data
        report.appendLine("MILK IN ENTRIES")
        report.appendLine("-".repeat(20))
        report.appendLine(exportMilkInEntriesToCSV(milkInEntries))
        report.appendLine()

        // Milk Out Data
        report.appendLine("MILK OUT ENTRIES")
        report.appendLine("-".repeat(20))
        report.appendLine(exportMilkOutEntriesToCSV(milkOutEntries))
        report.appendLine()

        // Spoilt Milk Data
        report.appendLine("SPOILT MILK ENTRIES")
        report.appendLine("-".repeat(25))
        report.appendLine(exportSpoiltMilkToCSV(spoiltEntries))

        return report.toString()
    }
    
    /**
     * Export all data to a comprehensive CSV report
     * 
     * @param milkInEntries List of milk in entries
     * @param milkOutEntries List of milk out entries
     * @param members List of cooperative members
     * @param cows List of registered cows
     * @param customers List of customers
     * @param spoiltEntries List of spoilt milk records
     * @return CSV formatted string containing all data
     */
    fun exportAllDataToCSV(
        milkInEntries: List<MilkInEntry>,
        milkOutEntries: List<MilkOutEntry>,
        members: List<Member>,
        cows: List<Cow>,
        customers: List<Customer>,
        spoiltEntries: List<MilkSpoiltEntry>
    ): String {
        val sb = StringBuilder()
        
        // Add report header
        sb.append("CMFC MILK ADMIN - COMPREHENSIVE REPORT\n")
        sb.append("Generated: ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}\n\n")
        
        // Add summary section
        sb.append("SUMMARY\n")
        sb.append("Total Members,${members.size}\n")
        sb.append("Total Cows,${cows.size}\n")
        sb.append("Total Customers,${customers.size}\n")
        val totalMilkIn = milkInEntries.sumOf { it.liters }
        val totalMilkOut = milkOutEntries.sumOf { it.quantitySold }
        val totalSpoilt = spoiltEntries.sumOf { it.amountSpoilt }
        sb.append("Total Milk In (L),${totalMilkIn}\n")
        sb.append("Total Milk Out (L),${totalMilkOut}\n")
        sb.append("Total Milk Spoilt (L),${totalSpoilt}\n")
        val totalEarnings = milkOutEntries.sumOf { it.quantitySold * it.pricePerLiter }
        sb.append("Total Earnings (KSh),${totalEarnings}\n\n")
        
        // Add milk in entries section
        sb.append("MILK IN ENTRIES\n")
        sb.append("Date,Cow ID,Cow Name,Member ID,Member Name,Quantity (L),Milking Type\n")
        milkInEntries.forEach { entry ->
            val cow = cows.find { it.cowId == entry.cowId }
            val member = members.find { it.memberId == cow?.ownerId }
            sb.append("${entry.date},")
            sb.append("${entry.cowId ?: "Unknown"},")
            sb.append("${cow?.name ?: "Unknown"},")
            sb.append("${cow?.ownerId ?: "Unknown"},")
            sb.append("${member?.name ?: "Unknown"},")
            sb.append("${entry.liters},")
            sb.append("${entry.milkingType}\n")
        }
        sb.append("\n")
        
        // Add milk out entries section
        sb.append("MILK SALES\n")
        sb.append("Date,Customer ID,Customer Name,Quantity (L),Price/L (KSh),Total (KSh),Payment Mode\n")
        milkOutEntries.forEach { entry ->
            val customer = customers.find { it.customerId == entry.customerId }
            sb.append("${entry.date},")
            sb.append("${entry.customerId ?: "N/A"},")
            sb.append("${customer?.name ?: entry.customerName},")
            sb.append("${entry.quantitySold},")
            sb.append("${entry.pricePerLiter},")
            sb.append("${entry.quantitySold * entry.pricePerLiter},")
            sb.append("${entry.paymentMode}\n")
        }
        sb.append("\n")
        
        // Add spoilt milk section
        if (spoiltEntries.isNotEmpty()) {
            sb.append("SPOILT MILK RECORDS\n")
            sb.append("Date,Quantity (L),Loss Amount (KSh),Cause\n")
            spoiltEntries.forEach { entry ->
                sb.append("${entry.date},")
                sb.append("${entry.amountSpoilt},")
                sb.append("${entry.lossAmount},")
                sb.append("${entry.cause ?: "Unknown"}\n")
            }
        } else {
            sb.append("SPOILT MILK RECORDS\n")
            sb.append("No spoilt milk records available\n")
        }
        
        return sb.toString()
    }
}
