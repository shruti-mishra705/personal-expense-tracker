package com.example.personalexpensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // Primary key as Int, non-null
    val amount: Double,          // Amount as Double for calculations
    val category: String,
    val date: String,
    val time: String
)
