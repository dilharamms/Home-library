package com.example.data

data class LoanWithDetails(
    val loanId: Long,
    val bookId: Long,
    val memberId: Long,
    val borrowDate: Long,
    val dueDate: Long,
    val returnDate: Long?,
    val status: String,
    val notes: String,
    val bookTitle: String,
    val bookAuthor: String,
    val bookGenre: String,
    val bookCoverColor: String,
    val rackId: String,
    val shelfNumber: String,
    val memberName: String,
    val memberPhone: String,
    val memberEmail: String,
    val memberCode: String
)
