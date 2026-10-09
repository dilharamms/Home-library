package com.example.data

import org.json.JSONArray
import org.json.JSONObject

data class BackupStats(
    val booksCount: Int,
    val membersCount: Int,
    val loansCount: Int
)

object BackupRestoreManager {

    suspend fun exportToJson(database: LibraryDatabase): String {
        val books = database.bookDao().getAllBooksSync()
        val members = database.memberDao().getAllMembersSync()
        val loans = database.loanDao().getAllLoansSync()
        val categories = database.categoryDao().getAllCategoriesSync()

        val rootObj = JSONObject()
        rootObj.put("version", 2)
        rootObj.put("appName", "Home Library")
        rootObj.put("exportedAt", System.currentTimeMillis())

        // Categories Array
        val categoriesArray = JSONArray()
        categories.forEach { cat ->
            val cObj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("colorHex", cat.colorHex)
                put("description", cat.description)
                put("isDefault", cat.isDefault)
            }
            categoriesArray.put(cObj)
        }
        rootObj.put("categories", categoriesArray)

        // Books Array
        val booksArray = JSONArray()
        books.forEach { book ->
            val bObj = JSONObject().apply {
                put("id", book.id)
                put("title", book.title)
                put("author", book.author)
                put("genre", book.genre)
                put("isbn", book.isbn)
                put("rackId", book.rackId)
                put("shelfNumber", book.shelfNumber)
                put("section", book.section)
                put("totalCopies", book.totalCopies)
                put("availableCopies", book.availableCopies)
                put("publicationYear", book.publicationYear ?: JSONObject.NULL)
                put("coverColorHex", book.coverColorHex)
                put("notes", book.notes)
                put("dateAdded", book.dateAdded)
            }
            booksArray.put(bObj)
        }
        rootObj.put("books", booksArray)

        // Members Array
        val membersArray = JSONArray()
        members.forEach { member ->
            val mObj = JSONObject().apply {
                put("id", member.id)
                put("membershipCode", member.membershipCode)
                put("name", member.name)
                put("email", member.email)
                put("phone", member.phone)
                put("address", member.address)
                put("notes", member.notes)
                put("registeredDate", member.registeredDate)
                put("activeLoansCount", member.activeLoansCount)
            }
            membersArray.put(mObj)
        }
        rootObj.put("members", membersArray)

        // Loans Array
        val loansArray = JSONArray()
        loans.forEach { loan ->
            val lObj = JSONObject().apply {
                put("id", loan.id)
                put("bookId", loan.bookId)
                put("memberId", loan.memberId)
                put("borrowDate", loan.borrowDate)
                put("dueDate", loan.dueDate)
                put("returnDate", loan.returnDate ?: JSONObject.NULL)
                put("status", loan.status)
                put("notes", loan.notes)
            }
            loansArray.put(lObj)
        }
        rootObj.put("loans", loansArray)

        return rootObj.toString(2)
    }

    suspend fun importFromJson(
        jsonString: String,
        database: LibraryDatabase,
        replaceExisting: Boolean
    ): Result<BackupStats> {
        return try {
            val rootObj = JSONObject(jsonString)

            if (!rootObj.has("books") || !rootObj.has("members")) {
                return Result.failure(IllegalArgumentException("Invalid backup file: Missing books or members data."))
            }

            val booksArray = rootObj.getJSONArray("books")
            val membersArray = rootObj.getJSONArray("members")
            val loansArray = if (rootObj.has("loans")) rootObj.getJSONArray("loans") else JSONArray()

            val parsedBooks = mutableListOf<Book>()
            for (i in 0 until booksArray.length()) {
                val b = booksArray.getJSONObject(i)
                parsedBooks.add(
                    Book(
                        id = if (replaceExisting) b.optLong("id", 0L) else 0L,
                        title = b.getString("title"),
                        author = b.getString("author"),
                        genre = b.optString("genre", "General"),
                        isbn = b.optString("isbn", ""),
                        rackId = b.optString("rackId", "Rack A"),
                        shelfNumber = b.optString("shelfNumber", "Shelf 1"),
                        section = b.optString("section", ""),
                        totalCopies = b.optInt("totalCopies", 1),
                        availableCopies = b.optInt("availableCopies", 1),
                        publicationYear = if (b.isNull("publicationYear")) null else b.optInt("publicationYear"),
                        coverColorHex = b.optString("coverColorHex", "#2563EB"),
                        notes = b.optString("notes", ""),
                        dateAdded = b.optLong("dateAdded", System.currentTimeMillis())
                    )
                )
            }

            val parsedMembers = mutableListOf<Member>()
            for (i in 0 until membersArray.length()) {
                val m = membersArray.getJSONObject(i)
                parsedMembers.add(
                    Member(
                        id = if (replaceExisting) m.optLong("id", 0L) else 0L,
                        membershipCode = m.optString("membershipCode", "HL-001"),
                        name = m.getString("name"),
                        email = m.optString("email", ""),
                        phone = m.optString("phone", ""),
                        address = m.optString("address", ""),
                        notes = m.optString("notes", ""),
                        registeredDate = m.optLong("registeredDate", System.currentTimeMillis()),
                        activeLoansCount = m.optInt("activeLoansCount", 0)
                    )
                )
            }

            val parsedLoans = mutableListOf<Loan>()
            for (i in 0 until loansArray.length()) {
                val l = loansArray.getJSONObject(i)
                parsedLoans.add(
                    Loan(
                        id = if (replaceExisting) l.optLong("id", 0L) else 0L,
                        bookId = l.getLong("bookId"),
                        memberId = l.getLong("memberId"),
                        borrowDate = l.optLong("borrowDate", System.currentTimeMillis()),
                        dueDate = l.getLong("dueDate"),
                        returnDate = if (l.isNull("returnDate")) null else l.optLong("returnDate"),
                        status = l.optString("status", "ACTIVE"),
                        notes = l.optString("notes", "")
                    )
                )
            }

            val bookDao = database.bookDao()
            val memberDao = database.memberDao()
            val loanDao = database.loanDao()
            val categoryDao = database.categoryDao()

            if (rootObj.has("categories")) {
                val catArray = rootObj.getJSONArray("categories")
                val parsedCats = mutableListOf<Category>()
                for (i in 0 until catArray.length()) {
                    val c = catArray.getJSONObject(i)
                    parsedCats.add(
                        Category(
                            id = if (replaceExisting) c.optLong("id", 0L) else 0L,
                            name = c.getString("name"),
                            colorHex = c.optString("colorHex", "#2563EB"),
                            description = c.optString("description", ""),
                            isDefault = c.optBoolean("isDefault", false)
                        )
                    )
                }
                if (replaceExisting) {
                    categoryDao.clearAllCategories()
                    categoryDao.insertAll(parsedCats)
                } else {
                    categoryDao.insertAll(parsedCats)
                }
            }

            if (replaceExisting) {
                loanDao.clearAllLoans()
                bookDao.clearAllBooks()
                memberDao.clearAllMembers()

                bookDao.insertAll(parsedBooks)
                memberDao.insertAll(parsedMembers)
                loanDao.insertAll(parsedLoans)
            } else {
                // Merge: insert books and members with auto-generated IDs
                parsedBooks.forEach { bookDao.insertBook(it) }
                parsedMembers.forEach { memberDao.insertMember(it) }
            }

            Result.success(
                BackupStats(
                    booksCount = parsedBooks.size,
                    membersCount = parsedMembers.size,
                    loansCount = if (replaceExisting) parsedLoans.size else 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
