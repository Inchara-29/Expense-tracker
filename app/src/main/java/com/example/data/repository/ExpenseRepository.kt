package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.FinancialGoal
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.PaymentMethod
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseRepository(private val database: AppDatabase) {

    // DAOs
    private val accountDao = database.accountDao()
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val goalDao = database.goalDao()
    private val recurringDao = database.recurringDao()
    private val notificationDao = database.notificationDao()

    // Flows
    val activeAccounts: Flow<List<Account>> = accountDao.getAllActiveAccounts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<FinancialGoal>> = goalDao.getAllGoals()
    val allRecurring: Flow<List<RecurringTransaction>> = recurringDao.getAllActiveRecurring()
    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    // Transaction Management with automatic account balance reflection
    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)

        // Adjust Account Balance
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustAccountBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                accountDao.adjustAccountBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustAccountBalance(transaction.accountId, -transaction.amount)
                transaction.destinationAccountId?.let { destId ->
                    accountDao.adjustAccountBalance(destId, transaction.amount)
                }
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)

        // Reverse the balance impact
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustAccountBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.INCOME -> {
                accountDao.adjustAccountBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustAccountBalance(transaction.accountId, transaction.amount)
                transaction.destinationAccountId?.let { destId ->
                    accountDao.adjustAccountBalance(destId, -transaction.amount)
                }
            }
        }
    }

    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        // Revert old
        deleteTransaction(oldTransaction)
        // Add new
        addTransaction(newTransaction)
    }

    suspend fun bulkDeleteTransactions(transactions: List<Transaction>) {
        transactions.forEach { deleteTransaction(it) }
    }

    suspend fun bulkUpdateCategory(ids: List<String>, newCategoryId: String, newCategoryName: String) {
        transactionDao.bulkUpdateCategory(ids, newCategoryId, newCategoryName)
    }

    // Account Operations
    suspend fun addAccount(account: Account) = accountDao.insertAccount(account)
    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)
    suspend fun deleteAccount(id: String) = accountDao.deleteAccountById(id)

    // Category Operations
    suspend fun addCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(id: String) = categoryDao.deleteCategoryById(id)

    // Budget Operations
    suspend fun addBudget(budget: Budget) = budgetDao.insertBudget(budget)
    suspend fun updateBudget(budget: Budget) = budgetDao.updateBudget(budget)
    suspend fun deleteBudget(id: String) = budgetDao.deleteBudgetById(id)

    // Goal Operations
    suspend fun addGoal(goal: FinancialGoal) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: FinancialGoal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(id: String) = goalDao.deleteGoalById(id)
    suspend fun contributeToGoal(id: String, amount: Double, fromAccountId: String?, fromAccountName: String?) {
        goalDao.contributeToGoal(id, amount)
        if (fromAccountId != null && fromAccountName != null) {
            val tx = Transaction(
                type = TransactionType.EXPENSE,
                amount = amount,
                categoryId = "cat_savings",
                categoryName = "Goal Contribution",
                description = "Contribution to goal",
                accountId = fromAccountId,
                accountName = fromAccountName,
                paymentMethod = PaymentMethod.NET_BANKING,
                tags = "Goal,Savings"
            )
            addTransaction(tx)
        }
    }

    // Recurring Operations
    suspend fun addRecurring(recurring: RecurringTransaction) = recurringDao.insertRecurring(recurring)
    suspend fun updateRecurring(recurring: RecurringTransaction) = recurringDao.updateRecurring(recurring)
    suspend fun deleteRecurring(id: String) = recurringDao.deleteRecurringById(id)
    suspend fun executeRecurring(recurring: RecurringTransaction) {
        val tx = Transaction(
            type = recurring.type,
            amount = recurring.amount,
            categoryId = recurring.categoryId,
            categoryName = recurring.categoryName,
            description = recurring.title,
            accountId = recurring.accountId,
            accountName = recurring.accountName,
            tags = "Recurring,Auto"
        )
        addTransaction(tx)

        // Advance next due date
        val cal = Calendar.getInstance().apply { timeInMillis = recurring.nextDueDate }
        when (recurring.frequency) {
            com.example.data.model.RecurrenceFrequency.DAILY -> cal.add(Calendar.DAY_OF_MONTH, 1)
            com.example.data.model.RecurrenceFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            com.example.data.model.RecurrenceFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            com.example.data.model.RecurrenceFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        recurringDao.updateRecurring(recurring.copy(nextDueDate = cal.timeInMillis))
    }

    // Notification Operations
    suspend fun addNotification(item: NotificationItem) = notificationDao.insertNotification(item)
    suspend fun markNotificationRead(id: String) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsRead() = notificationDao.markAllAsRead()
    suspend fun deleteNotification(id: String) = notificationDao.deleteNotificationById(id)
    suspend fun clearNotifications() = notificationDao.clearAllNotifications()

    // Export CSV and JSON
    fun exportToCsv(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Amount,Category,Description,Account,Payment Method,Tags,Notes\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        transactions.forEach { tx ->
            val dateStr = sdf.format(Date(tx.date))
            val cleanDesc = tx.description.replace(",", " ")
            val cleanNotes = tx.notes.replace(",", " ")
            sb.append("${tx.id},$dateStr,${tx.type.name},${tx.amount},${tx.categoryName},\"$cleanDesc\",\"${tx.accountName}\",${tx.paymentMethod.displayName},\"${tx.tags}\",\"$cleanNotes\"\n")
        }
        return sb.toString()
    }

    fun exportToJson(transactions: List<Transaction>, accounts: List<Account>, budgets: List<Budget>, goals: List<FinancialGoal>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val txJson = transactions.joinToString(",") { tx ->
            """{"id":"${tx.id}","type":"${tx.type.name}","amount":${tx.amount},"category":"${tx.categoryName}","description":"${escapeJson(tx.description)}","account":"${escapeJson(tx.accountName)}","date":"${sdf.format(Date(tx.date))}"}"""
        }
        val accJson = accounts.joinToString(",") { acc ->
            """{"id":"${acc.id}","name":"${escapeJson(acc.name)}","type":"${acc.type.name}","balance":${acc.balance}}"""
        }
        return """{"exportDate":"${sdf.format(Date())}","accounts":[$accJson],"transactions":[$txJson]}"""
    }

    suspend fun importCsv(csvContent: String, defaultAccountId: String, defaultAccountName: String): Int {
        var count = 0
        val lines = csvContent.lines()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue
            val tokens = line.split(",")
            if (tokens.size >= 4) {
                try {
                    val date = runCatching { sdf.parse(tokens.getOrNull(1)?.trim() ?: "")?.time }.getOrNull() ?: System.currentTimeMillis()
                    val typeStr = tokens.getOrNull(2)?.trim()?.uppercase() ?: "EXPENSE"
                    val type = runCatching { TransactionType.valueOf(typeStr) }.getOrDefault(TransactionType.EXPENSE)
                    val amount = tokens.getOrNull(3)?.trim()?.toDoubleOrNull() ?: continue
                    val category = tokens.getOrNull(4)?.trim() ?: "General"
                    val desc = tokens.getOrNull(5)?.trim() ?: "Imported transaction"

                    val tx = Transaction(
                        type = type,
                        amount = amount,
                        categoryId = "cat_misc",
                        categoryName = category,
                        description = desc,
                        date = date,
                        accountId = defaultAccountId,
                        accountName = defaultAccountName,
                        tags = "Imported"
                    )
                    addTransaction(tx)
                    count++
                } catch (e: Exception) {
                    // skip malformed line
                }
            }
        }
        return count
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    }

    suspend fun resetDemoData() {
        AppDatabase.populateInitialData(database)
    }
}
