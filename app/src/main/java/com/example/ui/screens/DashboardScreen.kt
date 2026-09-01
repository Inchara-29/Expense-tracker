package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.FinancialGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.AccountCard
import com.example.ui.components.BudgetStatusCard
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.GoalProgressCard
import com.example.ui.components.MainBalanceCard
import com.example.ui.components.SimpleSpendingBarChart
import com.example.ui.components.TransactionItemRow
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedNegativeRose
import com.example.ui.theme.SophisticatedOnPrimary
import com.example.ui.theme.SophisticatedPositiveMint
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onTransferClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onDepositGoalClick: (FinancialGoal) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.rawTransactions.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val recurring by viewModel.recurringTransactions.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()

    val totalBalance = viewModel.getTotalBalance(accounts)
    val thisMonthIncome = viewModel.getThisMonthIncome(transactions)
    val thisMonthExpense = viewModel.getThisMonthExpense(transactions)
    val savingsRate = viewModel.getSavingsRate(thisMonthIncome, thisMonthExpense)
    val categorySpending = viewModel.getCategorySpendingMap(transactions)
    val weeklySpending = viewModel.getRecentDailySpending(transactions)
    val insight = viewModel.getSpendingInsight(transactions, budgets, goals)

    // Calculate summary statistics for Quick Stats widgets
    val totalBudgetAmount = budgets.sumOf { it.amount }
    val totalBudgetSpent = transactions.filter { tx ->
        tx.type == TransactionType.EXPENSE && budgets.any { b -> b.categoryId == tx.categoryId || b.categoryName.equals(tx.categoryName, ignoreCase = true) }
    }.sumOf { it.amount }
    val budgetProgress = if (totalBudgetAmount > 0) (totalBudgetSpent / totalBudgetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val budgetRemaining = (totalBudgetAmount - totalBudgetSpent).coerceAtLeast(0.0)

    val totalGoalsTarget = goals.sumOf { it.targetAmount }
    val totalGoalsSaved = goals.sumOf { it.currentAmount }
    val goalsProgress = if (totalGoalsTarget > 0) (totalGoalsSaved / totalGoalsTarget).toFloat().coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GOOD MORNING,",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = SophisticatedTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = userName.ifBlank { "Alex Rivera" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SophisticatedTextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.testTag("notif_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(
                                        containerColor = SophisticatedPrimary,
                                        contentColor = SophisticatedOnPrimary
                                    ) { Text("$unreadNotifs") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = SophisticatedTextPrimary
                            )
                        }
                    }

                    // Avatar Circle with gradient accent border
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.5.dp, SophisticatedPrimary, CircleShape)
                            .clip(CircleShape)
                            .background(SophisticatedSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userName.take(1).ifBlank { "A" }).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedPrimary
                        )
                    }
                }
            }
        }

        // Main Financial Overview Card
        item {
            MainBalanceCard(
                totalBalance = totalBalance,
                thisMonthIncome = thisMonthIncome,
                thisMonthExpense = thisMonthExpense,
                savingsRate = savingsRate,
                currencySymbol = currency.symbol,
                onTransferClick = onTransferClick
            )
        }

        // Quick Stats Summary Grid (Monthly Budget & Savings Goal)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly Budget Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToBudgets() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = SophisticatedTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${(budgetProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "${formatCurrency(budgetRemaining, currency.symbol)} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = SophisticatedPrimary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SophisticatedPrimary,
                            trackColor = SophisticatedBorder
                        )
                    }
                }

                // Savings Goal Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToGoals() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Savings Goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = SophisticatedTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = formatCurrency(totalGoalsSaved, currency.symbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "Goal: ${(goalsProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = SophisticatedPrimary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { goalsProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SophisticatedPrimary,
                            trackColor = SophisticatedBorder
                        )
                    }
                }
            }
        }

        // AI Wealth Coach Insight Highlight Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAi() }
                    .testTag("ai_advisor_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SophisticatedSurface
                ),
                border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(listOf(SophisticatedPrimary, SophisticatedOnPrimary)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Spending Coach",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SophisticatedPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Score: ${insight.healthScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SophisticatedPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = insight.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = SophisticatedTextSecondary,
                            maxLines = 2
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SophisticatedTextSecondary
                    )
                }
            }
        }

        // Wallets & Accounts Carousel
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accounts & Wallets (${accounts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedTextPrimary
                    )
                    TextButton(onClick = onAddAccountClick) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SophisticatedPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Account", color = SophisticatedPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accounts) { acc ->
                        AccountCard(
                            account = acc,
                            currencySymbol = currency.symbol,
                            onClick = onNavigateToTransactions
                        )
                    }
                }
            }
        }

        // Donut Chart - Category Spending
        item {
            CategoryDonutChart(
                categorySpending = categorySpending,
                currencySymbol = currency.symbol
            )
        }

        // Weekly Spending Outlay Bar Chart
        item {
            SimpleSpendingBarChart(
                weeklySpending = weeklySpending,
                currencySymbol = currency.symbol
            )
        }

        // Budget Status Snapshot
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget Alerts & Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedTextPrimary
                    )
                    TextButton(onClick = onNavigateToBudgets) {
                        Text("Manage Budgets", color = SophisticatedPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (budgets.isEmpty()) {
                    Text(
                        text = "No budgets set. Create budgets to monitor threshold alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SophisticatedTextSecondary
                    )
                } else {
                    budgets.take(3).forEach { bgt ->
                        val spent = transactions.filter {
                            it.type == TransactionType.EXPENSE && (it.categoryId == bgt.categoryId || it.categoryName.equals(bgt.categoryName, ignoreCase = true))
                        }.sumOf { it.amount }

                        BudgetStatusCard(
                            categoryName = bgt.categoryName,
                            spentAmount = spent,
                            budgetLimit = bgt.amount,
                            currencySymbol = currency.symbol,
                            colorHex = bgt.colorHex,
                            onEditClick = onNavigateToBudgets,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Financial Goals Snapshot
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Financial Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedTextPrimary
                    )
                    TextButton(onClick = onNavigateToGoals) {
                        Text("View Goals", color = SophisticatedPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (goals.isEmpty()) {
                    Text(
                        text = "No financial goals yet. Create targets like Emergency Fund or Laptop.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SophisticatedTextSecondary
                    )
                } else {
                    goals.take(2).forEach { goal ->
                        GoalProgressCard(
                            title = goal.title,
                            currentAmount = goal.currentAmount,
                            targetAmount = goal.targetAmount,
                            deadlineDate = goal.deadlineDate,
                            currencySymbol = currency.symbol,
                            iconName = goal.iconName,
                            colorHex = goal.colorHex,
                            onDepositClick = { onDepositGoalClick(goal) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Upcoming Recurring Payments
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Recurring Bills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedTextPrimary
                    )
                    TextButton(onClick = onNavigateToGoals) {
                        Text("All Bills", color = SophisticatedPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                recurring.take(3).forEach { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SophisticatedPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = SophisticatedPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = rec.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SophisticatedTextPrimary)
                                    Text(text = "Due on ${sdf.format(Date(rec.nextDueDate))} • ${rec.frequency.displayName}", style = MaterialTheme.typography.labelSmall, color = SophisticatedTextSecondary)
                                }
                            }
                            Text(
                                text = formatCurrency(rec.amount, currency.symbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions List
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedTextPrimary
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("See All (${transactions.size})", color = SophisticatedPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions yet. Click + to add your first transaction.", color = SophisticatedTextSecondary)
                    }
                } else {
                    transactions.take(5).forEach { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            currencySymbol = currency.symbol,
                            onClick = { onTransactionClick(tx) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
