package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Book::class, Member::class, Loan::class, Category::class],
    version = 2,
    exportSchema = false
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun memberDao(): MemberDao
    abstract fun loanDao(): LoanDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "home_library_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(LibraryDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class LibraryDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: LibraryDatabase) {
            val bookDao = database.bookDao()
            val memberDao = database.memberDao()
            val loanDao = database.loanDao()
            val categoryDao = database.categoryDao()

            // 0. Initial Categories & Genres
            val initialCategories = listOf(
                Category(name = "Classic Fiction", colorHex = "#2563EB", isDefault = true),
                Category(name = "Technology", colorHex = "#059669", isDefault = true),
                Category(name = "History", colorHex = "#D97706", isDefault = true),
                Category(name = "Philosophy", colorHex = "#7C3AED", isDefault = true),
                Category(name = "Design", colorHex = "#DC2626", isDefault = true),
                Category(name = "Dystopian Fiction", colorHex = "#475569", isDefault = true),
                Category(name = "Sci-Fi", colorHex = "#EA580C", isDefault = true),
                Category(name = "Science", colorHex = "#0284C7", isDefault = true),
                Category(name = "Mystery & Thriller", colorHex = "#9333EA", isDefault = true),
                Category(name = "Biography", colorHex = "#0D9488", isDefault = true),
                Category(name = "Art & Literature", colorHex = "#E11D48", isDefault = true)
            )
            categoryDao.insertAll(initialCategories)

            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            // 1. Initial Books
            val initialBooks = listOf(
                Book(
                    id = 1,
                    title = "To Kill a Mockingbird",
                    author = "Harper Lee",
                    genre = "Classic Fiction",
                    isbn = "978-0061120084",
                    rackId = "Rack A",
                    shelfNumber = "Shelf 1",
                    section = "Main Living Room",
                    totalCopies = 2,
                    availableCopies = 1,
                    publicationYear = 1960,
                    coverColorHex = "#2563EB",
                    notes = "Pulitzer Prize winner. Leather-bound collector's edition."
                ),
                Book(
                    id = 2,
                    title = "Clean Code: Agile Software Craftsmanship",
                    author = "Robert C. Martin",
                    genre = "Technology",
                    isbn = "978-0132350884",
                    rackId = "Rack B",
                    shelfNumber = "Shelf 2",
                    section = "Study Room East",
                    totalCopies = 2,
                    availableCopies = 1,
                    publicationYear = 2008,
                    coverColorHex = "#059669",
                    notes = "Handbook of software craftsmanship."
                ),
                Book(
                    id = 3,
                    title = "Sapiens: A Brief History of Humankind",
                    author = "Yuval Noah Harari",
                    genre = "History",
                    isbn = "978-0062316097",
                    rackId = "Rack C",
                    shelfNumber = "Shelf 3",
                    section = "Study Room West",
                    totalCopies = 3,
                    availableCopies = 2,
                    publicationYear = 2014,
                    coverColorHex = "#D97706",
                    notes = "Survey of human evolutionary history."
                ),
                Book(
                    id = 4,
                    title = "Meditations",
                    author = "Marcus Aurelius",
                    genre = "Philosophy",
                    isbn = "978-0140449334",
                    rackId = "Rack C",
                    shelfNumber = "Shelf 1",
                    section = "Study Room West",
                    totalCopies = 2,
                    availableCopies = 2,
                    publicationYear = 180,
                    coverColorHex = "#7C3AED",
                    notes = "Hays translation. Stoic philosophy classic."
                ),
                Book(
                    id = 5,
                    title = "The Design of Everyday Things",
                    author = "Don Norman",
                    genre = "Design",
                    isbn = "978-0465050659",
                    rackId = "Rack B",
                    shelfNumber = "Shelf 1",
                    section = "Study Room East",
                    totalCopies = 1,
                    availableCopies = 0,
                    publicationYear = 2013,
                    coverColorHex = "#DC2626",
                    notes = "Revised edition. Fundamental cognitive design text."
                ),
                Book(
                    id = 6,
                    title = "1984",
                    author = "George Orwell",
                    genre = "Dystopian Fiction",
                    isbn = "978-0451524935",
                    rackId = "Rack A",
                    shelfNumber = "Shelf 2",
                    section = "Main Living Room",
                    totalCopies = 2,
                    availableCopies = 2,
                    publicationYear = 1949,
                    coverColorHex = "#475569",
                    notes = "Centennial hardback release."
                ),
                Book(
                    id = 7,
                    title = "Dune",
                    author = "Frank Herbert",
                    genre = "Sci-Fi",
                    isbn = "978-0441013593",
                    rackId = "Rack A",
                    shelfNumber = "Shelf 3",
                    section = "Main Living Room",
                    totalCopies = 2,
                    availableCopies = 2,
                    publicationYear = 1965,
                    coverColorHex = "#EA580C",
                    notes = "Sci-fi epic with fold-out Arrakis map."
                ),
                Book(
                    id = 8,
                    title = "Cosmos",
                    author = "Carl Sagan",
                    genre = "Science",
                    isbn = "978-0345539434",
                    rackId = "Rack B",
                    shelfNumber = "Shelf 3",
                    section = "Study Room East",
                    totalCopies = 1,
                    availableCopies = 1,
                    publicationYear = 1980,
                    coverColorHex = "#0284C7",
                    notes = "Illustrated coffee-table edition."
                )
            )
            bookDao.insertAll(initialBooks)

            // 2. Initial Members
            val initialMembers = listOf(
                Member(
                    id = 1,
                    membershipCode = "HL-101",
                    name = "Alice Vance",
                    email = "alice.vance@example.com",
                    phone = "+1 (555) 234-5678",
                    address = "Oak Grove, Apt 4B",
                    notes = "Avid fiction & sci-fi reader. Family friend.",
                    registeredDate = now - (30 * dayMs),
                    activeLoansCount = 1
                ),
                Member(
                    id = 2,
                    membershipCode = "HL-102",
                    name = "Marcus Sterling",
                    email = "marcus.s@example.com",
                    phone = "+1 (555) 876-5432",
                    address = "12 Maple Street",
                    notes = "Software architect. Borrows engineering books.",
                    registeredDate = now - (20 * dayMs),
                    activeLoansCount = 1
                ),
                Member(
                    id = 3,
                    membershipCode = "HL-103",
                    name = "Elena Rostova",
                    email = "elena.r@example.com",
                    phone = "+1 (555) 432-1098",
                    address = "87 Birch Boulevard",
                    notes = "Design lead, loves UX & history.",
                    registeredDate = now - (15 * dayMs),
                    activeLoansCount = 1
                ),
                Member(
                    id = 4,
                    membershipCode = "HL-104",
                    name = "David Chen",
                    email = "david.c@example.com",
                    phone = "+1 (555) 321-7890",
                    address = "204 Pine View Way",
                    notes = "History enthusiast.",
                    registeredDate = now - (10 * dayMs),
                    activeLoansCount = 0
                )
            )
            memberDao.insertAll(initialMembers)

            // 3. Initial Loans
            // Loan 1: Overdue loan (due 3 days ago) -> Triggers overdue notification & dashboard warning
            val loanOverdue = Loan(
                id = 1,
                bookId = 5, // The Design of Everyday Things
                memberId = 3, // Elena Rostova
                borrowDate = now - (17 * dayMs),
                dueDate = now - (3 * dayMs),
                returnDate = null,
                status = "ACTIVE",
                notes = "Elena requested 2-week loan for project research."
            )

            // Loan 2: Due tomorrow (Approaching return date!)
            val loanDueSoon = Loan(
                id = 2,
                bookId = 1, // To Kill a Mockingbird
                memberId = 1, // Alice Vance
                borrowDate = now - (13 * dayMs),
                dueDate = now + (1 * dayMs),
                returnDate = null,
                status = "ACTIVE",
                notes = "Borrowed for weekend book club discussion."
            )

            // Loan 3: Active loan due in 6 days
            val loanActive = Loan(
                id = 3,
                bookId = 2, // Clean Code
                memberId = 2, // Marcus Sterling
                borrowDate = now - (5 * dayMs),
                dueDate = now + (6 * dayMs),
                returnDate = null,
                status = "ACTIVE",
                notes = "Deep dive into clean architecture chapters."
            )

            loanDao.insertAll(listOf(loanOverdue, loanDueSoon, loanActive))
        }
    }
}
