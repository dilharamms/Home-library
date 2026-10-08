package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Long): Flow<Member?>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberByIdSync(id: Long): Member?

    @Query("""
        SELECT * FROM members 
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR membershipCode LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchMembers(query: String): Flow<List<Member>>

    @Query("SELECT COUNT(*) FROM members")
    fun getTotalMembersCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<Member>)

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("SELECT * FROM members ORDER BY id ASC")
    suspend fun getAllMembersSync(): List<Member>

    @Query("DELETE FROM members")
    suspend fun clearAllMembers()

    @Query("UPDATE members SET activeLoansCount = activeLoansCount + 1 WHERE id = :memberId")
    suspend fun incrementActiveLoans(memberId: Long)

    @Query("UPDATE members SET activeLoansCount = MAX(0, activeLoansCount - 1) WHERE id = :memberId")
    suspend fun decrementActiveLoans(memberId: Long)
}
