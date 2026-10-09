package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LibraryRepository(
    private val bookDao: BookDao,
    private val memberDao: MemberDao,
    private val loanDao: LoanDao,
    private val categoryDao: CategoryDao
) {
    // Categories & Genres
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allCategoryNames: Flow<List<String>> = categoryDao.getAllCategoryNames()

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    suspend fun getAllCategoriesSync(): List<Category> = categoryDao.getAllCategoriesSync()

    // Books
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allGenres: Flow<List<String>> = bookDao.getAllGenres()
    val allRacks: Flow<List<String>> = bookDao.getAllRacks()
    val totalBooksCount: Flow<Int> = bookDao.getTotalBooksCount()
    val totalCopiesCount: Flow<Int> = bookDao.getTotalCopiesCount()
    val availableCopiesCount: Flow<Int> = bookDao.getAvailableCopiesCount()

    fun searchBooks(query: String, genre: String, rackId: String): Flow<List<Book>> =
        bookDao.searchBooks(query, genre, rackId)

    fun getBooksByRack(rackId: String): Flow<List<Book>> =
        bookDao.getBooksByRack(rackId)

    fun getBookById(id: Long): Flow<Book?> = bookDao.getBookById(id)

    suspend fun insertBook(book: Book): Long = bookDao.insertBook(book)

    suspend fun updateBook(book: Book) = bookDao.updateBook(book)

    suspend fun deleteBook(book: Book) = bookDao.deleteBook(book)

    // Members
    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()
    val totalMembersCount: Flow<Int> = memberDao.getTotalMembersCount()

    fun searchMembers(query: String): Flow<List<Member>> =
        memberDao.searchMembers(query)

    fun getMemberById(id: Long): Flow<Member?> = memberDao.getMemberById(id)

    suspend fun insertMember(member: Member): Long = memberDao.insertMember(member)

    suspend fun updateMember(member: Member) = memberDao.updateMember(member)

    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)

    // Loans
    val allLoansWithDetails: Flow<List<LoanWithDetails>> = loanDao.getAllLoansWithDetails()
    val activeLoansWithDetails: Flow<List<LoanWithDetails>> = loanDao.getActiveLoansWithDetails()
    val activeLoansCount: Flow<Int> = loanDao.getActiveLoansCount()

    fun getOverdueLoansWithDetails(currentTime: Long): Flow<List<LoanWithDetails>> =
        loanDao.getOverdueLoansWithDetails(currentTime)

    fun getOverdueLoansCount(currentTime: Long): Flow<Int> =
        loanDao.getOverdueLoansCount(currentTime)

    suspend fun getActiveLoansSync(): List<LoanWithDetails> =
        loanDao.getActiveLoansWithDetailsSync()

    suspend fun lendBook(
        bookId: Long,
        memberId: Long,
        dueDate: Long,
        notes: String = ""
    ): Result<Long> {
        val book = bookDao.getBookByIdSync(bookId) ?: return Result.failure(Exception("Book not found"))
        if (book.availableCopies <= 0) {
            return Result.failure(Exception("No available copies to lend for '${book.title}'"))
        }

        val rowsUpdated = bookDao.decrementAvailableCopies(bookId)
        if (rowsUpdated == 0) {
            return Result.failure(Exception("Could not update available copies"))
        }

        val loan = Loan(
            bookId = bookId,
            memberId = memberId,
            borrowDate = System.currentTimeMillis(),
            dueDate = dueDate,
            status = "ACTIVE",
            notes = notes
        )
        val loanId = loanDao.insertLoan(loan)
        memberDao.incrementActiveLoans(memberId)
        return Result.success(loanId)
    }

    suspend fun returnBook(loanId: Long): Result<Unit> {
        val loan = loanDao.getLoanById(loanId) ?: return Result.failure(Exception("Loan record not found"))
        if (loan.status == "RETURNED") {
            return Result.failure(Exception("Book already marked as returned"))
        }

        val updatedLoan = loan.copy(
            returnDate = System.currentTimeMillis(),
            status = "RETURNED"
        )
        loanDao.updateLoan(updatedLoan)
        bookDao.incrementAvailableCopies(loan.bookId)
        memberDao.decrementActiveLoans(loan.memberId)
        return Result.success(Unit)
    }

    suspend fun deleteLoan(loanId: Long) {
        val loan = loanDao.getLoanById(loanId) ?: return
        if (loan.status != "RETURNED") {
            bookDao.incrementAvailableCopies(loan.bookId)
            memberDao.decrementActiveLoans(loan.memberId)
        }
        loanDao.deleteLoan(loan)
    }
}
