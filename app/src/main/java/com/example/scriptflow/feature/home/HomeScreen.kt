package com.example.scriptflow.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptflow.R
import com.example.scriptflow.domain.model.Script
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (Long) -> Unit,
    onNavigateToTeleprompter: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToQuickStart: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Script?>(null) }
    var selectedScriptForDetails by remember { mutableStateOf<Script?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isSearchActive = (uiState as? HomeUiState.Success)?.isSearchActive == true
    val searchQuery = (uiState as? HomeUiState.Success)?.searchQuery ?: ""

    var showTopOverflowMenu by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                onHomeClick = { scope.launch { drawerState.close() } },
                onSettingsClick = { 
                    scope.launch { drawerState.close() }
                    onNavigateToSettings() 
                },
                onQuickStartClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToQuickStart()
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { 
                        if (isSearchActive) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = viewModel::onSearchQueryChanged,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Search scripts...",
                                            color = Color.White.copy(alpha = 0.4f),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.logo_scriptflow),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(color = Color.White)) {
                                            append("Script")
                                        }
                                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                            append("Flow")
                                        }
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                ) 
                            }
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = viewModel::toggleSearch) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        if (isSearchActive) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.White)
                                }
                            }
                        } else {
                            IconButton(onClick = viewModel::toggleSearch) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                            }
                            Box {
                                IconButton(onClick = { showTopOverflowMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                                }
                                HomeOverflowMenu(
                                    expanded = showTopOverflowMenu,
                                    onDismiss = { showTopOverflowMenu = false },
                                    onSortOptionSelected = viewModel::onSortOptionSelected,
                                    onBulkSelectClick = viewModel::toggleBulkSelectionMode
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Scripts") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToQuickStart,
                        icon = { Icon(Icons.Default.SettingsVoice, contentDescription = null) },
                        label = { Text("Prompter") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToSettings,
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            },
            containerColor = Color.Black
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is HomeUiState.Empty -> {
                        EmptyState(onCreateClick = { onNavigateToEditor(-1L) })
                    }
                    is HomeUiState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Scripts",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                
                                Button(
                                    onClick = { onNavigateToEditor(-1L) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.Black
                                    ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("New Script", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            CategoryChips(
                                selectedCategory = state.selectedCategory,
                                onCategorySelected = viewModel::onCategorySelected
                            )
                        }

                        if (state.isBulkSelectionMode) {
                            BulkActionBar(
                                selectedCount = state.selectedScriptIds.size,
                                onDeleteClick = viewModel::deleteSelectedScripts,
                                onCancelClick = viewModel::toggleBulkSelectionMode
                            )
                        }
                        
                        ScriptList(
                            scripts = state.scripts,
                            isBulkSelectionMode = state.isBulkSelectionMode,
                            selectedScriptIds = state.selectedScriptIds,
                            onScriptClick = { scriptId ->
                                if (state.isBulkSelectionMode) {
                                    viewModel.toggleScriptSelection(scriptId)
                                } else {
                                    selectedScriptForDetails = state.scripts.find { it.id == scriptId }
                                }
                            },
                            onPlayClick = { scriptId ->
                                val script = state.scripts.find { it.id == scriptId }
                                if (script?.title.isNullOrBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please add a title to the script before playing")
                                    }
                                } else if (script?.content.isNullOrBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please add content to the script before playing")
                                    }
                                } else {
                                    onNavigateToTeleprompter(scriptId)
                                }
                            },
                            onDeleteClick = { showDeleteDialog = it },
                            onDuplicateClick = viewModel::duplicateScript,
                            onFavoriteClick = viewModel::toggleFavorite,
                            onCategoryChange = viewModel::updateScriptCategory
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { script ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Script") },
            text = { Text("Are you sure you want to delete '${script.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteScript(script)
                    showDeleteDialog = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedScriptForDetails != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedScriptForDetails = null },
            sheetState = sheetState,
            containerColor = Color(0xFF121212),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
        ) {
            ScriptDetailsSheet(
                script = selectedScriptForDetails!!,
                onPlayClick = {
                    val id = selectedScriptForDetails!!.id
                    selectedScriptForDetails = null
                    onNavigateToTeleprompter(id)
                },
                onEditClick = {
                    val id = selectedScriptForDetails!!.id
                    selectedScriptForDetails = null
                    onNavigateToEditor(id)
                },
                onDuplicateClick = {
                    viewModel.duplicateScript(selectedScriptForDetails!!)
                    selectedScriptForDetails = null
                },
                onDeleteClick = {
                    showDeleteDialog = selectedScriptForDetails
                    selectedScriptForDetails = null
                }
            )
        }
    }
}

@Composable
fun AppDrawer(
    drawerState: DrawerState,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onQuickStartClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerState = drawerState,
        drawerContainerColor = Color(0xFF0A0A0A),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.logo_scriptflow),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "ScriptFlow",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            DrawerItem("My Scripts", Icons.Default.List, onHomeClick)
            DrawerItem("Quick Start", Icons.Default.SettingsVoice, onQuickStartClick)
            DrawerItem("Settings", Icons.Default.Settings, onSettingsClick)
            
            Spacer(Modifier.weight(1f))
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(Modifier.height(16.dp))
            
            Text(
                "About ScriptFlow",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Version 1.2.0 • Premium Edition",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.width(16.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun HomeOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onBulkSelectClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(Color(0xFF1A1A1A))
    ) {
        Text(
            "Sort scripts by:",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        DropdownMenuItem(
            text = { Text("Recently Updated", color = Color.White) },
            onClick = { onSortOptionSelected(SortOption.RECENTLY_UPDATED); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text("Title A–Z", color = Color.White) },
            onClick = { onSortOptionSelected(SortOption.TITLE_AZ); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text("Word Count", color = Color.White) },
            onClick = { onSortOptionSelected(SortOption.WORD_COUNT); onDismiss() }
        )
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        DropdownMenuItem(
            text = { Text("Bulk Select", color = Color.White) },
            onClick = { onBulkSelectClick(); onDismiss() }
        )
    }
}

@Composable
fun BulkActionBar(
    selectedCount: Int,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$selectedCount selected",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color.Black)
                }
                IconButton(onClick = onCancelClick) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun CategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Recent", "Videos", "Speeches", "Lessons")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 24.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
                modifier = Modifier.height(32.dp),
                onClick = { onCategorySelected(category) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ScriptList(
    scripts: List<Script>,
    isBulkSelectionMode: Boolean,
    selectedScriptIds: Set<Long>,
    onScriptClick: (Long) -> Unit,
    onPlayClick: (Long) -> Unit,
    onDeleteClick: (Script) -> Unit,
    onDuplicateClick: (Script) -> Unit,
    onFavoriteClick: (Script) -> Unit,
    onCategoryChange: (Script, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(scripts, key = { it.id }) { script ->
            ScriptCard(
                script = script,
                isSelected = selectedScriptIds.contains(script.id),
                isBulkSelectionMode = isBulkSelectionMode,
                onClick = { onScriptClick(script.id) },
                onPlayClick = { onPlayClick(script.id) },
                onDeleteClick = { onDeleteClick(script) },
                onDuplicateClick = { onDuplicateClick(script) },
                onFavoriteClick = { onFavoriteClick(script) },
                onCategoryChange = { onCategoryChange(script, it) }
            )
        }
    }
}

@Composable
fun ScriptCard(
    script: Script,
    isSelected: Boolean,
    isBulkSelectionMode: Boolean,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCategoryChange: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFF121212)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBulkSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(8.dp))
            }

            // File Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = script.title.ifBlank { "Untitled Script" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                Text(
                    text = "Updated ${getTimeAgo(script.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
                if (script.category != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = script.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Favorite Icon
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (script.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (script.isFavorite) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert, 
                        contentDescription = "Menu",
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
                DropdownMenu(
                    expanded = showMenu, 
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = Color.White) },
                        onClick = { showMenu = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Set Category", color = Color.White) },
                        onClick = { showMenu = false; showCategoryMenu = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = Color.White) },
                        onClick = { showMenu = false; onDuplicateClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDeleteClick() }
                    )
                }
                
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    val categories = listOf("None", "Videos", "Speeches", "Lessons")
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = Color.White) },
                            onClick = { onCategoryChange(cat); showCategoryMenu = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScriptDetailsSheet(
    script: Script,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val wordCount = remember(script.content) { 
        script.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size 
    }
    val charCount = script.content.length
    val estMinutes = (wordCount / 150.0).let { if (it < 1.0) 1 else it.toInt() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = script.title.ifBlank { "Untitled Script" },
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
            color = Color.White
        )
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailItem(label = "Words", value = wordCount.toString(), modifier = Modifier.weight(1f))
            DetailItem(label = "Characters", value = charCount.toString(), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailItem(label = "Est. Reading", value = "$estMinutes min", modifier = Modifier.weight(1f))
            DetailItem(label = "Modified", value = dateFormat.format(Date(script.updatedAt)), modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "PREVIEW",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(8.dp))
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Text(
                text = script.content.ifBlank { "No content" },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onPlayClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("START PROMPTER", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.Black)
        }
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit", color = Color.White)
            }
            
            IconButton(
                onClick = onDuplicateClick,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color.White)
            }
            
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
fun EmptyState(onCreateClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF121212), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "No scripts yet", 
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Create your first script and start presenting with confidence.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onCreateClick,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Script", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                }
            }
        }
    }
}

fun getTimeAgo(time: Long): String {
    val diff = System.currentTimeMillis() - time
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val df = SimpleDateFormat("MMM dd", Locale.getDefault())
            df.format(Date(time))
        }
    }
}
