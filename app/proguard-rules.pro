# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt / Dagger
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# ScriptFlow Data Entities & Domain Models
-keep class com.example.scriptflow.data.local.entity.** { *; }
-keep class com.example.scriptflow.domain.model.** { *; }

# DataStore
-keepclassmembers class **.PreferencesKeys {
    ** *;
}
