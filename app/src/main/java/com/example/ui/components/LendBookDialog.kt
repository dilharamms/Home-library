package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.Book
import com.example.data.Member
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendBookDialog(
    availableBooks: List<Book>,
    members: List<Member>,
    preselectedBookId: Long? = null,
    onDismiss: () -> Unit,
    onLend: (bookId: Long, memberId: Long, dueDate: Long, notes: String) -> Unit
) {
    var selectedBookId by remember {
        mutableStateOf(preselectedBookId ?: availableBooks.firstOrNull { it.availableCopies > 0 }?.id)
    }
    var selectedMemberId by remember {
        mutableStateOf(members.firstOrNull()?.id)
    }

    var durationDays by remember { mutableIntStateOf(14) }
    var notes by remember { mutableStateOf("") }

    val dayMs = 24L * 60 * 60 * 1000
    val calculatedDueDate = remember(durationDays) {
        System.currentTimeMillis() + (durationDays * dayMs)
    }

    var bookDropdownExpanded by remember { mutableStateOf(false) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    val selectedBook = availableBooks.find { it.id == selectedBookId }
    val selectedMember = members.find { it.id == selectedMemberId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Lend a Book",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Book Selector
                Text(
                    text = "1. Select Book to Lend",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (availableBooks.none { it.availableCopies > 0 }) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No books are currently in stock to lend.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = bookDropdownExpanded,
                        onExpandedChange = { bookDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBook?.let { "${it.title} (${it.availableCopies} available)" } ?: "Select Book",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Available Book") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("select_book_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = bookDropdownExpanded,
                            onDismissRequest = { bookDropdownExpanded = false }
                        ) {
                            availableBooks.filter { it.availableCopies > 0 }.forEach { book ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(book.title, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${book.author} • Location: ${book.rackId} (${book.shelfNumber}) • ${book.availableCopies} in stock",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedBookId = book.id
                                        bookDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Shelf location reminder for librarian
                    selectedBook?.let { book ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Located at: ${book.rackId} • ${book.shelfNumber}${if (book.section.isNotBlank()) " (${book.section})" else ""}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Member Selector
                Text(
                    text = "2. Select Member / Borrower",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (members.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Please register at least one member first.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = memberDropdownExpanded,
                        onExpandedChange = { memberDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMember?.let { "${it.name} (${it.membershipCode})" } ?: "Select Member",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Member") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("select_member_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = memberDropdownExpanded,
                            onDismissRequest = { memberDropdownExpanded = false }
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(m.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${m.membershipCode} • ${m.phone} • ${m.activeLoansCount} active loans",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedMemberId = m.id
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Loan Duration Presets
                Text(
                    text = "3. Loan Duration",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(7 to "1 Week", 14 to "2 Weeks", 21 to "3 Weeks", 30 to "1 Month").forEach { (days, label) ->
                        FilterChip(
                            selected = durationDays == days,
                            onClick = { durationDays = days },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Due Date summary card
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Return Due Date",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = DateUtils.formatDate(calculatedDueDate),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Borrowing Notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bookId = selectedBookId ?: return@Button
                    val memberId = selectedMemberId ?: return@Button
                    onLend(bookId, memberId, calculatedDueDate, notes.trim())
                },
                enabled = selectedBookId != null && selectedMemberId != null && (selectedBook?.availableCopies ?: 0) > 0,
                modifier = Modifier.testTag("confirm_lend_button")
            ) {
                Text("Confirm Lending")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
