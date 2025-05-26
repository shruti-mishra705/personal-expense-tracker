package com.example.personalexpensetracker

import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expense_table")
    suspend fun getAllExpenses(): List<Expense>

    @Query("DELETE FROM expense_table")
    suspend fun deleteAll()

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)


}
