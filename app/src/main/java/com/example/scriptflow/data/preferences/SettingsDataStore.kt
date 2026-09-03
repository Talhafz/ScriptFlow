package com.example.scriptflow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.scriptflow.domain.model.DisplayMode
import com.example.scriptflow.domain.model.ScreenOrientation
import com.example.scriptflow.domain.model.TeleprompterSettings
import com.example.scriptflow.domain.model.TextAlignment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val WPM = intPreferencesKey("wpm")
        val TEXT_COLOR = longPreferencesKey("text_color")
        val BACKGROUND_COLOR = longPreferencesKey("background_color")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val TEXT_ALIGNMENT = stringPreferencesKey("text_alignment")
        val MIRROR_MODE = booleanPreferencesKey("mirror_mode")
        val COUNTDOWN_SECONDS = intPreferencesKey("countdown_seconds")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val ORIENTATION = stringPreferencesKey("orientation")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val DISPLAY_MODE = stringPreferencesKey("display_mode")
    }

    val settingsFlow: Flow<TeleprompterSettings> = context.dataStore.data
        .map { preferences ->
            TeleprompterSettings(
                fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 24f,
                scrollSpeed = preferences[PreferencesKeys.SCROLL_SPEED] ?: 1.0f,
                wpm = preferences[PreferencesKeys.WPM] ?: 150,
                textColor = preferences[PreferencesKeys.TEXT_COLOR] ?: 0xFFFFFFFFL,
                backgroundColor = preferences[PreferencesKeys.BACKGROUND_COLOR] ?: 0xFF000000L,
                lineSpacing = preferences[PreferencesKeys.LINE_SPACING] ?: 1.2f,
                letterSpacing = preferences[PreferencesKeys.LETTER_SPACING] ?: 0f,
                textAlignment = preferences[PreferencesKeys.TEXT_ALIGNMENT].safeValueOf(TextAlignment.CENTER),
                mirrorMode = preferences[PreferencesKeys.MIRROR_MODE] ?: false,
                countdownSeconds = preferences[PreferencesKeys.COUNTDOWN_SECONDS] ?: 3,
                keepScreenAwake = preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] ?: true,
                orientation = preferences[PreferencesKeys.ORIENTATION].safeValueOf(ScreenOrientation.LANDSCAPE),
                hasSeenOnboarding = preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false,
                displayMode = preferences[PreferencesKeys.DISPLAY_MODE].safeValueOf(DisplayMode.VERTICAL)
            )
        }

    suspend fun updateSettings(settings: TeleprompterSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = settings.fontSize
            preferences[PreferencesKeys.SCROLL_SPEED] = settings.scrollSpeed
            preferences[PreferencesKeys.WPM] = settings.wpm
            preferences[PreferencesKeys.TEXT_COLOR] = settings.textColor
            preferences[PreferencesKeys.BACKGROUND_COLOR] = settings.backgroundColor
            preferences[PreferencesKeys.LINE_SPACING] = settings.lineSpacing
            preferences[PreferencesKeys.LETTER_SPACING] = settings.letterSpacing
            preferences[PreferencesKeys.TEXT_ALIGNMENT] = settings.textAlignment.name
            preferences[PreferencesKeys.MIRROR_MODE] = settings.mirrorMode
            preferences[PreferencesKeys.COUNTDOWN_SECONDS] = settings.countdownSeconds
            preferences[PreferencesKeys.KEEP_SCREEN_AWAKE] = settings.keepScreenAwake
            preferences[PreferencesKeys.ORIENTATION] = settings.orientation.name
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = settings.hasSeenOnboarding
            preferences[PreferencesKeys.DISPLAY_MODE] = settings.displayMode.name
        }
    }

    private inline fun <reified T : Enum<T>> String?.safeValueOf(defaultValue: T): T {
        if (this == null) return defaultValue
        return enumValues<T>().find { it.name == this } ?: defaultValue
    }
}
