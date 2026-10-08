package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val genre: String,
    val isbn: String = "",
    val rackId: String,          // e.g. "Rack A", "Rack B", "Rack 1"
    val shelfNumber: String,     // e.g. "Shelf 1", "Shelf 2", "Top Shelf"
    val section: String = "",    // e.g. "Main Room", "Study Nook"
    val totalCopies: Int = 1,
    val availableCopies: Int = 1,
    val publicationYear: Int? = null,
    val coverColorHex: String = "#3B82F6", // Hex color for book badge/spine styling
    val notes: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)
