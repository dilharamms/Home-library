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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book

val AVAILABLE_COLORS = listOf(
    "#2563EB", // Blue
    "#059669", // Emerald
    "#D97706", // Amber
    "#7C3AED", // Purple
    "#DC2626", // Red
    "#475569", // Slate
    "#EA580C", // Orange
    "#0284C7"  // Sky
)

val DEFAULT_GENRES = listOf(
    "Classic Fiction", "Technology", "History", "Philosophy",
    "Science", "Design", "Sci-Fi", "Fantasy", "Mystery", "Biography"
)

val DEFAULT_RACKS = listOf("Rack A", "Rack B", "Rack C", "Rack D", "Study Nook")
val DEFAULT_SHELVES = listOf("Shelf 1", "Shelf 2", "Shelf 3", "Shelf 4", "Top Shelf", "Bottom Shelf")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookDialog(
    initialBook: Book? = null,
    categories: List<com.example.data.Category> = emptyList(),
    onAddCategory: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (Book) -> Unit
) {
    var title by remember { mutableStateOf(initialBook?.title ?: "") }
    var author by remember { mutableStateOf(initialBook?.author ?: "") }
    var genre by remember { mutableStateOf(initialBook?.genre ?: "Classic Fiction") }
    var isbn by remember { mutableStateOf(initialBook?.isbn ?: "") }
    var rackId by remember { mutableStateOf(initialBook?.rackId ?: "Rack A") }
    var shelfNumber by remember { mutableStateOf(initialBook?.shelfNumber ?: "Shelf 1") }
    var section by remember { mutableStateOf(initialBook?.section ?: "Main Room") }
    var totalCopies by remember { mutableIntStateOf(initialBook?.totalCopies ?: 1) }
    var availableCopies by remember { mutableIntStateOf(initialBook?.availableCopies ?: 1) }
    var publicationYearText by remember { mutableStateOf(initialBook?.publicationYear?.toString() ?: "") }
    var coverColorHex by remember { mutableStateOf(initialBook?.coverColorHex ?: "#2563EB") }
    var notes by remember { mutableStateOf(initialBook?.notes ?: "") }

    var genreExpanded by remember { mutableStateOf(false) }
    var rackExpanded by remember { mutableStateOf(false) }
    var shelfExpanded by remember { mutableStateOf(false) }

    var isAddingCustomCategory by remember { mutableStateOf(false) }
    var customCategoryName by remember { mutableStateOf("") }

    val categoryOptions = remember(categories) {
        val names = categories.map { it.name }.filter { it.isNotBlank() }
        if (names.isEmpty()) DEFAULT_GENRES else (names + DEFAULT_GENRES).distinct().sorted()
    }

    var titleError by remember { mutableStateOf(false) }
    var authorError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (initialBook == null) "Stock New Book" else "Edit Book Details",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Book Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    label = { Text("Book Title *") },
                    isError = titleError,
                    supportingText = if (titleError) { { Text("Title cannot be empty") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_title_input")
                )

                // Author
                OutlinedTextField(
                    value = author,
                    onValueChange = {
                        author = it
                        if (authorError && it.isNotBlank()) authorError = false
                    },
                    label = { Text("Author *") },
                    isError = authorError,
                    supportingText = if (authorError) { { Text("Author cannot be empty") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_author_input")
                )

                // Genre Dropdown / Input
                ExposedDropdownMenuBox(
                    expanded = genreExpanded,
                    onExpandedChange = { genreExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre / Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = genreExpanded,
                        onDismissRequest = { genreExpanded = false }
                    ) {
                        categoryOptions.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    genre = item
                                    genreExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Create New Category", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                            },
                            onClick = {
                                genreExpanded = false
                                isAddingCustomCategory = true
                            }
                        )
                    }
                }

                if (isAddingCustomCategory) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customCategoryName,
                                onValueChange = { customCategoryName = it },
                                placeholder = { Text("New Genre Name (e.g. Psychology)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (customCategoryName.isNotBlank()) {
                                        val trimmed = customCategoryName.trim()
                                        onAddCategory?.invoke(trimmed)
                                        genre = trimmed
                                        customCategoryName = ""
                                        isAddingCustomCategory = false
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }

                // Rack & Shelf Location Section (Core Requirement)
                Text(
                    text = "Library Location (Rack & Shelf)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rack Dropdown
                    ExposedDropdownMenuBox(
                        expanded = rackExpanded,
                        onExpandedChange = { rackExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = rackId,
                            onValueChange = { rackId = it },
                            label = { Text("Rack *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rackExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .testTag("book_rack_input")
                        )
                        ExposedDropdownMenu(
                            expanded = rackExpanded,
                            onDismissRequest = { rackExpanded = false }
                        ) {
                            DEFAULT_RACKS.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        rackId = r
                                        rackExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Shelf Dropdown
                    ExposedDropdownMenuBox(
                        expanded = shelfExpanded,
                        onExpandedChange = { shelfExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = shelfNumber,
                            onValueChange = { shelfNumber = it },
                            label = { Text("Shelf *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shelfExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .testTag("book_shelf_input")
                        )
                        ExposedDropdownMenu(
                            expanded = shelfExpanded,
                            onDismissRequest = { shelfExpanded = false }
                        ) {
                            DEFAULT_SHELVES.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        shelfNumber = s
                                        shelfExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Section / Room Location
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Room / Section (e.g., Living Room)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Copies & Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalCopies.toString(),
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 1
                            totalCopies = parsed.coerceAtLeast(1)
                            if (availableCopies > totalCopies) {
                                availableCopies = totalCopies
                            }
                        },
                        label = { Text("Total Copies") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = availableCopies.toString(),
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 0
                            availableCopies = parsed.coerceIn(0, totalCopies)
                        },
                        label = { Text("In Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // ISBN & Year
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = isbn,
                        onValueChange = { isbn = it },
                        label = { Text("ISBN") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )

                    OutlinedTextField(
                        value = publicationYearText,
                        onValueChange = { publicationYearText = it },
                        label = { Text("Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                // Cover Color Palette Selector
                Text(
                    text = "Spine / Badge Accent Color",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AVAILABLE_COLORS.forEach { hex ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            Color.Blue
                        }
                        val isSelected = coverColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { coverColorHex = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Edition info") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    if (author.isBlank()) {
                        authorError = true
                        return@Button
                    }

                    val updatedBook = (initialBook ?: Book(
                        title = "",
                        author = "",
                        genre = "",
                        rackId = "",
                        shelfNumber = ""
                    )).copy(
                        title = title.trim(),
                        author = author.trim(),
                        genre = genre.trim().ifEmpty { "General" },
                        isbn = isbn.trim(),
                        rackId = rackId.trim().ifEmpty { "Rack A" },
                        shelfNumber = shelfNumber.trim().ifEmpty { "Shelf 1" },
                        section = section.trim(),
                        totalCopies = totalCopies,
                        availableCopies = availableCopies,
                        publicationYear = publicationYearText.toIntOrNull(),
                        coverColorHex = coverColorHex,
                        notes = notes.trim()
                    )
                    onSave(updatedBook)
                },
                modifier = Modifier.testTag("save_book_button")
            ) {
                Text("Save to Library")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
