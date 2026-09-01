package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.CurrencyInfo
import com.example.data.model.FinancialGoal
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.PaymentMethod
import com.example.data.model.RecurringTransaction
import com.example.data.model.SupportedCurrencies
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.repository.AiAdvisorService
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.SpendingInsight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilter(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today")
}

enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST("Highest Amount"),
    LOWEST("Lowest Amount"),
    CATEGORY("Category A-Z")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FilterState(
    val query: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val typeFilter: TransactionType? = null,
    val categoryFilter: String? = null,
    val accountFilter: String? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = ExpenseRepository(database)
    private val aiService = AiAdvisorService()

    // Room DB Flow streams
    val accounts: StateFlow<List<Account>> = repository.activeAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<FinancialGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTransactions: StateFlow<List<RecurringTransaction>> = repository.allRecurring
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // User Preferences & Settings State
    private val _selectedCurrency = MutableStateFlow(SupportedCurrencies.first())
    val selectedCurrency: StateFlow<CurrencyInfo> = _selectedCurrency.asStateFlow()

    private val _userName = MutableStateFlow("Alex Morgan")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("alex.morgan@example.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _securityPin = MutableStateFlow("1234")
    val securityPin: StateFlow<String> = _securityPin.asStateFlow()

    // Filter & Search State
    val searchQuery = MutableStateFlow("")
    val dateFilter = MutableStateFlow(DateFilter.ALL)
    val typeFilter = MutableStateFlow<TransactionType?>(null)
    val categoryFilter = MutableStateFlow<String?>(null)
    val accountFilter = MutableStateFlow<String?>(null)
    val sortOrder = MutableStateFlow(SortOrder.NEWEST)

    // Bulk actions
    private val _selectedTxIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTxIds: StateFlow<Set<String>> = _selectedTxIds.asStateFlow()

    // AI Chat State
    private val _aiChatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "Hello! I am your AI Wealth & Expense Advisor. Ask me anything about your spending habits, category breakdowns, savings opportunities, or budget recommendations!"
            )
        )
    )
    val aiChatMessages: StateFlow<List<ChatMessage>> = _aiChatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Filter State Flow
    private val filterStateFlow = combine(
        searchQuery,
        dateFilter,
        typeFilter,
        categoryFilter
    ) { q, df, tf, cf ->
        FilterState(query = q, dateFilter = df, typeFilter = tf, categoryFilter = cf)
    }

    private val extraFilterStateFlow = combine(
        filterStateFlow,
        accountFilter,
        sortOrder
    ) { fs, af, so ->
        fs.copy(accountFilter = af, sortOrder = so)
    }

    // Filtered Transactions Combined Flow
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        rawTransactions,
        extraFilterStateFlow
    ) { txs, fs ->
        var list = txs

        // 1. Search Query
        if (fs.query.isNotBlank()) {
            val q = fs.query.trim().lowercase()
            list = list.filter {
                it.description.lowercase().contains(q) ||
                it.categoryName.lowercase().contains(q) ||
                it.accountName.lowercase().contains(q) ||
                it.tags.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.amount.toString().contains(q)
            }
        }

        // 2. Date Filter
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        list = when (fs.dateFilter) {
            DateFilter.ALL -> list
            DateFilter.TODAY -> list.filter { it.date >= startOfToday }
            DateFilter.THIS_WEEK -> list.filter { it.date >= startOfWeek }
            DateFilter.THIS_MONTH -> list.filter { it.date >= startOfMonth }
        }

        // 3. Type Filter
        if (fs.typeFilter != null) {
            list = list.filter { it.type == fs.typeFilter }
        }

        // 4. Category Filter
        if (fs.categoryFilter != null) {
            list = list.filter { it.categoryId == fs.categoryFilter || it.categoryName == fs.categoryFilter }
        }

        // 5. Account Filter
        if (fs.accountFilter != null) {
            list = list.filter { it.accountId == fs.accountFilter || it.destinationAccountId == fs.accountFilter }
        }

        // 6. Sort
        when (fs.sortOrder) {
            SortOrder.NEWEST -> list.sortedByDescending { it.date }
            SortOrder.OLDEST -> list.sortedBy { it.date }
            SortOrder.HIGHEST -> list.sortedByDescending { it.amount }
            SortOrder.LOWEST -> list.sortedBy { it.amount }
            SortOrder.CATEGORY -> list.sortedBy { it.categoryName }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aggregates & Analytics
    fun getTotalBalance(accountsList: List<Account>): Double {
        return accountsList.sumOf { it.balance }
    }

    fun getThisMonthIncome(transactions: List<Transaction>): Double {
        val startOfMonth = getStartOfMonthTimestamp()
        return transactions.filter { it.type == TransactionType.INCOME && it.date >= startOfMonth }.sumOf { it.amount }
    }

    fun getThisMonthExpense(transactions: List<Transaction>): Double {
        val startOfMonth = getStartOfMonthTimestamp()
        return transactions.filter { it.type == TransactionType.EXPENSE && it.date >= startOfMonth }.sumOf { it.amount }
    }

    fun getSavingsRate(income: Double, expense: Double): Double {
        return if (income > 0) ((income - expense) / income * 100.0).coerceAtLeast(0.0) else 0.0
    }

    fun getCategorySpendingMap(transactions: List<Transaction>): Map<String, Double> {
        val startOfMonth = getStartOfMonthTimestamp()
        return transactions.filter { it.type == TransactionType.EXPENSE && it.date >= startOfMonth }
            .groupBy { it.categoryName }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
    }

    fun getRecentDailySpending(transactions: List<Transaction>): List<Pair<String, Double>> {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val result = mutableListOf<Pair<String, Double>>()

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startDay = dayCal.timeInMillis
            val endDay = startDay + 86400000L
            val dayLabel = if (i == 0) "Today" else dayFormat.format(Date(startDay))

            val spent = transactions.filter { it.type == TransactionType.EXPENSE && it.date in startDay until endDay }
                .sumOf { it.amount }
            result.add(Pair(dayLabel, spent))
        }
        return result
    }

    fun getSpendingInsight(transactions: List<Transaction>, budgets: List<Budget>, goals: List<FinancialGoal>): SpendingInsight {
        return aiService.generateLocalAnalytics(transactions, budgets, goals, _selectedCurrency.value.symbol)
    }

    // CRUD Transactions
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
            checkBudgetAlerts(transaction)
        }
    }

    fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(oldTransaction, newTransaction)
            checkBudgetAlerts(newTransaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun duplicateTransaction(transaction: Transaction) {
        val copy = transaction.copy(
            id = java.util.UUID.randomUUID().toString(),
            date = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        addTransaction(copy)
    }

    // Bulk Actions
    fun toggleTxSelection(id: String) {
        val current = _selectedTxIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedTxIds.value = current
    }

    fun clearTxSelection() {
        _selectedTxIds.value = emptySet()
    }

    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selected = _selectedTxIds.value
            val txsToDelete = rawTransactions.value.filter { selected.contains(it.id) }
            repository.bulkDeleteTransactions(txsToDelete)
            clearTxSelection()
        }
    }

    fun bulkCategorizeSelected(newCategory: Category) {
        viewModelScope.launch {
            val selected = _selectedTxIds.value.toList()
            repository.bulkUpdateCategory(selected, newCategory.id, newCategory.name)
            clearTxSelection()
        }
    }

    // CRUD Accounts
    fun addAccount(account: Account) = viewModelScope.launch { repository.addAccount(account) }
    fun updateAccount(account: Account) = viewModelScope.launch { repository.updateAccount(account) }
    fun deleteAccount(id: String) = viewModelScope.launch { repository.deleteAccount(id) }

    fun transferMoney(fromAccount: Account, toAccount: Account, amount: Double, note: String) {
        viewModelScope.launch {
            val tx = Transaction(
                type = TransactionType.TRANSFER,
                amount = amount,
                categoryId = "cat_transfer",
                categoryName = "Transfer",
                description = note,
                accountId = fromAccount.id,
                accountName = fromAccount.name,
                destinationAccountId = toAccount.id,
                destinationAccountName = toAccount.name,
                paymentMethod = PaymentMethod.NET_BANKING,
                tags = "Transfer"
            )
            repository.addTransaction(tx)
        }
    }

    // CRUD Budgets
    fun addBudget(budget: Budget) = viewModelScope.launch { repository.addBudget(budget) }
    fun updateBudget(budget: Budget) = viewModelScope.launch { repository.updateBudget(budget) }
    fun deleteBudget(id: String) = viewModelScope.launch { repository.deleteBudget(id) }

    // CRUD Goals
    fun addGoal(goal: FinancialGoal) = viewModelScope.launch { repository.addGoal(goal) }
    fun updateGoal(goal: FinancialGoal) = viewModelScope.launch { repository.updateGoal(goal) }
    fun deleteGoal(id: String) = viewModelScope.launch { repository.deleteGoal(id) }
    fun contributeToGoal(goal: FinancialGoal, amount: Double, fromAccount: Account) {
        viewModelScope.launch {
            repository.contributeToGoal(goal.id, amount, fromAccount.id, fromAccount.name)
            if (goal.currentAmount + amount >= goal.targetAmount) {
                repository.addNotification(
                    NotificationItem(
                        title = "🎉 Goal Achieved!",
                        message = "Congratulations! You reached 100% of your target for '${goal.title}'!",
                        type = NotificationType.GOAL_PROGRESS
                    )
                )
            }
        }
    }

    // CRUD Recurring
    fun addRecurring(recurring: RecurringTransaction) = viewModelScope.launch { repository.addRecurring(recurring) }
    fun executeRecurring(recurring: RecurringTransaction) = viewModelScope.launch { repository.executeRecurring(recurring) }
    fun deleteRecurring(id: String) = viewModelScope.launch { repository.deleteRecurring(id) }

    // Notifications
    fun markNotificationRead(id: String) = viewModelScope.launch { repository.markNotificationRead(id) }
    fun markAllNotificationsRead() = viewModelScope.launch { repository.markAllNotificationsRead() }
    fun clearNotifications() = viewModelScope.launch { repository.clearNotifications() }

    // AI Advisor Query
    fun sendAiQuestion(question: String) {
        if (question.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = question)
        _aiChatMessages.value = _aiChatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val response = aiService.askAiAdvisor(
                userQuestion = question,
                transactions = rawTransactions.value,
                accounts = accounts.value,
                budgets = budgets.value,
                goals = goals.value,
                currencySymbol = _selectedCurrency.value.symbol
            )
            _aiChatMessages.value = _aiChatMessages.value + ChatMessage(sender = "ai", text = response)
            _isAiLoading.value = false
        }
    }

    // Settings
    fun setCurrency(currencyInfo: CurrencyInfo) {
        _selectedCurrency.value = currencyInfo
    }

    fun updateUserProfile(name: String, email: String) {
        _userName.value = name
        _userEmail.value = email
    }

    fun setSecurityPin(pin: String) {
        _securityPin.value = pin
    }

    fun setAppLocked(locked: Boolean) {
        _isAppLocked.value = locked
    }

    // Export & Import
    fun getCsvExportData(): String = repository.exportToCsv(rawTransactions.value)
    fun getJsonExportData(): String = repository.exportToJson(rawTransactions.value, accounts.value, budgets.value, goals.value)
    fun importCsv(csvString: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val defaultAcc = accounts.value.firstOrNull() ?: Account("acc_1", "Main Account", AccountType.BANK, 0.0)
            val count = repository.importCsv(csvString, defaultAcc.id, defaultAcc.name)
            onComplete(count)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
        }
    }

    private fun checkBudgetAlerts(tx: Transaction) {
        if (tx.type != TransactionType.EXPENSE) return
        val currentBudgets = budgets.value
        val txs = rawTransactions.value
        val startOfMonth = getStartOfMonthTimestamp()

        currentBudgets.forEach { bgt ->
            if (bgt.categoryId == tx.categoryId || bgt.categoryName.equals(tx.categoryName, ignoreCase = true)) {
                val spent = txs.filter { it.type == TransactionType.EXPENSE && (it.categoryId == bgt.categoryId || it.categoryName.equals(bgt.categoryName, ignoreCase = true)) && it.date >= startOfMonth }
                    .sumOf { it.amount } + tx.amount
                val ratio = if (bgt.amount > 0) spent / bgt.amount else 0.0

                if (ratio >= 1.0) {
                    viewModelScope.launch {
                        repository.addNotification(
                            NotificationItem(
                                title = "⚠️ Budget Limit Exceeded",
                                message = "You have exceeded your ${bgt.categoryName} monthly budget limit (${_selectedCurrency.value.symbol}${String.format(Locale.getDefault(), "%,.0f", bgt.amount)}).",
                                type = NotificationType.BUDGET_EXCEEDED
                            )
                        )
                    }
                } else if (ratio >= 0.85) {
                    viewModelScope.launch {
                        repository.addNotification(
                            NotificationItem(
                                title = "⚡ Budget Warning (85%+)",
                                message = "You have spent ${(ratio * 100).toInt()}% of your ${bgt.categoryName} monthly budget.",
                                type = NotificationType.BUDGET_WARNING
                            )
                        )
                    }
                }
            }
        }
    }

    private fun getStartOfMonthTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
