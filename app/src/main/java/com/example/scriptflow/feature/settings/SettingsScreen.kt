package com.example.scriptflow.feature.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::resetToDefaults) {
                        Icon(
                            Icons.Default.Refresh, 
                            contentDescription = "Reset Defaults",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Live Preview Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "Live Preview", 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Real-time feedback",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LivePreviewCard(settings = uiState.settings)
                }

                // PROMPTER Category
                SettingsSection(title = "Prompter") {
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

                    SliderSetting(
                        label = "Letter Spacing",
                        value = uiState.settings.letterSpacing,
                        range = -2f..10f,
                        valueDisplay = { String.format(Locale.getDefault(), "%.1f", it) },
                        onValueChange = viewModel::updateLetterSpacing
                    )

                    AlignmentSetting(
                        selected = uiState.settings.textAlignment,
                        onAlignmentSelected = viewModel::updateTextAlignment
                    )
                }

                // APPEARANCE Category
                SettingsSection(title = "Appearance") {
                    Text("Theme Presets", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PresetTheme.entries.filter { it != PresetTheme.CUSTOM }.forEach { preset ->
                            ThemeSwatch(
                                preset = preset,
                                isSelected = uiState.activePreset == preset,
                                onClick = { viewModel.applyThemePreset(preset) }
                            )
                        }
                    }
                }

                // PLAYBACK Category
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

                    SwitchSetting(
                        label = "Mirror Mode",
                        description = "Horizontal flip for prompter glass",
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
fun ThemeSwatch(
    preset: PresetTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor: Color
    val textColor: Color
    
    when (preset) {
        PresetTheme.CLASSIC -> {
            bgColor = Color.Black
            textColor = Color.White
        }
        PresetTheme.DARK -> {
            bgColor = Color(0xFF151517)
            textColor = Color(0xFFA7A7AC)
        }
        PresetTheme.HIGH_CONTRAST -> {
            bgColor = Color.Black
            textColor = Color(0xFFFFD54A) // Using WarningHighlight for pro contrast
        }
        PresetTheme.CUSTOM -> {
            bgColor = Color(0xFF1C1C1F)
            textColor = Color.White
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(60.dp),
            shape = MaterialTheme.shapes.small,
            color = bgColor,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "Aa", 
                    color = textColor, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = preset.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(20.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        content()
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
