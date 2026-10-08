package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["memberId"]),
        Index(value = ["status"])
    ]
)
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val memberId: Long,
    val borrowDate: Long = System.currentTimeMillis(),
    val dueDate: Long,
    val returnDate: Long? = null,
    val status: String = "ACTIVE", // "ACTIVE", "RETURNED", "OVERDUE"
    val notes: String = ""
)
