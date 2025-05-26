package com.example.personalexpensetracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var expenseListView: LinearLayout
    private lateinit var totalTextView: TextView
    private lateinit var db: ExpenseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expenseListView = findViewById(R.id.expenseList)
        totalTextView = findViewById(R.id.tv_total)
        val addButton = findViewById<Button>(R.id.btn_add)
        val detailsButton = findViewById<Button>(R.id.btn_details) // ✅ Fix: initialize correctly

        db = ExpenseDatabase.getDatabase(this)

        addButton.setOnClickListener {
            showAddExpenseDialog()
        }

        detailsButton.setOnClickListener {
            val intent = Intent(this, DetailsActivity::class.java)
            startActivity(intent)
        }

        loadExpensesFromDB()
    }

    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.et_amount)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val dateText = dialogView.findViewById<TextView>(R.id.tv_date)
        val timeText = dialogView.findViewById<TextView>(R.id.tv_time)

// Set up category spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.expense_categories,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        dateText.text = dateFormat.format(calendar.time)
        timeText.text = timeFormat.format(calendar.time)

        dateText.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dateText.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeText.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    timeText.text = timeFormat.format(calendar.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull()
                val category = categorySpinner.selectedItem.toString()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val dateStr = dateFormat.format(calendar.time)
                val timeStr = timeFormat.format(calendar.time)
                val expense = Expense(
                    amount = amount,
                    category = category,
                    date = dateStr,
                    time = timeStr
                )
                lifecycleScope.launch {
                    db.expenseDao().insert(expense)
                    loadExpensesFromDB()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(expense: Expense) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.et_amount)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val dateText = dialogView.findViewById<TextView>(R.id.tv_date)
        val timeText = dialogView.findViewById<TextView>(R.id.tv_time)

// Set up category spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.expense_categories,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

// Set current values
        amountInput.setText(expense.amount.toString())
        val categoryIndex = resources.getStringArray(R.array.expense_categories).indexOf(expense.category)
        if (categoryIndex >= 0) categorySpinner.setSelection(categoryIndex)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            val dateParts = expense.date.split("/")
            val day = dateParts[0].toInt()
            val month = dateParts[1].toInt() - 1
            val year = dateParts[2].toInt()
            calendar.set(year, month, day)
        } catch (e: Exception) { }

        dateText.text = expense.date
        timeText.text = expense.time

        dateText.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dateText.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeText.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    timeText.text = timeFormat.format(calendar.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Expense")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newAmount = amountInput.text.toString().toDoubleOrNull()
                val newCategory = categorySpinner.selectedItem.toString()

                if (newAmount == null || newAmount <= 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newDate = dateText.text.toString()
                val newTime = timeText.text.toString()

                val updatedExpense = expense.copy(
                    amount = newAmount,
                    category = newCategory,
                    date = newDate,
                    time = newTime
                )

                lifecycleScope.launch {
                    db.expenseDao().update(updatedExpense)
                    loadExpensesFromDB()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun loadExpensesFromDB() {
        lifecycleScope.launch {
            val expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getAllExpenses()
            }
            updateUI(expenses)
        }
    }

    private fun updateUI(expenses: List<Expense>) {
        expenseListView.removeAllViews()
        var total = 0.0

        for (expense in expenses) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 10, 0, 10)
            }

            val expenseText = TextView(this).apply {
                text = "${expense.category}: ₹${expense.amount}\nDate: ${expense.date} Time: ${expense.time}"
                textSize = 16f
            }

            val buttonRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(this).apply {
                text = "Edit"
                setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    showEditDialog(expense)
                }
            }

            val deleteButton = Button(this).apply {
                text = "Delete"
                setBackgroundColor(Color.parseColor("#F44336")) // Red
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    lifecycleScope.launch {
                        db.expenseDao().delete(expense)
                        loadExpensesFromDB()
                    }
                }
            }

            buttonRow.addView(editButton)
            buttonRow.addView(deleteButton)

            itemLayout.addView(expenseText)
            itemLayout.addView(buttonRow)

            expenseListView.addView(itemLayout)

            total += expense.amount
        }

        totalTextView.text = "Total: ₹$total"
    }
}