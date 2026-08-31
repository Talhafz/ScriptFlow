package com.example.scriptflow.feature.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    onNavigateToTeleprompter: (Long) -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChanged,
                        placeholder = { Text("Script Title") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.forceSave(onBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (uiState.title.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please add a title before starting the teleprompter")
                                }
                            } else if (uiState.content.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please add content before starting the teleprompter")
                                }
                            } else {
                                viewModel.forceSave { onNavigateToTeleprompter(uiState.scriptId) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Teleprompter")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.wordCount} words | ${uiState.characterCount} chars",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Est: ${uiState.estimatedDuration}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else if (!uiState.hasUnsavedChanges) {
                        Text("Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BasicTextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChanged,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (uiState.content.isEmpty()) {
                        Text(
                            "Start typing your script here...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
