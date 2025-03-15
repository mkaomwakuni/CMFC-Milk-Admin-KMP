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

import cnc.coop.milkcreamies.domain.repository.MemberRepository
import cnc.coop.milkcreamies.models.Member
import cnc.coop.milkcreamies.data.remote.MilkClient
import kotlinx.datetime.LocalDate

class MemberRepositoryImpl(
    private val milkClient: MilkClient
) : MemberRepository {

    override suspend fun getMemberById(memberId: String): Member? {
        return try {
            milkClient.getMemberById(memberId).getOrNull()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllMembers(): List<Member> {
        return try {
            milkClient.getMembers().getOrNull() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addMember(member: Member): Member {
        return try {
            val result = milkClient.addMember(member)
            if (result.isSuccess) {
                result.getOrThrow()
            } else {
                // If server call fails, we could optionally store locally
                // For now, rethrow with more context
                throw Exception("Failed to save member to server: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            // Add more context to the error
            throw Exception("Unable to add member '${member.name}': ${e.message}", e)
        }
    }

    override suspend fun updateMember(member: Member): Boolean {
        return try {
            val result = milkClient.updateMember(member)
            if (result.isFailure) {
                return false
            }
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun archiveMember(
        memberId: String,
        reason: String,
        archiveDate: LocalDate
    ): Boolean {
        return try {
            val result = milkClient.archiveMember(memberId, reason, archiveDate)
            if (result.isSuccess) {
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
