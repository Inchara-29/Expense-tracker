package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.PaymentMethod
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ChartBlue
import com.example.ui.theme.ChartGreen
import com.example.ui.theme.ChartIndigo
import com.example.ui.theme.ChartOrange
import com.example.ui.theme.ChartPink
import com.example.ui.theme.ChartPurple
import com.example.ui.theme.ChartRed
import com.example.ui.theme.ChartYellow
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBlue
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedNegativeRose
import com.example.ui.theme.SophisticatedOnPrimary
import com.example.ui.theme.SophisticatedOnPrimaryContainer
import com.example.ui.theme.SophisticatedPositiveMint
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedPrimaryContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceVariant
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double, symbol: String = "₹"): String {
    val isNegative = amount < 0
    val absAmount = Math.abs(amount)
    val formatted = String.format(Locale.getDefault(), "%,.2f", absAmount)
    return if (isNegative) "-$symbol$formatted" else "$symbol$formatted"
}

fun formatShortCurrency(amount: Double, symbol: String = "₹"): String {
    val isNegative = amount < 0
    val absAmount = Math.abs(amount)
    val formatted = when {
        absAmount >= 10_000_000 -> String.format(Locale.getDefault(), "%.2f Cr", absAmount / 10_000_000)
        absAmount >= 100_000 -> String.format(Locale.getDefault(), "%.2f L", absAmount / 100_000)
        absAmount >= 1_000 -> String.format(Locale.getDefault(), "%.1f k", absAmount / 1_000)
        else -> String.format(Locale.getDefault(), "%.0f", absAmount)
    }
    return if (isNegative) "-$symbol$formatted" else "$symbol$formatted"
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "shopping_cart", "groceries" -> Icons.Default.ShoppingCart
        "home", "rent" -> Icons.Default.Home
        "bolt", "electricity" -> Icons.Default.Bolt
        "water_drop", "water" -> Icons.Default.WaterDrop
        "wifi", "internet" -> Icons.Default.Wifi
        "directions_car", "transportation" -> Icons.Default.DirectionsCar
        "medical_services", "healthcare" -> Icons.Default.MedicalServices
        "school", "education" -> Icons.Default.School
        "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
        "movie", "entertainment" -> Icons.Default.Movie
        "flight", "travel" -> Icons.Default.Flight
        "local_dining", "restaurants" -> Icons.Default.LocalDining
        "subscriptions" -> Icons.Default.Subscriptions
        "palette", "hobbies" -> Icons.Default.Palette
        "card_giftcard", "gifts" -> Icons.Default.CardGiftcard
        "account_balance", "taxes" -> Icons.Default.AccountBalance
        "security", "insurance" -> Icons.Default.Security
        "trending_up", "investments" -> Icons.Default.TrendingUp
        "payments", "salary" -> Icons.Default.Payments
        "laptop_mac", "laptop", "freelancing" -> Icons.Default.Laptop
        "store", "business" -> Icons.Default.Store
        "show_chart" -> Icons.Default.ShowChart
        "savings", "interest" -> Icons.Default.Savings
        "attach_money", "money" -> Icons.Default.AttachMoney
        else -> Icons.Default.Category
    }
}

fun getAccountIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Default.Payments
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.SAVINGS -> Icons.Default.Savings
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
        AccountType.UPI -> Icons.Default.PhoneAndroid
        AccountType.INVESTMENT -> Icons.Default.TrendingUp
        AccountType.DIGITAL_WALLET -> Icons.Default.AccountBalanceWallet
    }
}

@Composable
fun MainBalanceCard(
    totalBalance: Double,
    thisMonthIncome: Double,
    thisMonthExpense: Double,
    savingsRate: Double,
    currencySymbol: String = "₹",
    onTransferClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(28.dp))
            .testTag("main_balance_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedPrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SophisticatedPrimary,
                            Color(0xFFDCCEFF)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Total Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = SophisticatedOnPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(totalBalance, currencySymbol),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedOnPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.clickable { onTransferClick() }
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Transfer",
                                tint = SophisticatedOnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Income Stat
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "INCOME",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedOnPrimary.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+${formatShortCurrency(thisMonthIncome, currencySymbol)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedOnPrimary
                            )
                        }
                    }

                    // Expense Stat
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "EXPENSES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedOnPrimary.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "-${formatShortCurrency(thisMonthExpense, currencySymbol)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedOnPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Savings meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Savings Rate",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = SophisticatedOnPrimary.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", savingsRate)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedOnPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (savingsRate / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SophisticatedOnPrimary,
                    trackColor = SophisticatedOnPrimary.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    currencySymbol: String = "₹",
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(180.dp)
            .clickable { onClick() }
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) SophisticatedPrimary else SophisticatedBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(account.colorHex).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountIcon(account.type),
                        contentDescription = null,
                        tint = Color(account.colorHex),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (account.accountNumberMasked.isNotBlank()) {
                    Text(
                        text = account.accountNumberMasked,
                        style = MaterialTheme.typography.labelSmall,
                        color = SophisticatedTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = account.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SophisticatedTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatCurrency(account.balance, currencySymbol),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (account.balance < 0) SophisticatedNegativeRose else SophisticatedTextPrimary
            )

            Text(
                text = account.type.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = SophisticatedTextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: Transaction,
    currencySymbol: String = "₹",
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
    showCheckbox: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(transaction.date))

    val isExpense = transaction.type == TransactionType.EXPENSE
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val amountColor = when {
        isTransfer -> SophisticatedBlue
        isExpense -> SophisticatedNegativeRose
        else -> SophisticatedPositiveMint
    }

    val amountPrefix = when {
        isTransfer -> "⇄ "
        isExpense -> "- "
        else -> "+ "
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("transaction_row_${transaction.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SophisticatedSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) SophisticatedPrimary else SophisticatedBorder.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = when {
                            isTransfer -> SophisticatedBlue.copy(alpha = 0.15f)
                            isExpense -> SophisticatedPrimaryContainer
                            else -> SophisticatedOnPrimary
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isTransfer -> Icons.Default.SwapHoriz
                        else -> getCategoryIcon(transaction.categoryName)
                    },
                    contentDescription = transaction.categoryName,
                    tint = when {
                        isTransfer -> SophisticatedBlue
                        isExpense -> SophisticatedOnPrimaryContainer
                        else -> SophisticatedPrimary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifBlank { transaction.categoryName },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SophisticatedTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = SophisticatedTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • ${transaction.accountName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SophisticatedTextSecondary.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = SophisticatedTextSecondary.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix$currencySymbol${String.format(Locale.getDefault(), "%,.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = transaction.paymentMethod.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = SophisticatedTextSecondary
                )
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    categorySpending: Map<String, Double>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val totalExpense = categorySpending.values.sum()
    val palette = listOf(
        SophisticatedPrimary, SophisticatedPositiveMint, SophisticatedNegativeRose, SophisticatedBlue, AccentGold, ChartPurple, ChartPink, SecondaryTeal
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Category Spending Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SophisticatedTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (totalExpense <= 0 || categorySpending.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SophisticatedTextSecondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Donut Canvas
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            val strokeWidth = 24.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            var startAngle = -90f

                            val sortedEntries = categorySpending.entries.sortedByDescending { it.value }
                            sortedEntries.forEachIndexed { index, entry ->
                                val sweepAngle = ((entry.value / totalExpense) * 360f).toFloat()
                                val color = palette[index % palette.size]
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle - 2f,
                                    useCenter = false,
                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = SophisticatedTextSecondary
                            )
                            Text(
                                text = formatShortCurrency(totalExpense, currencySymbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sortedEntries = categorySpending.entries.sortedByDescending { it.value }.take(5)
                        sortedEntries.forEachIndexed { index, entry ->
                            val pct = (entry.value / totalExpense * 100).toInt()
                            val color = palette[index % palette.size]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SophisticatedTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "$pct% (${formatShortCurrency(entry.value, currencySymbol)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SophisticatedTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleSpendingBarChart(
    weeklySpending: List<Pair<String, Double>>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val maxVal = (weeklySpending.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Recent Daily / Weekly Outlay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SophisticatedTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklySpending.forEach { (label, amount) ->
                    val heightRatio = (amount / maxVal).toFloat().coerceIn(0.05f, 1f)
                    val animHeight by animateFloatAsState(targetValue = heightRatio, animationSpec = tween(600))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatShortCurrency(amount, currencySymbol),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = SophisticatedTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((80 * animHeight).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(SophisticatedPrimary, SophisticatedPrimary.copy(alpha = 0.4f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = SophisticatedTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetStatusCard(
    categoryName: String,
    spentAmount: Double,
    budgetLimit: Double,
    currencySymbol: String = "₹",
    colorHex: Long = 0xFFD0BCFF,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val ratio = if (budgetLimit > 0) (spentAmount / budgetLimit).toFloat() else 0f
    val remaining = budgetLimit - spentAmount
    val percentInt = (ratio * 100).toInt()

    val alertColor = when {
        ratio >= 1.0f -> SophisticatedNegativeRose
        ratio >= 0.9f -> ChartOrange
        ratio >= 0.75f -> AccentGold
        else -> SophisticatedPrimary
    }

    val badgeText = when {
        ratio >= 1.0f -> "Exceeded"
        ratio >= 0.9f -> "90% Alert"
        ratio >= 0.75f -> "75% Warning"
        else -> "Safe"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("budget_card_$categoryName"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(colorHex).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(categoryName),
                            contentDescription = null,
                            tint = Color(colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedTextPrimary
                        )
                        Text(
                            text = "Limit: ${formatCurrency(budgetLimit, currencySymbol)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SophisticatedTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = alertColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = alertColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = alertColor,
                trackColor = SophisticatedBorder
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${formatCurrency(spentAmount, currencySymbol)} ($percentInt%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SophisticatedTextSecondary
                )
                Text(
                    text = if (remaining >= 0) "Left: ${formatCurrency(remaining, currencySymbol)}" else "Over by: ${formatCurrency(-remaining, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining >= 0) SophisticatedPositiveMint else SophisticatedNegativeRose
                )
            }
        }
    }
}

@Composable
fun GoalProgressCard(
    title: String,
    currentAmount: Double,
    targetAmount: Double,
    deadlineDate: Long,
    currencySymbol: String = "₹",
    iconName: String = "laptop",
    colorHex: Long = 0xFFD0BCFF,
    onDepositClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val remaining = (targetAmount - currentAmount).coerceAtLeast(0.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("goal_card_$title"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(colorHex).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(iconName),
                            contentDescription = null,
                            tint = Color(colorHex),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedTextPrimary
                        )
                        Text(
                            text = "Target: ${formatCurrency(targetAmount, currencySymbol)} • Target Date: ${sdf.format(Date(deadlineDate))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SophisticatedTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SophisticatedPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { onDepositClick() }
                ) {
                    Text(
                        text = "+ Deposit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SophisticatedPrimary,
                trackColor = SophisticatedBorder
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved: ${formatCurrency(currentAmount, currencySymbol)} ($percent%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SophisticatedTextSecondary
                )
                Text(
                    text = "Remaining: ${formatCurrency(remaining, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = SophisticatedTextPrimary
                )
            }
        }
    }
}
