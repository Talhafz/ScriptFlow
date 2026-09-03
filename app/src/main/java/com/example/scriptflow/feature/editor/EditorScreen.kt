package com.example.scriptflow.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    onNavigateToTeleprompter: (Long) -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTitleDialog by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFindReplaceDialog by remember { mutableStateOf(false) }
    
    // Toolbar Menus
    var showStyleMenu by remember { mutableStateOf(false) }
    var showFormatMenu by remember { mutableStateOf(false) }
    var showAlignMenu by remember { mutableStateOf(false) }
    var showInsertMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.title.ifBlank { "Untitled Script" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.clickable { showTitleDialog = true }
                            )
                            if (uiState.category != null) {
                                Text(
                                    text = uiState.category!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.forceSave { onBack() } }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::toggleFavorite) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = { showCategoryMenu = true }) {
                            Icon(Icons.Default.Category, contentDescription = "Category", tint = Color.White.copy(alpha = 0.6f))
                            DropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false },
                                modifier = Modifier.background(Color(0xFF1A1A1A))
                            ) {
                                val categories = listOf("None", "Videos", "Speeches", "Lessons")
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = Color.White) },
                                        onClick = { 
                                            viewModel.onCategoryChanged(cat)
                                            showCategoryMenu = false 
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { 
                            viewModel.forceSave { newId -> onNavigateToTeleprompter(newId) }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
                
                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabItem("Editor", selectedTab == 0, modifier = Modifier.weight(1f)) { selectedTab = 0 }
                    TabItem("Preview", selectedTab == 1, modifier = Modifier.weight(1f)) { selectedTab = 1 }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
                    .navigationBarsPadding()
            ) {
                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Words", uiState.wordCount.toString())
                    StatItem("Characters", uiState.characterCount.toString())
                    StatItem("Est. Time", uiState.estimatedDuration)
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                
                // Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Style
                    Box {
                        EditorActionItem(Icons.Default.TextFormat, "Style", onClick = { showStyleMenu = true })
                        DropdownMenu(expanded = showStyleMenu, onDismissRequest = { showStyleMenu = false }, modifier = Modifier.background(Color(0xFF1A1A1A))) {
                            DropdownMenuItem(text = { Text("Normal", color = Color.White) }, onClick = { showStyleMenu = false })
                            DropdownMenuItem(text = { Text("Heading", color = Color.White) }, onClick = { viewModel.applyFormatting("# ", ""); showStyleMenu = false })
                            DropdownMenuItem(text = { Text("Cue/Direction", color = Color.White) }, onClick = { viewModel.applyFormatting("(", ")"); showStyleMenu = false })
                        }
                    }
                    
                    // Format
                    Box {
                        EditorActionItem(Icons.Default.TextFields, "Format", onClick = { showFormatMenu = true })
                        DropdownMenu(expanded = showFormatMenu, onDismissRequest = { showFormatMenu = false }, modifier = Modifier.background(Color(0xFF1A1A1A))) {
                            DropdownMenuItem(text = { Text("Bold", color = Color.White, fontWeight = FontWeight.Bold) }, onClick = { viewModel.applyFormatting("**"); showFormatMenu = false })
                            DropdownMenuItem(text = { Text("Italic", color = Color.White, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }, onClick = { viewModel.applyFormatting("*"); showFormatMenu = false })
                            DropdownMenuItem(text = { Text("Underline", color = Color.White, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline) }, onClick = { viewModel.applyFormatting("__"); showFormatMenu = false })
                        }
                    }
                    
                    // Align
                    Box {
                        EditorActionItem(Icons.AutoMirrored.Filled.FormatAlignLeft, "Align", onClick = { showAlignMenu = true })
                        DropdownMenu(expanded = showAlignMenu, onDismissRequest = { showAlignMenu = false }, modifier = Modifier.background(Color(0xFF1A1A1A))) {
                            DropdownMenuItem(text = { Text("Left", color = Color.White) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, null, tint = Color.White) }, onClick = { viewModel.applyAlignment("LEFT"); showAlignMenu = false })
                            DropdownMenuItem(text = { Text("Center", color = Color.White) }, leadingIcon = { Icon(Icons.Default.FormatAlignCenter, null, tint = Color.White) }, onClick = { viewModel.applyAlignment("CENTER"); showAlignMenu = false })
                            DropdownMenuItem(text = { Text("Right", color = Color.White) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatAlignRight, null, tint = Color.White) }, onClick = { viewModel.applyAlignment("RIGHT"); showAlignMenu = false })
                        }
                    }
                    
                    // Insert
                    Box {
                        EditorActionItem(Icons.Default.AddBox, "Insert", onClick = { showInsertMenu = true })
                        DropdownMenu(expanded = showInsertMenu, onDismissRequest = { showInsertMenu = false }, modifier = Modifier.background(Color(0xFF1A1A1A))) {
                            DropdownMenuItem(text = { Text("Pause Marker", color = Color.White) }, onClick = { viewModel.insertText("[PAUSE]"); showInsertMenu = false })
                            DropdownMenuItem(text = { Text("Section Break", color = Color.White) }, onClick = { viewModel.insertText("\n---\n"); showInsertMenu = false })
                            DropdownMenuItem(text = { Text("Cue Note", color = Color.White) }, onClick = { viewModel.insertText("[CUE: ]"); showInsertMenu = false })
                        }
                    }
                    
                    // More
                    Box {
                        EditorActionItem(Icons.Default.MoreHoriz, "More", onClick = { showMoreMenu = true })
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }, modifier = Modifier.background(Color(0xFF1A1A1A))) {
                            DropdownMenuItem(text = { Text("Undo", color = Color.White) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Undo, null, tint = Color.White) }, onClick = { viewModel.undo(); showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Redo", color = Color.White) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Redo, null, tint = Color.White) }, onClick = { viewModel.redo(); showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Find & Replace", color = Color.White) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) }, onClick = { showFindReplaceDialog = true; showMoreMenu = false })
                            DropdownMenuItem(text = { Text("Clear Formatting", color = Color.White) }, leadingIcon = { Icon(Icons.Default.FormatClear, null, tint = Color.White) }, onClick = { viewModel.clearFormatting(); showMoreMenu = false })
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .imePadding()
        ) {
            if (selectedTab == 0) {
                BasicTextField(
                    value = uiState.content,
                    onValueChange = viewModel::onContentChanged,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 32.sp,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (uiState.content.text.isEmpty()) {
                            Text(
                                "Start writing your script...",
                                color = Color.White.copy(alpha = 0.2f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 20.sp
                            )
                        }
                        innerTextField()
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val previewText = uiState.content.text.parseMarkdown(
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            lineHeight = 48.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }

    if (showTitleDialog) {
        var titleInput by remember { mutableStateOf(uiState.title) }
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            containerColor = Color(0xFF121212),
            shape = RoundedCornerShape(24.dp),
            title = { 
                Text(
                    "Edit Script Title", 
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                ) 
            },
            text = {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title", color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onTitleChanged(titleInput)
                        showTitleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTitleDialog = false }
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    if (showFindReplaceDialog) {
        var findInput by remember { mutableStateOf("") }
        var replaceInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFindReplaceDialog = false },
            containerColor = Color(0xFF121212),
            shape = RoundedCornerShape(24.dp),
            title = { Text("Find & Replace", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = findInput,
                        onValueChange = { findInput = it },
                        label = { Text("Find") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = replaceInput,
                        onValueChange = { replaceInput = it },
                        label = { Text("Replace with") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.findReplace(findInput, replaceInput)
                        showFindReplaceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Replace All", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFindReplaceDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
fun TabItem(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f)
            )
            if (isSelected) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
fun EditorActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
    }
}
