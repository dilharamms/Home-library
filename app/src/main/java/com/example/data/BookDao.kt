package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdSync(id: Long): Book?

    @Query("""
        SELECT * FROM books 
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR isbn LIKE '%' || :query || '%')
        AND (:genre = '' OR genre = :genre)
        AND (:rackId = '' OR rackId = :rackId)
        ORDER BY title ASC
    """)
    fun searchBooks(query: String, genre: String, rackId: String): Flow<List<Book>>

    @Query("SELECT DISTINCT genre FROM books WHERE genre != '' ORDER BY genre ASC")
    fun getAllGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT rackId FROM books WHERE rackId != '' ORDER BY rackId ASC")
    fun getAllRacks(): Flow<List<String>>

    @Query("SELECT * FROM books WHERE rackId = :rackId ORDER BY shelfNumber ASC, title ASC")
    fun getBooksByRack(rackId: String): Flow<List<Book>>

    @Query("SELECT COUNT(*) FROM books")
    fun getTotalBooksCount(): Flow<Int>

    @Query("SELECT IFNULL(SUM(totalCopies), 0) FROM books")
    fun getTotalCopiesCount(): Flow<Int>

    @Query("SELECT IFNULL(SUM(availableCopies), 0) FROM books")
    fun getAvailableCopiesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<Book>)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE books SET availableCopies = availableCopies - 1 WHERE id = :bookId AND availableCopies > 0")
    suspend fun decrementAvailableCopies(bookId: Long): Int

    @Query("UPDATE books SET availableCopies = MIN(totalCopies, availableCopies + 1) WHERE id = :bookId")
    suspend fun incrementAvailableCopies(bookId: Long): Int
}
