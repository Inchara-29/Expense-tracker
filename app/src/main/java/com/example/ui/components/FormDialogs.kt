package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Budget
import com.example.data.model.BudgetPeriod
import com.example.data.model.Category
import com.example.data.model.CategoryGroup
import com.example.data.model.CategoryType
import com.example.data.model.FinancialGoal
import com.example.data.model.PaymentMethod
import com.example.data.model.RecurrenceFrequency
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PrimaryEmerald
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: Transaction? = null,
    categories: List<Category>,
    accounts: List<Account>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((Transaction) -> Unit)? = null
) {
    var selectedType by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf(if (initialTransaction != null) initialTransaction.amount.toString() else "") }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }

    val filteredCategories = categories.filter {
        when (selectedType) {
            TransactionType.EXPENSE -> it.type == CategoryType.EXPENSE
            TransactionType.INCOME -> it.type == CategoryType.INCOME
            TransactionType.TRANSFER -> true
        }
    }

    var selectedCategory by remember {
        mutableStateOf(
            categories.find { it.id == initialTransaction?.categoryId }
                ?: filteredCategories.firstOrNull()
                ?: Category("cat_misc", "General", CategoryType.EXPENSE)
        )
    }

    var selectedAccount by remember {
        mutableStateOf(
            accounts.find { it.id == initialTransaction?.accountId }
                ?: accounts.firstOrNull()
                ?: Account("acc_1", "Main Account", AccountType.BANK, 0.0)
        )
    }

    var selectedDestAccount by remember {
        mutableStateOf(
            accounts.find { it.id == initialTransaction?.destinationAccountId }
                ?: accounts.filter { it.id != selectedAccount.id }.firstOrNull()
        )
    }

    var paymentMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethod.UPI) }
    var dateTimestamp by remember { mutableStateOf(initialTransaction?.date ?: System.currentTimeMillis()) }
    var tagsText by remember { mutableStateOf(initialTransaction?.tags ?: "") }
    var notesText by remember { mutableStateOf(initialTransaction?.notes ?: "") }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("transaction_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTransaction == null) "Record Transaction" else "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Tabs
                TabRow(
                    selectedTabIndex = selectedType.ordinal,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            selectedCategory = categories.firstOrNull { it.type == CategoryType.EXPENSE } ?: selectedCategory
                        },
                        text = { Text("Expense", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategory = categories.firstOrNull { it.type == CategoryType.INCOME } ?: selectedCategory
                        },
                        text = { Text("Income", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedType == TransactionType.TRANSFER,
                        onClick = { selectedType = TransactionType.TRANSFER },
                        text = { Text("Transfer", fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    leadingIcon = {
                        Text(
                            text = currencySymbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Merchant") },
                    placeholder = { Text("e.g., Grocery Mart, Salary, Coffee") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("description_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedType != TransactionType.TRANSFER) {
                    // Category Selection Chips
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredCategories) { cat ->
                            val isSelected = selectedCategory.id == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.name),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(cat.colorHex)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Account Selection
                Text(
                    text = if (selectedType == TransactionType.TRANSFER) "From Account" else "Account / Wallet",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accounts) { acc ->
                        val isSelected = selectedAccount.id == acc.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getAccountIcon(acc.type),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(acc.colorHex)
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (selectedType == TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "To Destination Account",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(accounts.filter { it.id != selectedAccount.id }) { acc ->
                            val isSelected = selectedDestAccount?.id == acc.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDestAccount = acc },
                                label = { Text(acc.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getAccountIcon(acc.type),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(acc.colorHex)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker & Payment Method Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateTimestamp }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    dateTimestamp = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = sdf.format(Date(dateTimestamp)), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Chips
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method.displayName, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tags & Notes
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (comma-separated)") },
                    placeholder = { Text("e.g., Dinner, Vacation, Work") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Memo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialTransaction != null && onDelete != null) {
                        IconButton(
                            onClick = { onDelete(initialTransaction) },
                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                containerColor = DangerCoral.copy(alpha = 0.15f),
                                contentColor = DangerCoral
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                val tx = Transaction(
                                    id = initialTransaction?.id ?: java.util.UUID.randomUUID().toString(),
                                    type = selectedType,
                                    amount = amount,
                                    categoryId = if (selectedType == TransactionType.TRANSFER) "cat_transfer" else selectedCategory.id,
                                    categoryName = if (selectedType == TransactionType.TRANSFER) "Transfer" else selectedCategory.name,
                                    description = description.ifBlank { if (selectedType == TransactionType.TRANSFER) "Transfer between accounts" else selectedCategory.name },
                                    date = dateTimestamp,
                                    paymentMethod = paymentMethod,
                                    accountId = selectedAccount.id,
                                    accountName = selectedAccount.name,
                                    destinationAccountId = if (selectedType == TransactionType.TRANSFER) selectedDestAccount?.id else null,
                                    destinationAccountName = if (selectedType == TransactionType.TRANSFER) selectedDestAccount?.name else null,
                                    tags = tagsText,
                                    notes = notesText
                                )
                                onSave(tx)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_transaction_button"),
                        shape = RoundedCornerShape(14.dp),
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditAccountDialog(
    initialAccount: Account? = null,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: ((Account) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialAccount?.name ?: "") }
    var selectedType by remember { mutableStateOf(initialAccount?.type ?: AccountType.BANK) }
    var balanceText by remember { mutableStateOf(if (initialAccount != null) initialAccount.balance.toString() else "0") }
    var maskedNumber by remember { mutableStateOf(initialAccount?.accountNumberMasked ?: "") }
    var selectedColor by remember { mutableStateOf(initialAccount?.colorHex ?: 0xFF10B981) }

    val colors = listOf(0xFF10B981, 0xFF2563EB, 0xFF7C3AED, 0xFFD97706, 0xFFDC2626, 0xFF0D9488, 0xFFEC4899)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAccount == null) "New Account / Wallet" else "Edit Account") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. Axis Bank, Paytm Wallet, Cash") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Account Type", style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AccountType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Current Balance ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = maskedNumber,
                    onValueChange = { maskedNumber = it },
                    label = { Text("Masked Number / UPI ID (Optional)") },
                    placeholder = { Text("•••• 4321 or user@upi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Badge Color", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(c), CircleShape)
                                .clickable { selectedColor = c },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == c) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val acc = Account(
                            id = initialAccount?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            type = selectedType,
                            balance = balanceText.toDoubleOrNull() ?: 0.0,
                            accountNumberMasked = maskedNumber.trim(),
                            colorHex = selectedColor
                        )
                        onSave(acc)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEditBudgetDialog(
    initialBudget: Budget? = null,
    categories: List<Category>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit,
    onDelete: ((Budget) -> Unit)? = null
) {
    val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }
    var selectedCategory by remember {
        mutableStateOf(
            expenseCategories.find { it.id == initialBudget?.categoryId } ?: expenseCategories.firstOrNull() ?: Category("cat_food", "Food", CategoryType.EXPENSE)
        )
    }
    var limitText by remember { mutableStateOf(initialBudget?.amount?.toString() ?: "") }
    var period by remember { mutableStateOf(initialBudget?.period ?: BudgetPeriod.MONTHLY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBudget == null) "Set Category Budget" else "Edit Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Category", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(expenseCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory.id == cat.id,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly Budget Limit ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Alert Thresholds: Automated alerts will fire at 50%, 75%, 90% and 100% exceeded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = limitText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val budget = Budget(
                            id = initialBudget?.id ?: java.util.UUID.randomUUID().toString(),
                            categoryId = selectedCategory.id,
                            categoryName = selectedCategory.name,
                            amount = amount,
                            period = period,
                            colorHex = selectedCategory.colorHex
                        )
                        onSave(budget)
                    }
                }
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEditGoalDialog(
    initialGoal: FinancialGoal? = null,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (FinancialGoal) -> Unit,
    onDelete: ((FinancialGoal) -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var targetText by remember { mutableStateOf(initialGoal?.targetAmount?.toString() ?: "") }
    var currentText by remember { mutableStateOf(initialGoal?.currentAmount?.toString() ?: "0") }
    var notes by remember { mutableStateOf(initialGoal?.notes ?: "") }
    var deadlineDate by remember {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }
        mutableStateOf(initialGoal?.deadlineDate ?: cal.timeInMillis)
    }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) "New Financial Goal" else "Edit Goal") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. MacBook Pro, Emergency Fund, House Downpayment") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    label = { Text("Already Saved ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = deadlineDate }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                deadlineDate = newCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Target Deadline: ${sdf.format(Date(deadlineDate))}")
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Motivation") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    val current = currentText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && target > 0) {
                        val goal = FinancialGoal(
                            id = initialGoal?.id ?: java.util.UUID.randomUUID().toString(),
                            title = title.trim(),
                            targetAmount = target,
                            currentAmount = current,
                            deadlineDate = deadlineDate,
                            notes = notes
                        )
                        onSave(goal)
                    }
                }
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DepositGoalDialog(
    goal: FinancialGoal,
    accounts: List<Account>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onDeposit: (Double, Account) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deposit to ${goal.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Target: ${formatCurrency(goal.targetAmount, currencySymbol)} • Currently Saved: ${formatCurrency(goal.currentAmount, currencySymbol)}")

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Deposit Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Deduct from Account:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accounts) { acc ->
                        FilterChip(
                            selected = selectedAccount?.id == acc.id,
                            onClick = { selectedAccount = acc },
                            label = { Text("${acc.name} (${formatShortCurrency(acc.balance, currencySymbol)})") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && selectedAccount != null) {
                        onDeposit(amt, selectedAccount!!)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && selectedAccount != null
            ) {
                Text("Confirm Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TransferMoneyDialog(
    accounts: List<Account>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onTransfer: (Account, Account, Double, String) -> Unit
) {
    var fromAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var toAccount by remember { mutableStateOf(accounts.filter { it.id != fromAccount?.id }.firstOrNull()) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Between Accounts") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("From Account:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accounts) { acc ->
                        FilterChip(
                            selected = fromAccount?.id == acc.id,
                            onClick = {
                                fromAccount = acc
                                if (toAccount?.id == acc.id) {
                                    toAccount = accounts.filter { it.id != acc.id }.firstOrNull()
                                }
                            },
                            label = { Text(acc.name) }
                        )
                    }
                }

                Text("To Account:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accounts.filter { it.id != fromAccount?.id }) { acc ->
                        FilterChip(
                            selected = toAccount?.id == acc.id,
                            onClick = { toAccount = acc },
                            label = { Text(acc.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Transfer Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Transfer Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && fromAccount != null && toAccount != null) {
                        onTransfer(fromAccount!!, toAccount!!, amt, note.ifBlank { "Account Transfer" })
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && fromAccount != null && toAccount != null
            ) {
                Text("Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditRecurringDialog(
    initialRecurring: RecurringTransaction? = null,
    categories: List<Category>,
    accounts: List<Account>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (RecurringTransaction) -> Unit,
    onDelete: ((RecurringTransaction) -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialRecurring?.title ?: "") }
    var amountText by remember { mutableStateOf(initialRecurring?.amount?.toString() ?: "") }
    var selectedFreq by remember { mutableStateOf(initialRecurring?.frequency ?: RecurrenceFrequency.MONTHLY) }
    val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }
    var selectedCategory by remember {
        mutableStateOf(
            categories.find { it.id == initialRecurring?.categoryId } ?: expenseCategories.firstOrNull() ?: Category("cat_misc", "General", CategoryType.EXPENSE)
        )
    }
    var selectedAccount by remember {
        mutableStateOf(
            accounts.find { it.id == initialRecurring?.accountId } ?: accounts.firstOrNull() ?: Account("acc_1", "Main Account", AccountType.BANK, 0.0)
        )
    }

    var nextDueDate by remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
        mutableStateOf(initialRecurring?.nextDueDate ?: cal.timeInMillis)
    }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRecurring == null) "Schedule Recurring Bill" else "Edit Recurring Bill") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill / Subscription Title") },
                    placeholder = { Text("e.g. Netflix, Rent, Electricity Bill, Gym") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Frequency", style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RecurrenceFrequency.values().forEach { freq ->
                        FilterChip(
                            selected = selectedFreq == freq,
                            onClick = { selectedFreq = freq },
                            label = { Text(freq.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Category", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(expenseCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory.id == cat.id,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name) }
                        )
                    }
                }

                Text("Payment Account", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accounts) { acc ->
                        FilterChip(
                            selected = selectedAccount.id == acc.id,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc.name) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = nextDueDate }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                nextDueDate = newCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("First Due Date: ${sdf.format(Date(nextDueDate))}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        val rec = RecurringTransaction(
                            id = initialRecurring?.id ?: java.util.UUID.randomUUID().toString(),
                            title = title.trim(),
                            amount = amt,
                            frequency = selectedFreq,
                            type = TransactionType.EXPENSE,
                            categoryId = selectedCategory.id,
                            categoryName = selectedCategory.name,
                            accountId = selectedAccount.id,
                            accountName = selectedAccount.name,
                            nextDueDate = nextDueDate
                        )
                        onSave(rec)
                    }
                }
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

