package com.example.personalexpensetracker

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {

    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvExpenseCount: TextView
    private lateinit var tvAvgExpense: TextView
    private lateinit var tvSavings: TextView
    private lateinit var tvSalary: TextView
    private lateinit var pieChart: PieChart
    private lateinit var btnChangeSalary: Button
    private lateinit var btnBack: Button
    private lateinit var db: ExpenseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        tvTotalExpenses = findViewById(R.id.tv_total_expenses)
        tvExpenseCount = findViewById(R.id.tv_expense_count)
        tvAvgExpense = findViewById(R.id.tv_avg_expense)
        tvSavings = findViewById(R.id.tv_savings)
        tvSalary = findViewById(R.id.tv_salary)
        pieChart = findViewById(R.id.pieChart)
        btnChangeSalary = findViewById(R.id.btn_change_salary)
        btnBack = findViewById(R.id.btn_back)

        db = ExpenseDatabase.getDatabase(this)

        btnChangeSalary.setOnClickListener {
            showSalaryDialog()
        }

        btnBack.setOnClickListener {
            finish() // Go back to MainActivity
        }

        askSalaryIfNeeded()
    }

    private fun showSalaryDialog() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        // Show current salary as hint
        val currentSalary = prefs.getFloat("salary", 0f)
        if (currentSalary > 0) input.hint = "Current: ₹%.2f".format(currentSalary)

        AlertDialog.Builder(this)
            .setTitle("Enter your monthly salary")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val entered = input.text.toString().toFloatOrNull()
                if (entered != null && entered > 0) {
                    prefs.edit().putFloat("salary", entered).apply()
                    loadExpenseSummary()
                } else {
                    // Show error and re-prompt
                    AlertDialog.Builder(this)
                        .setTitle("Invalid Salary")
                        .setMessage("Please enter a positive number for salary.")
                        .setPositiveButton("OK") { _, _ -> showSalaryDialog() }
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun askSalaryIfNeeded() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val salary = prefs.getFloat("salary", -1f)
        if (salary < 0) {
            showSalaryDialog()
        } else {
            loadExpenseSummary()
        }
    }

    private fun loadExpenseSummary() {
        lifecycleScope.launch {
            val expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getAllExpenses()
            }

            val total = expenses.sumOf { it.amount }
            val count = expenses.size
            val average = if (count > 0) total / count else 0.0

            tvTotalExpenses.text = "Total Expenses: ₹%.2f".format(total)
            tvExpenseCount.text = "Number of Expenses: $count"
            tvAvgExpense.text = "Average Expense: ₹%.2f".format(average)

            val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val salary = prefs.getFloat("salary", 0f).toDouble()
            tvSalary.text = "Salary: ₹%.2f".format(salary)

            val savings = salary - total
            val savingsPercent = if (salary > 0) (savings / salary) * 100 else 0.0

            // Color and message logic
            when {
                savings < 0 -> {
                    tvSavings.setTextColor(Color.RED)
                    tvSavings.text = "Overspent by ₹%.2f (%.1f%% over budget)".format(-savings, -savingsPercent)
                }
                savings == 0.0 -> {
                    tvSavings.setTextColor(Color.parseColor("#FFA000")) // Amber
                    tvSavings.text = "No savings (0%)"
                }
                else -> {
                    tvSavings.setTextColor(Color.parseColor("#388E3C")) // Green
                    tvSavings.text = "Savings: ₹%.2f (%.1f%%)".format(savings, savingsPercent)
                }
            }

            // Pie chart for expenses by category
            val categoryTotals = expenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            val entries = ArrayList<PieEntry>()
            for ((category, amount) in categoryTotals) {
                entries.add(PieEntry(amount.toFloat(), category))
            }
            val dataSet = PieDataSet(entries, "Expenses")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            pieChart.data = PieData(dataSet)
            pieChart.description.isEnabled = false
            pieChart.centerText = "Expenses by Category"
            pieChart.animateY(1000)
            pieChart.invalidate()
        }
    }
}
