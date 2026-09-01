package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    SAVINGS("Savings Account"),
    CREDIT_CARD("Credit Card"),
    UPI("UPI"),
    INVESTMENT("Investment Account"),
    DIGITAL_WALLET("Digital Wallet")
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AccountType,
    val balance: Double,
    val openingBalance: Double = balance,
    val currency: String = "INR",
    val accountNumberMasked: String = "",
    val colorHex: Long = 0xFF10B981,
    val isActive: Boolean = true
)

enum class CategoryType {
    EXPENSE,
    INCOME
}

enum class CategoryGroup(val title: String) {
    ESSENTIAL("Essential"),
    LIFESTYLE("Lifestyle"),
    OTHER("Other"),
    INCOME("Income")
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: CategoryType = CategoryType.EXPENSE,
    val group: CategoryGroup = CategoryGroup.ESSENTIAL,
    val iconName: String = "category",
    val colorHex: Long = 0xFF3B82F6,
    val isDefault: Boolean = false
)

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    NET_BANKING("Net Banking"),
    CHEQUE("Cheque"),
    OTHER("Other")
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val amount: Double,
    val categoryId: String,
    val categoryName: String,
    val subcategory: String = "",
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val accountId: String,
    val accountName: String,
    val destinationAccountId: String? = null, // For transfers
    val destinationAccountName: String? = null,
    val tags: String = "", // Comma-separated tags
    val notes: String = "",
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BudgetPeriod {
    WEEKLY,
    MONTHLY,
    YEARLY
}

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startDate: Long = System.currentTimeMillis(),
    val colorHex: Long = 0xFF6366F1
)

@Entity(tableName = "financial_goals")
data class FinancialGoal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadlineDate: Long,
    val colorHex: Long = 0xFFEC4899,
    val iconName: String = "savings",
    val category: String = "General",
    val notes: String = "",
    val isCompleted: Boolean = false
)

enum class RecurrenceFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TransactionType = TransactionType.EXPENSE,
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val categoryId: String,
    val categoryName: String,
    val accountId: String,
    val accountName: String,
    val nextDueDate: Long,
    val reminderEnabled: Boolean = true,
    val autoCreate: Boolean = false,
    val isActive: Boolean = true
)

enum class NotificationType {
    BUDGET_WARNING,
    BUDGET_EXCEEDED,
    RECURRING_DUE,
    GOAL_PROGRESS,
    MONTHLY_SUMMARY,
    UNUSUAL_SPENDING
}

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String
)

val SupportedCurrencies = listOf(
    CurrencyInfo("INR", "₹", "Indian Rupee"),
    CurrencyInfo("USD", "$", "US Dollar"),
    CurrencyInfo("EUR", "€", "Euro"),
    CurrencyInfo("GBP", "£", "British Pound"),
    CurrencyInfo("JPY", "¥", "Japanese Yen"),
    CurrencyInfo("CAD", "CA$", "Canadian Dollar"),
    CurrencyInfo("AUD", "AU$", "Australian Dollar"),
    CurrencyInfo("SGD", "S$", "Singapore Dollar"),
    CurrencyInfo("AED", "AED", "UAE Dirham")
)
