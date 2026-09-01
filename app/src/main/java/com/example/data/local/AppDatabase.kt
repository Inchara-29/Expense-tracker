package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Budget
import com.example.data.model.BudgetPeriod
import com.example.data.model.Category
import com.example.data.model.CategoryGroup
import com.example.data.model.CategoryType
import com.example.data.model.FinancialGoal
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.PaymentMethod
import com.example.data.model.RecurrenceFrequency
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class Converters {
    @TypeConverter fun fromAccountType(value: AccountType): String = value.name
    @TypeConverter fun toAccountType(value: String): AccountType = runCatching { AccountType.valueOf(value) }.getOrDefault(AccountType.CASH)

    @TypeConverter fun fromCategoryType(value: CategoryType): String = value.name
    @TypeConverter fun toCategoryType(value: String): CategoryType = runCatching { CategoryType.valueOf(value) }.getOrDefault(CategoryType.EXPENSE)

    @TypeConverter fun fromCategoryGroup(value: CategoryGroup): String = value.name
    @TypeConverter fun toCategoryGroup(value: String): CategoryGroup = runCatching { CategoryGroup.valueOf(value) }.getOrDefault(CategoryGroup.ESSENTIAL)

    @TypeConverter fun fromTransactionType(value: TransactionType): String = value.name
    @TypeConverter fun toTransactionType(value: String): TransactionType = runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.EXPENSE)

    @TypeConverter fun fromPaymentMethod(value: PaymentMethod): String = value.name
    @TypeConverter fun toPaymentMethod(value: String): PaymentMethod = runCatching { PaymentMethod.valueOf(value) }.getOrDefault(PaymentMethod.UPI)

    @TypeConverter fun fromBudgetPeriod(value: BudgetPeriod): String = value.name
    @TypeConverter fun toBudgetPeriod(value: String): BudgetPeriod = runCatching { BudgetPeriod.valueOf(value) }.getOrDefault(BudgetPeriod.MONTHLY)

    @TypeConverter fun fromRecurrenceFrequency(value: RecurrenceFrequency): String = value.name
    @TypeConverter fun toRecurrenceFrequency(value: String): RecurrenceFrequency = runCatching { RecurrenceFrequency.valueOf(value) }.getOrDefault(RecurrenceFrequency.MONTHLY)

    @TypeConverter fun fromNotificationType(value: NotificationType): String = value.name
    @TypeConverter fun toNotificationType(value: String): NotificationType = runCatching { NotificationType.valueOf(value) }.getOrDefault(NotificationType.BUDGET_WARNING)
}

@Database(
    entities = [
        Account::class,
        Category::class,
        Transaction::class,
        Budget::class,
        FinancialGoal::class,
        RecurringTransaction::class,
        NotificationItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun recurringDao(): RecurringDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val accountDao = database.accountDao()
            val categoryDao = database.categoryDao()
            val budgetDao = database.budgetDao()
            val goalDao = database.goalDao()
            val recurringDao = database.recurringDao()
            val transactionDao = database.transactionDao()
            val notificationDao = database.notificationDao()

            // Initial Default Categories
            val defaultCategories = listOf(
                // Essential Expenses
                Category("cat_food", "Food", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "restaurant", 0xFFEF4444, true),
                Category("cat_groceries", "Groceries", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "shopping_cart", 0xFFF97316, true),
                Category("cat_rent", "Rent", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "home", 0xFF8B5CF6, true),
                Category("cat_electricity", "Electricity", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "bolt", 0xFFEAB308, true),
                Category("cat_water", "Water", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "water_drop", 0xFF06B6D4, true),
                Category("cat_internet", "Internet", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "wifi", 0xFF3B82F6, true),
                Category("cat_transport", "Transportation", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "directions_car", 0xFF10B981, true),
                Category("cat_health", "Healthcare", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "medical_services", 0xFFEC4899, true),
                Category("cat_education", "Education", CategoryType.EXPENSE, CategoryGroup.ESSENTIAL, "school", 0xFF6366F1, true),

                // Lifestyle Expenses
                Category("cat_shopping", "Shopping", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, "shopping_bag", 0xFFA855F7, true),
                Category("cat_entertainment", "Entertainment", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, "movie", 0xFFD946EF, true),
                Category("cat_travel", "Travel", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, "flight", 0xFF14B8A6, true),
                Category("cat_restaurants", "Restaurants", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, "local_dining", 0xFFF43F5E, true),
                Category("cat_subscriptions", "Subscriptions", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, "subscriptions", 0xFF64748B, true),
                Category("cat_hobbies", "Hobbies", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, "palette", 0xFFF59E0B, true),

                // Other Expenses
                Category("cat_gifts", "Gifts", CategoryType.EXPENSE, CategoryGroup.OTHER, "card_giftcard", 0xFFEC4899, true),
                Category("cat_taxes", "Taxes", CategoryType.EXPENSE, CategoryGroup.OTHER, "account_balance", 0xFF64748B, true),
                Category("cat_insurance", "Insurance", CategoryType.EXPENSE, CategoryGroup.OTHER, "security", 0xFF0284C7, true),
                Category("cat_investments", "Investments", CategoryType.EXPENSE, CategoryGroup.OTHER, "trending_up", 0xFF059669, true),
                Category("cat_misc", "Miscellaneous", CategoryType.EXPENSE, CategoryGroup.OTHER, "more_horiz", 0xFF71717A, true),

                // Income Categories
                Category("cat_salary", "Salary", CategoryType.INCOME, CategoryGroup.INCOME, "payments", 0xFF10B981, true),
                Category("cat_freelance", "Freelancing", CategoryType.INCOME, CategoryGroup.INCOME, "laptop_mac", 0xFF3B82F6, true),
                Category("cat_business", "Business", CategoryType.INCOME, CategoryGroup.INCOME, "store", 0xFF8B5CF6, true),
                Category("cat_investment_inc", "Investments", CategoryType.INCOME, CategoryGroup.INCOME, "show_chart", 0xFF059669, true),
                Category("cat_rental_inc", "Rental Income", CategoryType.INCOME, CategoryGroup.INCOME, "apartment", 0xFFD97706, true),
                Category("cat_interest", "Interest", CategoryType.INCOME, CategoryGroup.INCOME, "savings", 0xFF0D9488, true),
                Category("cat_bonuses", "Bonuses", CategoryType.INCOME, CategoryGroup.INCOME, "star", 0xFFF59E0B, true),
                Category("cat_other_income", "Other Income", CategoryType.INCOME, CategoryGroup.INCOME, "attach_money", 0xFF64748B, true)
            )
            categoryDao.insertCategories(defaultCategories)

            // Initial Default Accounts
            val defaultAccounts = listOf(
                Account("acc_bank", "HDFC Main Bank", AccountType.BANK, 45000.0, 45000.0, "INR", "•••• 8912", 0xFF2563EB),
                Account("acc_savings", "High Yield Savings", AccountType.SAVINGS, 85000.0, 85000.0, "INR", "•••• 4420", 0xFF059669),
                Account("acc_upi", "Google Pay UPI", AccountType.UPI, 8400.0, 8400.0, "INR", "user@upi", 0xFF7C3AED),
                Account("acc_cash", "Wallet Cash", AccountType.CASH, 3200.0, 3200.0, "INR", "", 0xFFD97706),
                Account("acc_credit", "ICICI Platinum Credit", AccountType.CREDIT_CARD, -4500.0, -4500.0, "INR", "•••• 1098", 0xFFDC2626),
                Account("acc_invest", "Zerodha Portfolio", AccountType.INVESTMENT, 120000.0, 120000.0, "INR", "•••• 5501", 0xFF0D9488)
            )
            accountDao.insertAccounts(defaultAccounts)

            // Initial Sample Budgets
            val defaultBudgets = listOf(
                Budget("bgt_food", "cat_food", "Food", 8000.0, BudgetPeriod.MONTHLY, System.currentTimeMillis(), 0xFFEF4444),
                Budget("bgt_shopping", "cat_shopping", "Shopping", 5000.0, BudgetPeriod.MONTHLY, System.currentTimeMillis(), 0xFFA855F7),
                Budget("bgt_transport", "cat_transport", "Transportation", 3000.0, BudgetPeriod.MONTHLY, System.currentTimeMillis(), 0xFF10B981),
                Budget("bgt_entertainment", "cat_entertainment", "Entertainment", 2000.0, BudgetPeriod.MONTHLY, System.currentTimeMillis(), 0xFFD946EF)
            )
            budgetDao.insertBudgets(defaultBudgets)

            // Initial Sample Goals
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 4)
            val deadlineLaptop = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 8)
            val deadlineEmergency = calendar.timeInMillis

            val defaultGoals = listOf(
                FinancialGoal("goal_laptop", "Buy MacBook Pro", 80000.0, 35000.0, deadlineLaptop, 0xFF3B82F6, "laptop", "Tech", "Upgrading work machine"),
                FinancialGoal("goal_emergency", "Emergency Fund", 150000.0, 90000.0, deadlineEmergency, 0xFF10B981, "security", "Safety", "6 months living expenses reserve"),
                FinancialGoal("goal_trip", "Goa Vacation Trip", 25000.0, 18000.0, System.currentTimeMillis() + 30L * 86400000L, 0xFFF59E0B, "flight", "Travel", "Weekend getaway with friends")
            )
            goalDao.insertGoals(defaultGoals)

            // Initial Sample Recurring Items
            val nextMonthCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }
            val defaultRecurring = listOf(
                RecurringTransaction("rec_salary", "Monthly Company Salary", 65000.0, TransactionType.INCOME, RecurrenceFrequency.MONTHLY, System.currentTimeMillis(), null, "cat_salary", "Salary", "acc_bank", "HDFC Main Bank", nextMonthCal.timeInMillis, true, true),
                RecurringTransaction("rec_rent", "Apartment Rent", 16000.0, TransactionType.EXPENSE, RecurrenceFrequency.MONTHLY, System.currentTimeMillis(), null, "cat_rent", "Rent", "acc_bank", "HDFC Main Bank", nextMonthCal.timeInMillis, true, false),
                RecurringTransaction("rec_netflix", "Netflix 4K Ultra", 649.0, TransactionType.EXPENSE, RecurrenceFrequency.MONTHLY, System.currentTimeMillis(), null, "cat_subscriptions", "Subscriptions", "acc_credit", "ICICI Platinum Credit", nextMonthCal.timeInMillis + 86400000L * 2, true, false),
                RecurringTransaction("rec_wifi", "Fiber Broadband Bill", 999.0, TransactionType.EXPENSE, RecurrenceFrequency.MONTHLY, System.currentTimeMillis(), null, "cat_internet", "Internet", "acc_upi", "Google Pay UPI", nextMonthCal.timeInMillis + 86400000L * 4, true, false)
            )
            recurringDao.insertAllRecurring(defaultRecurring)

            // Initial Sample Transactions (Recent this month)
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            val sampleTransactions = listOf(
                Transaction("tx_1", TransactionType.INCOME, 65000.0, "cat_salary", "Salary", "Direct Deposit", "Monthly tech lead compensation", now - dayMs * 18, PaymentMethod.NET_BANKING, "acc_bank", "HDFC Main Bank", tags = "Salary,Job", notes = "After standard TDS deduction"),
                Transaction("tx_2", TransactionType.EXPENSE, 16000.0, "cat_rent", "Rent", "Flat #402", "August apartment rent payment", now - dayMs * 17, PaymentMethod.NET_BANKING, "acc_bank", "HDFC Main Bank", tags = "Housing,Fixed"),
                Transaction("tx_3", TransactionType.EXPENSE, 4200.0, "cat_groceries", "Groceries", "Organic Supermarket", "Weekly organic vegetables, fruits & dairy supplies", now - dayMs * 12, PaymentMethod.UPI, "acc_upi", "Google Pay UPI", tags = "Essential,Food"),
                Transaction("tx_4", TransactionType.EXPENSE, 1850.0, "cat_food", "Food", "Dinner", "Italian Bistro dinner with team", now - dayMs * 9, PaymentMethod.CREDIT_CARD, "acc_credit", "ICICI Platinum Credit", tags = "Dining"),
                Transaction("tx_5", TransactionType.EXPENSE, 2600.0, "cat_shopping", "Shopping", "Apparel", "Sportswear running shoes & socks", now - dayMs * 6, PaymentMethod.CREDIT_CARD, "acc_credit", "ICICI Platinum Credit", tags = "Shopping,Fitness"),
                Transaction("tx_6", TransactionType.EXPENSE, 850.0, "cat_transport", "Transportation", "Fuel", "City car petrol refill", now - dayMs * 4, PaymentMethod.UPI, "acc_upi", "Google Pay UPI", tags = "Fuel,Commute"),
                Transaction("tx_7", TransactionType.INCOME, 8500.0, "cat_freelance", "Freelancing", "Mobile UI Design", "Consulting UI kit delivery milestone", now - dayMs * 2, PaymentMethod.UPI, "acc_upi", "Google Pay UPI", tags = "Freelance,Design"),
                Transaction("tx_8", TransactionType.EXPENSE, 649.0, "cat_subscriptions", "Subscriptions", "Streaming", "Netflix 4K Monthly auto-debit", now - dayMs * 1, PaymentMethod.CREDIT_CARD, "acc_credit", "ICICI Platinum Credit", tags = "Entertainment"),
                Transaction("tx_9", TransactionType.TRANSFER, 5000.0, "cat_misc", "Transfer", "", "Monthly emergency savings deposit", now - dayMs * 1, PaymentMethod.NET_BANKING, "acc_bank", "HDFC Main Bank", "acc_savings", "High Yield Savings", tags = "Savings")
            )
            transactionDao.insertTransactions(sampleTransactions)

            // Notifications
            val sampleNotifications = listOf(
                NotificationItem("notif_1", "Budget Alert", "You have used 72% of your Food & Dining budget this month.", NotificationType.BUDGET_WARNING, now - dayMs * 2, false),
                NotificationItem("notif_2", "Recurring Bill Due", "Fiber Broadband Bill of ₹999 is due in 4 days.", NotificationType.RECURRING_DUE, now - dayMs * 1, false),
                NotificationItem("notif_3", "Goal Milestone", "MacBook Pro goal reached 43% target milestone!", NotificationType.GOAL_PROGRESS, now - dayMs * 3, true)
            )
            sampleNotifications.forEach { notificationDao.insertNotification(it) }
        }
    }
}
