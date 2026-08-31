package com.example.scriptflow.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptflow.domain.model.ScreenOrientation
import com.example.scriptflow.domain.model.TextAlignment
import com.example.scriptflow.feature.settings.components.LivePreviewCard
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::resetToDefaults) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Defaults")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Live Preview Section
                Text("Live Preview", style = MaterialTheme.typography.titleMedium)
                LivePreviewCard(settings = uiState.settings)

                // Playback Section
                SettingsSection(title = "Playback") {
                    SliderSetting(
                        label = "Words Per Minute",
                        value = uiState.settings.wpm.toFloat(),
                        range = 80f..250f,
                        valueDisplay = { "${it.toInt()} WPM" },
                        onValueChange = { viewModel.updateWpm(it.toInt()) }
                    )
                    
                    DropdownSetting(
                        label = "Countdown Duration",
                        selectedOption = "${uiState.settings.countdownSeconds}s",
                        options = listOf("0s", "3s", "5s", "10s"),
                        onOptionSelected = { viewModel.updateCountdownSeconds(it.removeSuffix("s").toInt()) }
                    )
                }

                // Appearance Section
                SettingsSection(title = "Appearance") {
                    Text("Theme Presets", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetTheme.entries.forEach { preset ->
                            FilterChip(
                                selected = uiState.activePreset == preset,
                                onClick = { viewModel.applyThemePreset(preset) },
                                label = { 
                                    Text(preset.name.lowercase().replaceFirstChar { 
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                                    }) 
                                }
                            )
                        }
                    }

                    SliderSetting(
                        label = "Font Size",
                        value = uiState.settings.fontSize,
                        range = 20f..72f,
                        valueDisplay = { "${it.toInt()} sp" },
                        onValueChange = viewModel::updateFontSize
                    )

                    SliderSetting(
                        label = "Line Spacing",
                        value = uiState.settings.lineSpacing,
                        range = 1.0f..2.5f,
                        valueDisplay = { String.format(Locale.getDefault(), "%.1fx", it) },
                        onValueChange = viewModel::updateLineSpacing
                    )

                    AlignmentSetting(
                        selected = uiState.settings.textAlignment,
                        onAlignmentSelected = viewModel::updateTextAlignment
                    )
                }

                // Display Category
                SettingsSection(title = "Display") {
                    SwitchSetting(
                        label = "Mirror Mode",
                        description = "Horizontal flip for beam-splitter glass",
                        checked = uiState.settings.mirrorMode,
                        onCheckedChange = viewModel::toggleMirrorMode
                    )
                    
                    SwitchSetting(
                        label = "Keep Screen Awake",
                        checked = uiState.settings.keepScreenAwake,
                        onCheckedChange = viewModel::toggleKeepScreenAwake
                    )

                    OrientationSetting(
                        selected = uiState.settings.orientation,
                        onOrientationSelected = viewModel::updateOrientation
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueDisplay: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(valueDisplay(value), style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SwitchSetting(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AlignmentSetting(
    selected: TextAlignment,
    onAlignmentSelected: (TextAlignment) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Text Alignment", style = MaterialTheme.typography.bodyMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            TextAlignment.entries.forEachIndexed { index, alignment ->
                SegmentedButton(
                    selected = selected == alignment,
                    onClick = { onAlignmentSelected(alignment) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = TextAlignment.entries.size)
                ) {
                    Text(alignment.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
fun OrientationSetting(
    selected: ScreenOrientation,
    onOrientationSelected: (ScreenOrientation) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Screen Orientation", style = MaterialTheme.typography.bodyMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ScreenOrientation.entries.forEachIndexed { index, orientation ->
                SegmentedButton(
                    selected = selected == orientation,
                    onClick = { onOrientationSelected(orientation) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ScreenOrientation.entries.size)
                ) {
                    Text(orientation.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
fun DropdownSetting(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selectedOption)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
