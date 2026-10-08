package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val membershipCode: String, // e.g. "LIB-101"
    val name: String,
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val registeredDate: Long = System.currentTimeMillis(),
    val activeLoansCount: Int = 0
)
