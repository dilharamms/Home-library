package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book
import com.example.ui.AvailabilityFilter
import com.example.ui.theme.AvailableGreen
import com.example.ui.theme.OverdueRed

@Composable
fun BooksScreen(
    books: List<Book>,
    genres: List<String>,
    racks: List<String>,
    searchQuery: String,
    selectedGenre: String,
    selectedRack: String,
    availabilityFilter: AvailabilityFilter,
    onSearchQueryChange: (String) -> Unit,
    onGenreSelect: (String) -> Unit,
    onRackSelect: (String) -> Unit,
    onAvailabilityFilterChange: (AvailabilityFilter) -> Unit,
    onBookClick: (Book) -> Unit,
    onLendClick: (Book) -> Unit,
    onAddBookClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBookClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_book")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Stock New Book")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("books_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search title, author, ISBN, or shelf...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("book_search_bar")
            )

            // Horizontal Filter Chips: Genres & Racks
            Column(
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // Genres Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedGenre.isEmpty(),
                        onClick = { onGenreSelect("") },
                        label = { Text("All Genres", fontSize = 12.sp) }
                    )
                    genres.forEach { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { onGenreSelect(genre) },
                            label = { Text(genre, fontSize = 12.sp) }
                        )
                    }
                }

                // Racks Row (Core location filter)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRack.isEmpty(),
                        onClick = { onRackSelect("") },
                        label = { Text("All Racks", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                    racks.forEach { rack ->
                        FilterChip(
                            selected = selectedRack == rack,
                            onClick = { onRackSelect(rack) },
                            label = { Text(rack, fontSize = 12.sp) }
                        )
                    }

                    // Availability toggles
                    FilterChip(
                        selected = availabilityFilter == AvailabilityFilter.AVAILABLE_ONLY,
                        onClick = {
                            onAvailabilityFilterChange(
                                if (availabilityFilter == AvailabilityFilter.AVAILABLE_ONLY)
                                    AvailabilityFilter.ALL
                                else AvailabilityFilter.AVAILABLE_ONLY
                            )
                        },
                        label = { Text("In Stock Only", fontSize = 12.sp) }
                    )
                }
            }

            // Results count header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${books.size} Book${if (books.size == 1) "" else "s"} cataloged",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Books List
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching books found",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Try adjusting your search query, genre, or rack filter.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(books, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book) },
                            onLendClick = { onLendClick(book) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onLendClick: () -> Unit
) {
    val spineColor = try {
        Color(android.graphics.Color.parseColor(book.coverColorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("book_item_${book.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Book Spine indicator
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(115.dp)
                    .background(spineColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "by ${book.author}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stock badge
                    Surface(
                        color = if (book.availableCopies > 0) AvailableGreen.copy(alpha = 0.15f)
                        else OverdueRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${book.availableCopies}/${book.totalCopies} in stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (book.availableCopies > 0) AvailableGreen else OverdueRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Location Badge (Rack & Shelf Highlight - Core Requirement)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${book.rackId} • ${book.shelfNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = book.genre,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    if (book.availableCopies > 0) {
                        OutlinedButton(
                            onClick = onLendClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lend", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
