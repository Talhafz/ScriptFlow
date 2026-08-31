package com.example.scriptflow.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Script?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("ScriptFlow") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEditor(-1L) }) {
                Icon(Icons.Default.Add, contentDescription = "Create Script")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HomeUiState.Empty -> {
                    EmptyState(onCreateClick = { onNavigateToEditor(-1L) })
                }
                is HomeUiState.Success -> {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged
                    )
                    ScriptList(
                        scripts = state.scripts,
                        onScriptClick = onNavigateToEditor,
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
                        onDuplicateClick = viewModel::duplicateScript
                    )
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
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search scripts...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun ScriptList(
    scripts: List<Script>,
    onScriptClick: (Long) -> Unit,
    onPlayClick: (Long) -> Unit,
    onDeleteClick: (Script) -> Unit,
    onDuplicateClick: (Script) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(scripts, key = { it.id }) { script ->
            ScriptCard(
                script = script,
                onClick = { onScriptClick(script.id) },
                onPlayClick = { onPlayClick(script.id) },
                onDeleteClick = { onDeleteClick(script) },
                onDuplicateClick = { onDuplicateClick(script) }
            )
        }
    }
}

@Composable
fun ScriptCard(
    script: Script,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = script.title.ifBlank { "Untitled Script" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${script.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size} words | ${dateFormat.format(Date(script.updatedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onPlayClick) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = { showMenu = false; onDuplicateClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { showMenu = false; onDeleteClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(onCreateClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No scripts found", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onCreateClick) {
                Text("Create Script")
            }
        }
    }
}
