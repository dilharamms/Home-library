package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Book
import com.example.data.LibraryDatabase
import com.example.data.LibraryRepository
import com.example.data.LoanWithDetails
import com.example.data.Member
import com.example.util.AlarmScheduler
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab(val title: String) {
    DASHBOARD("Dashboard"),
    BOOKS("Books"),
    LOANS("Lending"),
    MEMBERS("Members"),
    RACK_LOCATOR("Find Rack")
}

enum class AvailabilityFilter {
    ALL,
    AVAILABLE_ONLY,
    LENT_OUT_ONLY
}

enum class ThemeMode(val title: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class DashboardStats(
    val totalBooks: Int = 0,
    val totalCopies: Int = 0,
    val availableCopies: Int = 0,
    val lentCopies: Int = 0,
    val totalMembers: Int = 0,
    val activeLoans: Int = 0,
    val overdueLoans: Int = 0,
    val dueSoonLoans: Int = 0
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = LibraryDatabase.getDatabase(application, viewModelScope)
    private val repository = LibraryRepository(
        database.bookDao(),
        database.memberDao(),
        database.loanDao()
    )

    // Navigation Tab
    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    // Theme Mode Preference
    private val prefs = application.getSharedPreferences("home_library_prefs", android.content.Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("pref_theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("pref_theme_mode", mode.name).apply()
    }

    // Search and Filters for Books
    private val _bookSearchQuery = MutableStateFlow("")
    val bookSearchQuery: StateFlow<String> = _bookSearchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _selectedRack = MutableStateFlow("")
    val selectedRack: StateFlow<String> = _selectedRack.asStateFlow()

    private val _availabilityFilter = MutableStateFlow(AvailabilityFilter.ALL)
    val availabilityFilter: StateFlow<AvailabilityFilter> = _availabilityFilter.asStateFlow()

    // Member Search
    private val _memberSearchQuery = MutableStateFlow("")
    val memberSearchQuery: StateFlow<String> = _memberSearchQuery.asStateFlow()

    // Snackbars / feedback
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Filtered Books Flow
    val filteredBooks: StateFlow<List<Book>> = combine(
        repository.allBooks,
        _bookSearchQuery,
        _selectedGenre,
        _selectedRack,
        _availabilityFilter
    ) { allBooks, query, genre, rack, availability ->
        allBooks.filter { book ->
            val matchesQuery = query.isBlank() ||
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.isbn.contains(query, ignoreCase = true) ||
                    book.shelfNumber.contains(query, ignoreCase = true)
            val matchesGenre = genre.isBlank() || book.genre.equals(genre, ignoreCase = true)
            val matchesRack = rack.isBlank() || book.rackId.equals(rack, ignoreCase = true)
            val matchesAvailability = when (availability) {
                AvailabilityFilter.ALL -> true
                AvailabilityFilter.AVAILABLE_ONLY -> book.availableCopies > 0
                AvailabilityFilter.LENT_OUT_ONLY -> book.availableCopies < book.totalCopies
            }
            matchesQuery && matchesGenre && matchesRack && matchesAvailability
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Genres and Racks
    val genres: StateFlow<List<String>> = repository.allGenres
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val racks: StateFlow<List<String>> = repository.allRacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Members Flow
    val filteredMembers: StateFlow<List<Member>> = combine(
        repository.allMembers,
        _memberSearchQuery
    ) { members, query ->
        if (query.isBlank()) members
        else {
            members.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.membershipCode.contains(query, ignoreCase = true) ||
                        it.phone.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loans
    val allLoans: StateFlow<List<LoanWithDetails>> = repository.allLoansWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLoans: StateFlow<List<LoanWithDetails>> = repository.activeLoansWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueLoans: StateFlow<List<LoanWithDetails>> = repository.allLoansWithDetails
        .combine(MutableStateFlow(System.currentTimeMillis())) { loans, now ->
            loans.filter { it.status != "RETURNED" && it.dueDate < now }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueSoonLoans: StateFlow<List<LoanWithDetails>> = repository.allLoansWithDetails
        .combine(MutableStateFlow(System.currentTimeMillis())) { loans, now ->
            loans.filter { it.status != "RETURNED" && DateUtils.isDueSoon(it.dueDate, 2) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics Flow
    val dashboardStats: StateFlow<DashboardStats> = combine(
        repository.allBooks,
        repository.allMembers,
        repository.allLoansWithDetails
    ) { books, members, loans ->
        val now = System.currentTimeMillis()
        val totalBooks = books.size
        val totalCopies = books.sumOf { it.totalCopies }
        val availableCopies = books.sumOf { it.availableCopies }
        val lentCopies = totalCopies - availableCopies
        val totalMembers = members.size
        val activeLoans = loans.count { it.status != "RETURNED" }
        val overdueLoans = loans.count { it.status != "RETURNED" && it.dueDate < now }
        val dueSoonLoans = loans.count { it.status != "RETURNED" && DateUtils.isDueSoon(it.dueDate, 2) }

        DashboardStats(
            totalBooks = totalBooks,
            totalCopies = totalCopies,
            availableCopies = availableCopies,
            lentCopies = lentCopies,
            totalMembers = totalMembers,
            activeLoans = activeLoans,
            overdueLoans = overdueLoans,
            dueSoonLoans = dueSoonLoans
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        // Schedule periodic background check
        AlarmScheduler.schedulePeriodicCheck(application)
    }

    // Filter mutations
    fun updateBookSearchQuery(query: String) {
        _bookSearchQuery.value = query
    }

    fun selectGenre(genre: String) {
        _selectedGenre.value = if (_selectedGenre.value == genre) "" else genre
    }

    fun selectRack(rack: String) {
        _selectedRack.value = if (_selectedRack.value == rack) "" else rack
    }

    fun setAvailabilityFilter(filter: AvailabilityFilter) {
        _availabilityFilter.value = filter
    }

    fun updateMemberSearchQuery(query: String) {
        _memberSearchQuery.value = query
    }

    // Book Actions
    fun saveBook(book: Book) {
        viewModelScope.launch {
            if (book.id == 0L) {
                repository.insertBook(book)
                _userMessage.emit("Added '${book.title}' to stock on ${book.rackId} (${book.shelfNumber})")
            } else {
                repository.updateBook(book)
                _userMessage.emit("Updated '${book.title}'")
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            repository.deleteBook(book)
            _userMessage.emit("Removed '${book.title}' from library")
        }
    }

    // Member Actions
    fun registerMember(member: Member) {
        viewModelScope.launch {
            if (member.id == 0L) {
                repository.insertMember(member)
                _userMessage.emit("Registered new member: ${member.name} (${member.membershipCode})")
            } else {
                repository.updateMember(member)
                _userMessage.emit("Updated details for ${member.name}")
            }
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
            _userMessage.emit("Member ${member.name} removed")
        }
    }

    // Loan Actions
    fun lendBook(bookId: Long, memberId: Long, dueDate: Long, notes: String = "") {
        viewModelScope.launch {
            val result = repository.lendBook(bookId, memberId, dueDate, notes)
            result.onSuccess {
                _userMessage.emit("Book lent successfully! Return due on ${DateUtils.formatDate(dueDate)}")
            }.onFailure { error ->
                _userMessage.emit("Lending failed: ${error.message}")
            }
        }
    }

    fun returnBook(loanId: Long, bookTitle: String) {
        viewModelScope.launch {
            val result = repository.returnBook(loanId)
            result.onSuccess {
                _userMessage.emit("Returned '$bookTitle' successfully! Available copies updated.")
            }.onFailure { error ->
                _userMessage.emit("Return failed: ${error.message}")
            }
        }
    }

    fun deleteLoan(loanId: Long) {
        viewModelScope.launch {
            repository.deleteLoan(loanId)
            _userMessage.emit("Loan record removed")
        }
    }

    // Notification Testing Action
    fun checkAndSendNotificationsNow() {
        viewModelScope.launch {
            val active = repository.getActiveLoansSync()
            val now = System.currentTimeMillis()
            var dispatched = 0

            active.forEachIndexed { idx, loan ->
                val rackInfo = "${loan.rackId} • ${loan.shelfNumber}"
                if (loan.dueDate < now) {
                    dispatched++
                    val status = DateUtils.getDueStatusDescription(loan.dueDate)
                    NotificationHelper.showOverdueNotification(
                        context = getApplication(),
                        notificationId = 1000 + idx,
                        bookTitle = loan.bookTitle,
                        memberName = loan.memberName,
                        statusDescription = status,
                        rackLocation = rackInfo
                    )
                } else if (DateUtils.isDueSoon(loan.dueDate, 2)) {
                    dispatched++
                    val status = DateUtils.getDueStatusDescription(loan.dueDate)
                    NotificationHelper.showDueSoonNotification(
                        context = getApplication(),
                        notificationId = 2000 + idx,
                        bookTitle = loan.bookTitle,
                        memberName = loan.memberName,
                        statusDescription = status,
                        rackLocation = rackInfo
                    )
                }
            }

            if (dispatched > 0) {
                _userMessage.emit("Automated notifications dispatched for $dispatched book(s)")
            } else {
                _userMessage.emit("All books are on time! No overdue or approaching returns.")
            }
        }
    }

    // Backup & Restore
    suspend fun exportDatabaseBackup(): String {
        return com.example.data.BackupRestoreManager.exportToJson(database)
    }

    fun restoreDatabase(jsonString: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            val result = com.example.data.BackupRestoreManager.importFromJson(jsonString, database, replaceExisting)
            result.onSuccess { stats ->
                val mode = if (replaceExisting) "Restored" else "Merged"
                _userMessage.emit("Database restored successfully! $mode ${stats.booksCount} books, ${stats.membersCount} members, ${stats.loansCount} loans.")
            }.onFailure { error ->
                _userMessage.emit("Restore failed: ${error.message ?: "Invalid backup file"}")
            }
        }
    }
}
