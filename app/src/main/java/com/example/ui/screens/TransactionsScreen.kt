package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionItemRow
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatShortCurrency
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.viewmodel.DateFilter
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.SortOrder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionsScreen(
    viewModel: ExpenseViewModel,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val rawTransactions by viewModel.rawTransactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val accountFilter by viewModel.accountFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedTxIds by viewModel.selectedTxIds.collectAsStateWithLifecycle()

    var showSortMenu by remember { mutableStateOf(false) }
    var showBulkCategorizeDialog by remember { mutableStateOf(false) }
    var bulkDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Summary calculations for filtered list
    val filteredIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val filteredExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val isSelectionMode = selectedTxIds.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transactions_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Bulk Action Bar (when items are selected)
        if (isSelectionMode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedTxIds.size} Selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showBulkCategorizeDialog = true }) {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Categorize")
                        }

                        TextButton(
                            onClick = { bulkDeleteConfirmDialog = true },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = DangerCoral)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }

                        IconButton(onClick = { viewModel.clearTxSelection() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search merchant, category, tags, amount...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_field"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Date Filter Chips & Sort Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(DateFilter.values()) { df ->
                    FilterChip(
                        selected = dateFilter == df,
                        onClick = { viewModel.dateFilter.value = df },
                        label = { Text(df.label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOrder.values().forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.label) },
                            onClick = {
                                viewModel.sortOrder.value = order
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Transaction Type Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = typeFilter == null,
                onClick = { viewModel.typeFilter.value = null },
                label = { Text("All Types", style = MaterialTheme.typography.labelSmall) }
            )
            FilterChip(
                selected = typeFilter == TransactionType.EXPENSE,
                onClick = { viewModel.typeFilter.value = if (typeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE },
                label = { Text("Expenses", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DangerCoral.copy(alpha = 0.2f), selectedLabelColor = DangerCoral)
            )
            FilterChip(
                selected = typeFilter == TransactionType.INCOME,
                onClick = { viewModel.typeFilter.value = if (typeFilter == TransactionType.INCOME) null else TransactionType.INCOME },
                label = { Text("Income", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryEmerald.copy(alpha = 0.2f), selectedLabelColor = PrimaryEmerald)
            )
            FilterChip(
                selected = typeFilter == TransactionType.TRANSFER,
                onClick = { viewModel.typeFilter.value = if (typeFilter == TransactionType.TRANSFER) null else TransactionType.TRANSFER },
                label = { Text("Transfers", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = InfoBlue.copy(alpha = 0.2f), selectedLabelColor = InfoBlue)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${transactions.size} records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "In: +${formatShortCurrency(filteredIncome, currency.symbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryEmerald
                    )
                    Text(
                        text = "Out: -${formatShortCurrency(filteredExpense, currency.symbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DangerCoral
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching transactions found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        viewModel.searchQuery.value = ""
                        viewModel.dateFilter.value = DateFilter.ALL
                        viewModel.typeFilter.value = null
                    }) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions, key = { it.id }) { tx ->
                    val isSelected = selectedTxIds.contains(tx.id)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleTxSelection(tx.id) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        TransactionItemRow(
                            transaction = tx,
                            currencySymbol = currency.symbol,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleTxSelection(tx.id)
                                } else {
                                    onEditTransaction(tx)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Bulk Delete Confirmation Dialog
    if (bulkDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { bulkDeleteConfirmDialog = false },
            title = { Text("Delete ${selectedTxIds.size} Transactions?") },
            text = { Text("This will permanently remove the selected transactions and recalculate account balances.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedTransactions()
                        bulkDeleteConfirmDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DangerCoral)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { bulkDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bulk Categorize Dialog
    if (showBulkCategorizeDialog) {
        AlertDialog(
            onDismissRequest = { showBulkCategorizeDialog = false },
            title = { Text("Select New Category") },
            text = {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(categories) { cat ->
                        TextButton(
                            onClick = {
                                viewModel.bulkCategorizeSelected(cat)
                                showBulkCategorizeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat.name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBulkCategorizeDialog = false }) { Text("Cancel") }
            }
        )
    }
}
