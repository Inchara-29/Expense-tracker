package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.FinancialGoal
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.ui.components.AddEditAccountDialog
import com.example.ui.components.AddEditBudgetDialog
import com.example.ui.components.AddEditGoalDialog
import com.example.ui.components.AddEditRecurringDialog
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.components.DepositGoalDialog
import com.example.ui.components.TransferMoneyDialog
import com.example.ui.screens.AiAdvisorChatScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsAndGoalsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedOnPrimary
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedPrimaryContainer
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.ExpenseViewModel

sealed class Screen(val title: String, val icon: ImageVector) {
    object Dashboard : Screen("Dashboard", Icons.Default.Dashboard)
    object Transactions : Screen("Transactions", Icons.Default.ReceiptLong)
    object Budgets : Screen("Budgets", Icons.Default.PieChart)
    object AiAdvisor : Screen("AI Coach", Icons.Default.AutoAwesome)
    object Analytics : Screen("Analytics", Icons.Default.Analytics)
    object Settings : Screen("Settings", Icons.Default.Settings)
    object Notifications : Screen("Notifications", Icons.Default.Dashboard)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ExpenseViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()
    var isUnlockedForSession by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    // Dialog States
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedTransactionForEdit by remember { mutableStateOf<Transaction?>(null) }

    var showAccountDialog by remember { mutableStateOf(false) }
    var selectedAccountForEdit by remember { mutableStateOf<Account?>(null) }

    var showTransferDialog by remember { mutableStateOf(false) }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedBudgetForEdit by remember { mutableStateOf<Budget?>(null) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForEdit by remember { mutableStateOf<FinancialGoal?>(null) }

    var showDepositGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<FinancialGoal?>(null) }

    var showRecurringDialog by remember { mutableStateOf(false) }
    var selectedRecurringForEdit by remember { mutableStateOf<RecurringTransaction?>(null) }

    // PIN Lock Screen Protection
    if (isAppLocked && !isUnlockedForSession) {
        PinLockScreen(
            correctPin = securityPin,
            onUnlock = { isUnlockedForSession = true }
        )
        return
    }

    val navItems = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Budgets,
        Screen.AiAdvisor,
        Screen.Analytics,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SophisticatedPrimaryContainer,
                            selectedTextColor = SophisticatedTextPrimary,
                            unselectedIconColor = SophisticatedTextSecondary.copy(alpha = 0.7f),
                            unselectedTextColor = SophisticatedTextSecondary.copy(alpha = 0.7f),
                            indicatorColor = SophisticatedBorder
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.title.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == Screen.Dashboard || currentScreen == Screen.Transactions) {
                FloatingActionButton(
                    onClick = {
                        selectedTransactionForEdit = null
                        showTransactionDialog = true
                    },
                    containerColor = SophisticatedPrimary,
                    contentColor = SophisticatedOnPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_transaction")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentScreen = Screen.Transactions },
                        onNavigateToBudgets = { currentScreen = Screen.Budgets },
                        onNavigateToGoals = { currentScreen = Screen.Budgets },
                        onNavigateToAi = { currentScreen = Screen.AiAdvisor },
                        onNavigateToNotifications = { currentScreen = Screen.Notifications },
                        onAddTransactionClick = {
                            selectedTransactionForEdit = null
                            showTransactionDialog = true
                        },
                        onTransferClick = { showTransferDialog = true },
                        onAddAccountClick = {
                            selectedAccountForEdit = null
                            showAccountDialog = true
                        },
                        onDepositGoalClick = { goal ->
                            selectedGoalForDeposit = goal
                            showDepositGoalDialog = true
                        },
                        onTransactionClick = { tx ->
                            selectedTransactionForEdit = tx
                            showTransactionDialog = true
                        }
                    )
                }
                Screen.Transactions -> {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onAddTransactionClick = {
                            selectedTransactionForEdit = null
                            showTransactionDialog = true
                        },
                        onEditTransaction = { tx ->
                            selectedTransactionForEdit = tx
                            showTransactionDialog = true
                        }
                    )
                }
                Screen.Budgets -> {
                    BudgetsAndGoalsScreen(
                        viewModel = viewModel,
                        onAddBudgetClick = {
                            selectedBudgetForEdit = null
                            showBudgetDialog = true
                        },
                        onEditBudgetClick = { bgt ->
                            selectedBudgetForEdit = bgt
                            showBudgetDialog = true
                        },
                        onAddGoalClick = {
                            selectedGoalForEdit = null
                            showGoalDialog = true
                        },
                        onDepositGoalClick = { goal ->
                            selectedGoalForDeposit = goal
                            showDepositGoalDialog = true
                        },
                        onAddRecurringClick = {
                            selectedRecurringForEdit = null
                            showRecurringDialog = true
                        }
                    )
                }
                Screen.AiAdvisor -> {
                    AiAdvisorChatScreen(viewModel = viewModel)
                }
                Screen.Analytics -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
                Screen.Settings -> {
                    SettingsScreen(viewModel = viewModel)
                }
                Screen.Notifications -> {
                    NotificationsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Interactive Dialogs
    if (showTransactionDialog) {
        AddEditTransactionDialog(
            initialTransaction = selectedTransactionForEdit,
            categories = categories,
            accounts = accounts,
            currencySymbol = currency.symbol,
            onDismiss = {
                showTransactionDialog = false
                selectedTransactionForEdit = null
            },
            onSave = { tx ->
                if (selectedTransactionForEdit == null) {
                    viewModel.addTransaction(tx)
                } else {
                    viewModel.updateTransaction(selectedTransactionForEdit!!, tx)
                }
                showTransactionDialog = false
                selectedTransactionForEdit = null
            },
            onDelete = { tx ->
                viewModel.deleteTransaction(tx)
                showTransactionDialog = false
                selectedTransactionForEdit = null
            }
        )
    }

    if (showAccountDialog) {
        AddEditAccountDialog(
            initialAccount = selectedAccountForEdit,
            currencySymbol = currency.symbol,
            onDismiss = {
                showAccountDialog = false
                selectedAccountForEdit = null
            },
            onSave = { acc ->
                if (selectedAccountForEdit == null) {
                    viewModel.addAccount(acc)
                } else {
                    viewModel.updateAccount(acc)
                }
                showAccountDialog = false
                selectedAccountForEdit = null
            }
        )
    }

    if (showTransferDialog) {
        TransferMoneyDialog(
            accounts = accounts,
            currencySymbol = currency.symbol,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromAcc, toAcc, amt, note ->
                viewModel.transferMoney(fromAcc, toAcc, amt, note)
                showTransferDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        AddEditBudgetDialog(
            initialBudget = selectedBudgetForEdit,
            categories = categories,
            currencySymbol = currency.symbol,
            onDismiss = {
                showBudgetDialog = false
                selectedBudgetForEdit = null
            },
            onSave = { bgt ->
                if (selectedBudgetForEdit == null) {
                    viewModel.addBudget(bgt)
                } else {
                    viewModel.updateBudget(bgt)
                }
                showBudgetDialog = false
                selectedBudgetForEdit = null
            }
        )
    }

    if (showGoalDialog) {
        AddEditGoalDialog(
            initialGoal = selectedGoalForEdit,
            currencySymbol = currency.symbol,
            onDismiss = {
                showGoalDialog = false
                selectedGoalForEdit = null
            },
            onSave = { goal ->
                if (selectedGoalForEdit == null) {
                    viewModel.addGoal(goal)
                } else {
                    viewModel.updateGoal(goal)
                }
                showGoalDialog = false
                selectedGoalForEdit = null
            }
        )
    }

    if (showDepositGoalDialog && selectedGoalForDeposit != null) {
        DepositGoalDialog(
            goal = selectedGoalForDeposit!!,
            accounts = accounts,
            currencySymbol = currency.symbol,
            onDismiss = {
                showDepositGoalDialog = false
                selectedGoalForDeposit = null
            },
            onDeposit = { amount, fromAccount ->
                viewModel.contributeToGoal(selectedGoalForDeposit!!, amount, fromAccount)
                showDepositGoalDialog = false
                selectedGoalForDeposit = null
            }
        )
    }

    if (showRecurringDialog) {
        AddEditRecurringDialog(
            initialRecurring = selectedRecurringForEdit,
            categories = categories,
            accounts = accounts,
            currencySymbol = currency.symbol,
            onDismiss = {
                showRecurringDialog = false
                selectedRecurringForEdit = null
            },
            onSave = { rec ->
                if (selectedRecurringForEdit == null) {
                    viewModel.addRecurring(rec)
                }
                showRecurringDialog = false
                selectedRecurringForEdit = null
            }
        )
    }
}

@Composable
fun PinLockScreen(
    correctPin: String,
    onUnlock: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pin_lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryEmerald.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Expense Tracker Locked",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Enter 4-digit PIN to access your finances",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(30.dp))

            // PIN Dots Indicator
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (isError) DangerCoral else if (isFilled) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (isError) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Incorrect PIN. Try again.", style = MaterialTheme.typography.labelSmall, color = DangerCoral)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Keypad Numbers
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            keypad.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(64.dp))
                        } else if (key == "DEL") {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                            isError = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Backspace, contentDescription = "Delete", modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (enteredPin.length < 4) {
                                            val newPin = enteredPin + key
                                            enteredPin = newPin
                                            isError = false
                                            if (newPin.length == 4) {
                                                if (newPin == correctPin) {
                                                    onUnlock()
                                                } else {
                                                    isError = true
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = { onUnlock() }) {
                Text("Bypass PIN (Demo Mode)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
