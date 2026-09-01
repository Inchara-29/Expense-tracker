package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.FinancialGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class SpendingInsight(
    val healthScore: Int, // 0 - 100
    val scoreGrade: String, // "Excellent", "Good", "Needs Attention"
    val summary: String,
    val topSpendingCategory: String,
    val savingsRate: Double,
    val recommendations: List<String>,
    val alerts: List<String>
)

class AiAdvisorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun generateLocalAnalytics(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<FinancialGoal>,
        currencySymbol: String
    ): SpendingInsight {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100.0).coerceAtLeast(0.0) else 0.0

        val categorySpending = transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val topCategory = categorySpending.maxByOrNull { it.value }?.key ?: "None"
        val topCategoryAmount = categorySpending.maxByOrNull { it.value }?.value ?: 0.0

        // Calculate Budget alerts
        val alerts = mutableListOf<String>()
        budgets.forEach { budget ->
            val spent = transactions.filter { it.type == TransactionType.EXPENSE && (it.categoryId == budget.categoryId || it.categoryName.equals(budget.categoryName, ignoreCase = true)) }
                .sumOf { it.amount }
            val ratio = if (budget.amount > 0) spent / budget.amount else 0.0
            if (ratio >= 1.0) {
                alerts.add("⚠️ Exceeded ${budget.categoryName} budget! Spent $currencySymbol${String.format(Locale.getDefault(), "%,.0f", spent)} of $currencySymbol${String.format(Locale.getDefault(), "%,.0f", budget.amount)} limit (${(ratio * 100).toInt()}%).")
            } else if (ratio >= 0.85) {
                alerts.add("⚡ Close to limit: ${budget.categoryName} is at ${(ratio * 100).toInt()}% of budget.")
            }
        }

        // Calculate Health Score
        var score = 75
        if (savingsRate > 30) score += 15
        else if (savingsRate > 15) score += 8
        else if (savingsRate < 5) score -= 15

        if (alerts.any { it.contains("Exceeded") }) score -= 12
        if (totalExpense > totalIncome && totalIncome > 0) score -= 20

        score = score.coerceIn(10, 98)

        val grade = when {
            score >= 80 -> "Excellent"
            score >= 65 -> "Good"
            score >= 50 -> "Fair"
            else -> "Needs Attention"
        }

        val recommendations = mutableListOf<String>()
        if (savingsRate < 20) {
            recommendations.add("Aim to follow the 50/30/20 rule: Allocate 50% to needs, 30% to wants, and 20% directly to savings.")
        }
        if (topCategoryAmount > 0) {
            recommendations.add("Your highest expense is $topCategory ($currencySymbol${String.format(Locale.getDefault(), "%,.0f", topCategoryAmount)}). Setting a stricter budget could unlock ₹3,000+ monthly.")
        }
        if (goals.isNotEmpty()) {
            val totalGoalTarget = goals.sumOf { it.targetAmount }
            val totalGoalSaved = goals.sumOf { it.currentAmount }
            val overallProgress = if (totalGoalTarget > 0) (totalGoalSaved / totalGoalTarget * 100).toInt() else 0
            recommendations.add("You've funded $overallProgress% of your financial targets across ${goals.size} active goals. Keep depositing consistently!")
        } else {
            recommendations.add("Create an Emergency Fund goal to build at least 3-6 months of safety runway.")
        }

        val summary = if (totalIncome > totalExpense) {
            "Strong cash flow with a ${String.format(Locale.getDefault(), "%.1f", savingsRate)}% savings rate. You've retained $currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalIncome - totalExpense)} in net surplus."
        } else {
            "Expenses exceed recorded income by $currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalExpense - totalIncome)}. Review discretionary lifestyle outlays."
        }

        return SpendingInsight(
            healthScore = score,
            scoreGrade = grade,
            summary = summary,
            topSpendingCategory = topCategory,
            savingsRate = savingsRate,
            recommendations = recommendations,
            alerts = alerts
        )
    }

    suspend fun askAiAdvisor(
        userQuestion: String,
        transactions: List<Transaction>,
        accounts: List<Account>,
        budgets: List<Budget>,
        goals: List<FinancialGoal>,
        currencySymbol: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // High quality local context-aware response
            return@withContext generateSmartLocalResponse(userQuestion, transactions, accounts, budgets, goals, currencySymbol)
        }

        try {
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val balance = accounts.sumOf { it.balance }

            val topCategories = transactions.filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryName }
                .mapValues { it.value.sumOf { tx -> tx.amount } }
                .entries.sortedByDescending { it.value }
                .take(5)
                .joinToString(", ") { "${it.key}: $currencySymbol${it.value.toInt()}" }

            val systemPrompt = """
                You are a friendly, expert personal wealth & financial advisor for an expense tracking application.
                Current Financial Context:
                - Currency: $currencySymbol
                - Total Net Worth / Balance: $currencySymbol$balance
                - Monthly Income: $currencySymbol$totalIncome
                - Monthly Expense: $currencySymbol$totalExpense
                - Top Spending Categories: $topCategories
                - Active Budgets: ${budgets.size} configured
                - Financial Goals: ${goals.joinToString { "${it.title} (${(it.currentAmount/it.targetAmount.coerceAtLeast(1.0)*100).toInt()}%)" }}

                User Query: "$userQuestion"

                Provide a direct, encouraging, and actionable financial recommendation in 2 to 4 concise bullet points or short paragraphs. Keep it practical, empathetic, and clear.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().put("text", systemPrompt))
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val resJson = JSONObject(resStr)
                val candidates = resJson.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
            return@withContext generateSmartLocalResponse(userQuestion, transactions, accounts, budgets, goals, currencySymbol)
        } catch (e: Exception) {
            return@withContext generateSmartLocalResponse(userQuestion, transactions, accounts, budgets, goals, currencySymbol)
        }
    }

    private fun generateSmartLocalResponse(
        query: String,
        transactions: List<Transaction>,
        accounts: List<Account>,
        budgets: List<Budget>,
        goals: List<FinancialGoal>,
        currencySymbol: String
    ): String {
        val q = query.lowercase()
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense

        val categorySpending = transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryName }
            .mapValues { it.value.sumOf { tx -> tx.amount } }

        return when {
            q.contains("food") || q.contains("dining") || q.contains("eat") -> {
                val foodSpent = categorySpending["Food"] ?: 0.0
                val diningSpent = categorySpending["Restaurants"] ?: 0.0
                val groceries = categorySpending["Groceries"] ?: 0.0
                val totalFood = foodSpent + diningSpent + groceries
                "🍔 **Food & Dining Breakdown**\n• Total Food Outlay: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalFood)}\n• Groceries: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", groceries)}\n• Dining & Takeout: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", foodSpent + diningSpent)}\n\n💡 *Tip*: Meal prepping 2 days a week can easily save ~15-20% on weekly dining costs."
            }
            q.contains("save") || q.contains("cut") || q.contains("reduce") -> {
                val topCat = categorySpending.maxByOrNull { it.value }
                "💰 **Smart Savings Recommendations**\n• **Highest Outlay**: Your biggest non-fixed spending is in **${topCat?.key ?: "General"}** ($currencySymbol${String.format(Locale.getDefault(), "%,.0f", topCat?.value ?: 0.0)}).\n• **Audit Subscriptions**: Check recurring digital memberships for unused services.\n• **Automate 20%**: Transfer $currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalIncome * 0.2)} to your savings account on pay day before spending."
            }
            q.contains("budget") || q.contains("health") || q.contains("status") -> {
                val rate = if (totalIncome > 0) (netSavings / totalIncome * 100).toInt() else 0
                "📊 **Monthly Budget Assessment**\n• **Income**: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalIncome)}\n• **Expenses**: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalExpense)}\n• **Net Surplus**: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", netSavings)} ($rate% savings rate)\n• **Active Budgets**: ${budgets.size} categories monitored with automated limit alerts."
            }
            q.contains("goal") || q.contains("target") -> {
                if (goals.isEmpty()) {
                    "🎯 You don't have any financial goals configured yet. Go to the Accounts & Goals tab to create targets like Emergency Fund, Vacation, or Tech Gadget!"
                } else {
                    val summary = goals.joinToString("\n") {
                        val pct = if (it.targetAmount > 0) (it.currentAmount / it.targetAmount * 100).toInt() else 0
                        "• **${it.title}**: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", it.currentAmount)} / $currencySymbol${String.format(Locale.getDefault(), "%,.0f", it.targetAmount)} ($pct%)"
                    }
                    "🎯 **Goals Progress Overview**\n$summary\n\nKeep depositing your monthly surplus to reach your milestone deadlines on time!"
                }
            }
            else -> {
                val topCats = categorySpending.entries.sortedByDescending { it.value }.take(3)
                    .joinToString("\n") { "• ${it.key}: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", it.value)}" }
                "💡 **Financial Overview & Advice**\n• Total Balance Across Wallets: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", accounts.sumOf { it.balance })}\n• Monthly Surplus: $currencySymbol${String.format(Locale.getDefault(), "%,.0f", netSavings)}\n\n**Top Spending Drivers**:\n$topCats\n\nFeel free to ask specific questions like *'How much did I spend on Food?'* or *'Where can I save more?'*!"
            }
        }
    }
}
