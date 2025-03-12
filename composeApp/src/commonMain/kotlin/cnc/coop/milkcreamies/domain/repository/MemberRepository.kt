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

import cnc.coop.milkcreamies.models.Member
import kotlinx.datetime.LocalDate

interface MemberRepository {
    suspend fun getMemberById(memberId: String): Member?
    suspend fun getAllMembers(): List<Member>
    suspend fun addMember(member: Member): Member
    suspend fun updateMember(member: Member): Boolean

    /**
     * Archives a member by marking them as inactive but preserves their data.
     * This is implemented using the updateMember method with modified fields.
     *
     * @param memberId The ID of the member to archive
     * @param reason The reason for archiving
     * @param archiveDate The date when the member was archived
     * @return true if archiving was successful, false otherwise
     */
    suspend fun archiveMember(memberId: String, reason: String, archiveDate: LocalDate): Boolean
}
