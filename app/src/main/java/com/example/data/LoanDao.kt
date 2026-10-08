package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("""
        SELECT 
            l.id AS loanId,
            l.bookId AS bookId,
            l.memberId AS memberId,
            l.borrowDate AS borrowDate,
            l.dueDate AS dueDate,
            l.returnDate AS returnDate,
            l.status AS status,
            l.notes AS notes,
            b.title AS bookTitle,
            b.author AS bookAuthor,
            b.genre AS bookGenre,
            b.coverColorHex AS bookCoverColor,
            b.rackId AS rackId,
            b.shelfNumber AS shelfNumber,
            m.name AS memberName,
            m.phone AS memberPhone,
            m.email AS memberEmail,
            m.membershipCode AS memberCode
        FROM loans l
        JOIN books b ON l.bookId = b.id
        JOIN members m ON l.memberId = m.id
        ORDER BY l.borrowDate DESC
    """)
    fun getAllLoansWithDetails(): Flow<List<LoanWithDetails>>

    @Query("""
        SELECT 
            l.id AS loanId,
            l.bookId AS bookId,
            l.memberId AS memberId,
            l.borrowDate AS borrowDate,
            l.dueDate AS dueDate,
            l.returnDate AS returnDate,
            l.status AS status,
            l.notes AS notes,
            b.title AS bookTitle,
            b.author AS bookAuthor,
            b.genre AS bookGenre,
            b.coverColorHex AS bookCoverColor,
            b.rackId AS rackId,
            b.shelfNumber AS shelfNumber,
            m.name AS memberName,
            m.phone AS memberPhone,
            m.email AS memberEmail,
            m.membershipCode AS memberCode
        FROM loans l
        JOIN books b ON l.bookId = b.id
        JOIN members m ON l.memberId = m.id
        WHERE l.status != 'RETURNED'
        ORDER BY l.dueDate ASC
    """)
    fun getActiveLoansWithDetails(): Flow<List<LoanWithDetails>>

    @Query("""
        SELECT 
            l.id AS loanId,
            l.bookId AS bookId,
            l.memberId AS memberId,
            l.borrowDate AS borrowDate,
            l.dueDate AS dueDate,
            l.returnDate AS returnDate,
            l.status AS status,
            l.notes AS notes,
            b.title AS bookTitle,
            b.author AS bookAuthor,
            b.genre AS bookGenre,
            b.coverColorHex AS bookCoverColor,
            b.rackId AS rackId,
            b.shelfNumber AS shelfNumber,
            m.name AS memberName,
            m.phone AS memberPhone,
            m.email AS memberEmail,
            m.membershipCode AS memberCode
        FROM loans l
        JOIN books b ON l.bookId = b.id
        JOIN members m ON l.memberId = m.id
        WHERE l.status != 'RETURNED' AND l.dueDate < :currentTime
        ORDER BY l.dueDate ASC
    """)
    fun getOverdueLoansWithDetails(currentTime: Long): Flow<List<LoanWithDetails>>

    @Query("""
        SELECT 
            l.id AS loanId,
            l.bookId AS bookId,
            l.memberId AS memberId,
            l.borrowDate AS borrowDate,
            l.dueDate AS dueDate,
            l.returnDate AS returnDate,
            l.status AS status,
            l.notes AS notes,
            b.title AS bookTitle,
            b.author AS bookAuthor,
            b.genre AS bookGenre,
            b.coverColorHex AS bookCoverColor,
            b.rackId AS rackId,
            b.shelfNumber AS shelfNumber,
            m.name AS memberName,
            m.phone AS memberPhone,
            m.email AS memberEmail,
            m.membershipCode AS memberCode
        FROM loans l
        JOIN books b ON l.bookId = b.id
        JOIN members m ON l.memberId = m.id
        WHERE l.status != 'RETURNED'
        ORDER BY l.dueDate ASC
    """)
    suspend fun getActiveLoansWithDetailsSync(): List<LoanWithDetails>

    @Query("SELECT COUNT(*) FROM loans WHERE status != 'RETURNED'")
    fun getActiveLoansCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans WHERE status != 'RETURNED' AND dueDate < :currentTime")
    fun getOverdueLoansCount(currentTime: Long): Flow<Int>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    suspend fun getLoanById(loanId: Long): Loan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(loans: List<Loan>)

    @Update
    suspend fun updateLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)
}
