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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.example.data.LoanWithDetails
import com.example.ui.theme.AvailableGreen
import com.example.ui.theme.DueSoonYellow
import com.example.ui.theme.OverdueRed
import com.example.util.DateUtils

enum class LoanFilter {
    ACTIVE,
    OVERDUE_ONLY,
    ALL_HISTORY
}

@Composable
fun LoansScreen(
    allLoans: List<LoanWithDetails>,
    onReturnBook: (loanId: Long, bookTitle: String) -> Unit,
    onDeleteLoan: (loanId: Long) -> Unit,
    onLendBookClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(LoanFilter.ACTIVE) }

    val filteredLoans = remember(allLoans, selectedFilter) {
        val now = System.currentTimeMillis()
        when (selectedFilter) {
            LoanFilter.ACTIVE -> allLoans.filter { it.status != "RETURNED" }
            LoanFilter.OVERDUE_ONLY -> allLoans.filter { it.status != "RETURNED" && it.dueDate < now }
            LoanFilter.ALL_HISTORY -> allLoans
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onLendBookClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_lend_book")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Lend Book")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("loans_screen")
        ) {
            // Segmented Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == LoanFilter.ACTIVE,
                    onClick = { selectedFilter = LoanFilter.ACTIVE },
                    label = { Text("Active Loans", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedFilter == LoanFilter.OVERDUE_ONLY,
                    onClick = { selectedFilter = LoanFilter.OVERDUE_ONLY },
                    label = { Text("Overdue", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = OverdueRed, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedFilter == LoanFilter.ALL_HISTORY,
                    onClick = { selectedFilter = LoanFilter.ALL_HISTORY },
                    label = { Text("All History", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Loans List
            if (filteredLoans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedFilter == LoanFilter.OVERDUE_ONLY) "No overdue loans!"
                            else "No lending records in this category",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (selectedFilter == LoanFilter.OVERDUE_ONLY) "All borrowed books are within their return dates."
                            else "Tap the + button to lend a book to a member.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLoans, key = { it.loanId }) { loan ->
                        LoanCard(
                            loan = loan,
                            onReturn = { onReturnBook(loan.loanId, loan.bookTitle) },
                            onDelete = { onDeleteLoan(loan.loanId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoanCard(
    loan: LoanWithDetails,
    onReturn: () -> Unit,
    onDelete: () -> Unit
) {
    val isReturned = loan.status == "RETURNED"
    val isOverdue = !isReturned && DateUtils.isOverdue(loan.dueDate)
    val isDueSoon = !isReturned && !isOverdue && DateUtils.isDueSoon(loan.dueDate, 2)

    val cardColor = when {
        isReturned -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surface
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_card_${loan.loanId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Book title & status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.bookTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "by ${loan.bookAuthor}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status chip
                Surface(
                    color = when {
                        isReturned -> AvailableGreen.copy(alpha = 0.2f)
                        isOverdue -> OverdueRed
                        isDueSoon -> DueSoonYellow.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            isReturned -> "Returned"
                            isOverdue -> DateUtils.getDueStatusDescription(loan.dueDate)
                            isDueSoon -> DateUtils.getDueStatusDescription(loan.dueDate)
                            else -> DateUtils.getDueStatusDescription(loan.dueDate)
                        },
                        color = when {
                            isReturned -> AvailableGreen
                            isOverdue -> Color.White
                            isDueSoon -> DueSoonYellow
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Borrower details
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${loan.memberName} (${loan.memberCode})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        if (loan.memberPhone.isNotBlank()) {
                            Text(
                                text = "Phone: ${loan.memberPhone}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location to return to & Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rack: ${loan.rackId} • ${loan.shelfNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (isReturned && loan.returnDate != null)
                        "Returned ${DateUtils.formatShortDate(loan.returnDate)}"
                    else "Due: ${DateUtils.formatDate(loan.dueDate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOverdue) OverdueRed else MaterialTheme.colorScheme.onSurface
                )
            }

            // Action Bar
            if (!isReturned) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onReturn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("return_button_${loan.loanId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Returned")
                    }
                }
            }
        }
    }
}
