package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Budget
import com.example.data.model.FinancialGoal
import com.example.data.model.RecurringTransaction
import com.example.data.model.TransactionType
import com.example.ui.components.BudgetStatusCard
import com.example.ui.components.GoalProgressCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BudgetsAndGoalsScreen(
    viewModel: ExpenseViewModel,
    onAddBudgetClick: () -> Unit,
    onEditBudgetClick: (Budget) -> Unit,
    onAddGoalClick: () -> Unit,
    onDepositGoalClick: (FinancialGoal) -> Unit,
    onAddRecurringClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Budgets", "Goals", "Recurring Bills")

    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val recurring by viewModel.recurringTransactions.collectAsStateWithLifecycle()
    val transactions by viewModel.rawTransactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    val totalBudgetLimit = budgets.sumOf { it.amount }
    val totalBudgetSpent = budgets.sumOf { bgt ->
        transactions.filter {
            it.type == TransactionType.EXPENSE && (it.categoryId == bgt.categoryId || it.categoryName.equals(bgt.categoryName, ignoreCase = true))
        }.sumOf { it.amount }
    }

    val totalGoalTarget = goals.sumOf { it.targetAmount }
    val totalGoalSaved = goals.sumOf { it.currentAmount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("budgets_goals_screen")
    ) {
        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // Budgets Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Overview Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Monthly Budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(formatCurrency(totalBudgetLimit, currency.symbol), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(formatCurrency(totalBudgetSpent, currency.symbol), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (totalBudgetSpent > totalBudgetLimit) DangerCoral else PrimaryEmerald)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val overallRatio = if (totalBudgetLimit > 0) (totalBudgetSpent / totalBudgetLimit).toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { overallRatio.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (overallRatio >= 1f) DangerCoral else PrimaryEmerald,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (totalBudgetLimit > totalBudgetSpent) "Remaining Budget Pool: ${formatCurrency(totalBudgetLimit - totalBudgetSpent, currency.symbol)}" else "⚠️ Exceeded aggregate limits by ${formatCurrency(totalBudgetSpent - totalBudgetLimit, currency.symbol)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (totalBudgetLimit >= totalBudgetSpent) PrimaryEmerald else DangerCoral
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Category Budgets (${budgets.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = onAddBudgetClick, shape = RoundedCornerShape(12.dp)) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Budget")
                            }
                        }
                    }

                    if (budgets.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No category budgets configured yet. Tap 'New Budget' to create one.")
                            }
                        }
                    } else {
                        items(budgets, key = { it.id }) { bgt ->
                            val spent = transactions.filter {
                                it.type == TransactionType.EXPENSE && (it.categoryId == bgt.categoryId || it.categoryName.equals(bgt.categoryName, ignoreCase = true))
                            }.sumOf { it.amount }

                            BudgetStatusCard(
                                categoryName = bgt.categoryName,
                                spentAmount = spent,
                                budgetLimit = bgt.amount,
                                currencySymbol = currency.symbol,
                                colorHex = bgt.colorHex,
                                onEditClick = { onEditBudgetClick(bgt) }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Goals Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Goals Overview Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Targets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(formatCurrency(totalGoalTarget, currency.symbol), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Accumulated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(formatCurrency(totalGoalSaved, currency.symbol), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryEmerald)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val overallGoalRatio = if (totalGoalTarget > 0) (totalGoalSaved / totalGoalTarget).toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { overallGoalRatio.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PrimaryEmerald,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Overall Goal Completion: ${(overallGoalRatio * 100).toInt()}% of total aspirations funded.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Active Goals (${goals.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = onAddGoalClick, shape = RoundedCornerShape(12.dp)) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Goal")
                            }
                        }
                    }

                    if (goals.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No financial goals configured yet. Tap 'New Goal' to start saving.")
                            }
                        }
                    } else {
                        items(goals, key = { it.id }) { goal ->
                            GoalProgressCard(
                                title = goal.title,
                                currentAmount = goal.currentAmount,
                                targetAmount = goal.targetAmount,
                                deadlineDate = goal.deadlineDate,
                                currencySymbol = currency.symbol,
                                iconName = goal.iconName,
                                colorHex = goal.colorHex,
                                onDepositClick = { onDepositGoalClick(goal) }
                            )
                        }
                    }
                }
            }

            2 -> {
                // Recurring Bills Tab
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recurring Subscriptions & Bills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = onAddRecurringClick, shape = RoundedCornerShape(12.dp)) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Bill")
                            }
                        }
                    }

                    if (recurring.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No recurring bills or subscriptions scheduled.")
                            }
                        }
                    } else {
                        items(recurring, key = { it.id }) { rec ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(AccentGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getCategoryIcon(rec.categoryName),
                                                contentDescription = null,
                                                tint = AccentGold,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(text = rec.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "Next: ${sdf.format(Date(rec.nextDueDate))} • ${rec.frequency.displayName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "From: ${rec.accountName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatCurrency(rec.amount, currency.symbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { viewModel.executeRecurring(rec) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pay Now", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
