package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Book
import com.example.data.Member
import com.example.ui.components.AddEditBookDialog
import com.example.ui.components.AddEditMemberDialog
import com.example.ui.components.BookDetailDialog
import com.example.ui.components.LendBookDialog
import com.example.ui.components.MemberDetailDialog
import com.example.ui.screens.BooksScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoansScreen
import com.example.ui.screens.MembersScreen
import com.example.ui.screens.RackLocatorScreen
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryApp(
    viewModel: LibraryViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handled
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        NotificationHelper.createNotificationChannel(context)
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // UI States
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val books by viewModel.filteredBooks.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val racks by viewModel.racks.collectAsStateWithLifecycle()
    val members by viewModel.filteredMembers.collectAsStateWithLifecycle()
    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
    val activeLoans by viewModel.activeLoans.collectAsStateWithLifecycle()
    val overdueLoans by viewModel.overdueLoans.collectAsStateWithLifecycle()
    val dueSoonLoans by viewModel.dueSoonLoans.collectAsStateWithLifecycle()

    val bookSearchQuery by viewModel.bookSearchQuery.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val selectedRack by viewModel.selectedRack.collectAsStateWithLifecycle()
    val availabilityFilter by viewModel.availabilityFilter.collectAsStateWithLifecycle()
    val memberSearchQuery by viewModel.memberSearchQuery.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    // Dialog States
    var showAddBookDialog by remember { mutableStateOf(false) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var selectedBookDetail by remember { mutableStateOf<Book?>(null) }

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var selectedMemberDetail by remember { mutableStateOf<Member?>(null) }

    var showLendDialog by remember { mutableStateOf(false) }
    var preselectedLendBookId by remember { mutableStateOf<Long?>(null) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentTab.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.checkAndSendNotificationsNow() },
                        modifier = Modifier.testTag("app_bar_notification_action")
                    ) {
                        BadgedBox(
                            badge = {
                                if (stats.overdueLoans > 0 || stats.dueSoonLoans > 0) {
                                    Badge {
                                        Text("${stats.overdueLoans + stats.dueSoonLoans}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Check Notifications"
                            )
                        }
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("app_bar_settings_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Database Backup"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.DASHBOARD,
                    onClick = { viewModel.selectTab(NavigationTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Overview") },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.BOOKS,
                    onClick = { viewModel.selectTab(NavigationTab.BOOKS) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Books") },
                    label = { Text("Books") },
                    modifier = Modifier.testTag("nav_books")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.LOANS,
                    onClick = { viewModel.selectTab(NavigationTab.LOANS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (stats.overdueLoans > 0) {
                                    Badge { Text("${stats.overdueLoans}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = "Lending")
                        }
                    },
                    label = { Text("Lend") },
                    modifier = Modifier.testTag("nav_loans")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.MEMBERS,
                    onClick = { viewModel.selectTab(NavigationTab.MEMBERS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Members") },
                    label = { Text("Members") },
                    modifier = Modifier.testTag("nav_members")
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.RACK_LOCATOR,
                    onClick = { viewModel.selectTab(NavigationTab.RACK_LOCATOR) },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Find Rack") },
                    label = { Text("Racks") },
                    modifier = Modifier.testTag("nav_rack_locator")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.DASHBOARD -> DashboardScreen(
                        stats = stats,
                        overdueLoans = overdueLoans,
                        dueSoonLoans = dueSoonLoans,
                        activeLoans = activeLoans,
                        onOpenAddBook = { showAddBookDialog = true },
                        onOpenLendBook = {
                            preselectedLendBookId = null
                            showLendDialog = true
                        },
                        onOpenAddMember = { showAddMemberDialog = true },
                        onCheckNotifications = { viewModel.checkAndSendNotificationsNow() },
                        onReturnBook = { loanId, title -> viewModel.returnBook(loanId, title) },
                        onNavigateToBooks = { viewModel.selectTab(NavigationTab.BOOKS) },
                        onNavigateToLoans = { viewModel.selectTab(NavigationTab.LOANS) },
                        onNavigateToRackLocator = { viewModel.selectTab(NavigationTab.RACK_LOCATOR) },
                        onOpenSettings = { showSettingsDialog = true }
                    )

                    NavigationTab.BOOKS -> BooksScreen(
                        books = books,
                        genres = genres,
                        racks = racks,
                        searchQuery = bookSearchQuery,
                        selectedGenre = selectedGenre,
                        selectedRack = selectedRack,
                        availabilityFilter = availabilityFilter,
                        onSearchQueryChange = { viewModel.updateBookSearchQuery(it) },
                        onGenreSelect = { viewModel.selectGenre(it) },
                        onRackSelect = { viewModel.selectRack(it) },
                        onAvailabilityFilterChange = { viewModel.setAvailabilityFilter(it) },
                        onBookClick = { selectedBookDetail = it },
                        onLendClick = {
                            preselectedLendBookId = it.id
                            showLendDialog = true
                        },
                        onAddBookClick = { showAddBookDialog = true }
                    )

                    NavigationTab.LOANS -> LoansScreen(
                        allLoans = allLoans,
                        onReturnBook = { loanId, title -> viewModel.returnBook(loanId, title) },
                        onDeleteLoan = { viewModel.deleteLoan(it) },
                        onLendBookClick = {
                            preselectedLendBookId = null
                            showLendDialog = true
                        }
                    )

                    NavigationTab.MEMBERS -> MembersScreen(
                        members = members,
                        searchQuery = memberSearchQuery,
                        onSearchQueryChange = { viewModel.updateMemberSearchQuery(it) },
                        onMemberClick = { selectedMemberDetail = it },
                        onAddMemberClick = { showAddMemberDialog = true }
                    )

                    NavigationTab.RACK_LOCATOR -> RackLocatorScreen(
                        books = books,
                        racks = if (racks.isEmpty()) listOf("Rack A", "Rack B", "Rack C") else racks,
                        onBookClick = { selectedBookDetail = it },
                        onLendClick = {
                            preselectedLendBookId = it.id
                            showLendDialog = true
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Book Dialog
    if (showAddBookDialog || bookToEdit != null) {
        AddEditBookDialog(
            initialBook = bookToEdit,
            onDismiss = {
                showAddBookDialog = false
                bookToEdit = null
            },
            onSave = { book ->
                viewModel.saveBook(book)
                showAddBookDialog = false
                bookToEdit = null
            }
        )
    }

    // Book Detail Dialog
    selectedBookDetail?.let { book ->
        BookDetailDialog(
            book = book,
            onDismiss = { selectedBookDetail = null },
            onEdit = {
                bookToEdit = book
                selectedBookDetail = null
            },
            onLend = {
                preselectedLendBookId = book.id
                showLendDialog = true
                selectedBookDetail = null
            },
            onDelete = {
                viewModel.deleteBook(book)
                selectedBookDetail = null
            }
        )
    }

    // Add / Edit Member Dialog
    if (showAddMemberDialog || memberToEdit != null) {
        AddEditMemberDialog(
            initialMember = memberToEdit,
            onDismiss = {
                showAddMemberDialog = false
                memberToEdit = null
            },
            onSave = { member ->
                viewModel.registerMember(member)
                showAddMemberDialog = false
                memberToEdit = null
            }
        )
    }

    // Member Detail Dialog
    selectedMemberDetail?.let { member ->
        val memberLoans = activeLoans.filter { it.memberId == member.id }
        MemberDetailDialog(
            member = member,
            activeLoansForMember = memberLoans,
            onDismiss = { selectedMemberDetail = null },
            onEdit = {
                memberToEdit = member
                selectedMemberDetail = null
            },
            onDelete = {
                viewModel.deleteMember(member)
                selectedMemberDetail = null
            },
            onReturnBook = { loanId, title ->
                viewModel.returnBook(loanId, title)
            }
        )
    }

    // Lend Book Dialog
    if (showLendDialog) {
        LendBookDialog(
            availableBooks = books,
            members = members,
            preselectedBookId = preselectedLendBookId,
            onDismiss = {
                showLendDialog = false
                preselectedLendBookId = null
            },
            onLend = { bookId, memberId, dueDate, notes ->
                viewModel.lendBook(bookId, memberId, dueDate, notes)
                showLendDialog = false
                preselectedLendBookId = null
            }
        )
    }

    // Settings Dialog (Backup & Restore)
    if (showSettingsDialog) {
        com.example.ui.components.SettingsDialog(
            stats = stats,
            currentThemeMode = themeMode,
            onThemeModeChange = { viewModel.setThemeMode(it) },
            onDismiss = { showSettingsDialog = false },
            onExportBackup = { viewModel.exportDatabaseBackup() },
            onRestoreBackup = { json, replaceExisting ->
                viewModel.restoreDatabase(json, replaceExisting)
            },
            onTestNotifications = { viewModel.checkAndSendNotificationsNow() }
        )
    }
}
