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
package cnc.coop.milkcreamies.presentation.viewmodel.members

import cnc.coop.milkcreamies.domain.repository.CowRepository
import cnc.coop.milkcreamies.domain.repository.MemberRepository
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * ViewModel for Members screen following MVVM best practices
 */
class MembersViewModel(
    private val memberRepository: MemberRepository,
    private val cowRepository: CowRepository,
    private val milkInEntryRepository: MilkInEntryRepository
) : BaseViewModel() {

    // Members state
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    // Member statistics
    private val _memberStats = MutableStateFlow<List<MemberStatistics>>(emptyList())
    val memberStats: StateFlow<List<MemberStatistics>> = _memberStats.asStateFlow()

    // Search and filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredMembers = MutableStateFlow<List<Member>>(emptyList())
    val filteredMembers: StateFlow<List<Member>> = _filteredMembers.asStateFlow()

    // Selected member for detailed view
    private val _selectedMember = MutableStateFlow<Member?>(null)
    val selectedMember: StateFlow<Member?> = _selectedMember.asStateFlow()

    // Member cows for detailed view
    private val _memberCows = MutableStateFlow<List<Cow>>(emptyList())
    val memberCows: StateFlow<List<Cow>> = _memberCows.asStateFlow()

    // Member milk entries for detailed view
    private val _memberMilkEntries = MutableStateFlow<List<MilkInEntry>>(emptyList())
    val memberMilkEntries: StateFlow<List<MilkInEntry>> = _memberMilkEntries.asStateFlow()

    init {
        loadMembersData()
    }

    fun loadMembersData() {
        executeWithLoading {
            loadMembers()
            calculateMemberStatistics()
            applyFilter()
        }
    }

    private suspend fun loadMembers() {
        val membersList = memberRepository.getAllMembers()
        if (membersList.isNotEmpty()) {
            _members.value = membersList
        } else {
            setError("No members found. Please add a member first.")
        }
    }

    private suspend fun calculateMemberStatistics() {
        val members = _members.value
        if (members.isEmpty()) return

        val allCows = cowRepository.getAllCows()
        val allMilkEntries = milkInEntryRepository.getAllMilkInEntries()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val weekAgo = today.minus(DatePeriod(days = 7))
        val monthAgo = today.minus(DatePeriod(months = 1))

        val statsData = members.map { member ->
            val memberCows =
                allCows.filter { cow -> member.memberId?.let { cow.ownerId == it } ?: false }
            val memberMilkEntries = allMilkEntries.filter { entry ->
                member.memberId?.let { entry.ownerId == it } ?: false
            }

            val todayEntries = memberMilkEntries.filter { it.date == today }
            val weeklyEntries = memberMilkEntries.filter { it.date >= weekAgo }
            val monthlyEntries = memberMilkEntries.filter { it.date >= monthAgo }

            MemberStatistics(
                member = member,
                totalCows = memberCows.size,
                healthyCows = memberCows.count { it.status.healthStatus == HealthStatus.HEALTHY },
                todayMilkProduction = todayEntries.sumOf { it.liters },
                weeklyMilkProduction = weeklyEntries.sumOf { it.liters },
                monthlyMilkProduction = monthlyEntries.sumOf { it.liters },
                averageDailyProduction = if (monthlyEntries.isNotEmpty()) monthlyEntries.sumOf { it.liters } / 30 else 0.0,
                lastEntryDate = memberMilkEntries.maxOfOrNull { it.date }
            )
        }

        _memberStats.value = statsData.sortedByDescending { it.monthlyMilkProduction }
    }

    fun addMember(name: String) {
        executeWithLoading {
            try {
                // Validation
                if (name.isBlank()) {
                    setError("Member name cannot be empty.")
                    return@executeWithLoading
                }

                if (_members.value.any { it.name.equals(name, ignoreCase = true) }) {
                    setError("A member with this name already exists.")
                    return@executeWithLoading
                }

                val newMember = Member(name = name.trim())
                memberRepository.addMember(newMember)
                loadMembersData() // Refresh member list and statistics
                clearError() // Clear any previous errors on success
            } catch (e: Exception) {
                setError("Failed to add member: ${e.message}")
            }
        }
    }

    fun searchMembers(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val query = _searchQuery.value.lowercase()
        _filteredMembers.value = if (query.isBlank()) {
            _members.value
        } else {
            _members.value.filter { member ->
                member.name.lowercase().contains(query) ||
                        (member.memberId?.lowercase()?.contains(query) == true)
            }
        }
    }
    

    fun getMemberPerformanceComparison(): List<MemberPerformanceData> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val lastMonth = today.minus(DatePeriod(months = 1))
        val twoMonthsAgo = today.minus(DatePeriod(months = 2))

        return _memberStats.value.map { stats ->
          
            val currentMonthProduction = stats.monthlyMilkProduction
            val previousMonthProduction = currentMonthProduction * 0.85 // Placeholder calculation
            val growthPercentage = if (previousMonthProduction > 0) {
                ((currentMonthProduction - previousMonthProduction) / previousMonthProduction) * 100
            } else 0.0

            MemberPerformanceData(
                member = stats.member,
                currentMonthProduction = currentMonthProduction,
                previousMonthProduction = previousMonthProduction,
                growthPercentage = growthPercentage,
                averagePerCow = if (stats.totalCows > 0) currentMonthProduction / stats.totalCows else 0.0
            )
        }.sortedByDescending { it.growthPercentage }
    }

    suspend fun loadMemberStats(memberId: String) {
        executeWithLoading {
            try {
                // Load selected member
                val member = memberRepository.getAllMembers().find { it.memberId == memberId }
                _selectedMember.value = member

                if (member != null) {
                    // Load member's cows
                    val allCows = cowRepository.getAllCows()
                    val memberCows = allCows.filter { it.ownerId == memberId }
                    _memberCows.value = memberCows

                    // Load member's milk entries
                    val allMilkEntries = milkInEntryRepository.getAllMilkInEntries()
                    val memberMilkEntries = allMilkEntries.filter { it.ownerId == memberId }
                    _memberMilkEntries.value = memberMilkEntries
                }
            } catch (e: Exception) {
                setError("Failed to load member stats: ${e.message}")
            }
        }
    }

    fun archiveMember(member: Member, reason: String) {
        executeWithLoading {
            try {
                val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

                // Use the repository archiveMember method
                val success = member.memberId?.let { memberId ->
                    memberRepository.archiveMember(memberId, reason, currentDate)
                } ?: false

                if (success) {
                    loadMembersData() // Refresh the list after update
                    clearError()
                } else {
                    setError("Failed to archive member: ${member.name}")
                }
            } catch (e: Exception) {
                setError("Error archiving member: ${e.message}")
            }
        }
    }

    /**
     * Data class for member statistics
     */
    data class MemberStatistics(
        val member: Member,
        val totalCows: Int,
        val healthyCows: Int,
        val todayMilkProduction: Double,
        val weeklyMilkProduction: Double,
        val monthlyMilkProduction: Double,
        val averageDailyProduction: Double,
        val lastEntryDate: LocalDate?
    )

    /**
     * Data class for member performance comparison
     */
    data class MemberPerformanceData(
        val member: Member,
        val currentMonthProduction: Double,
        val previousMonthProduction: Double,
        val growthPercentage: Double,
        val averagePerCow: Double
    )
}
