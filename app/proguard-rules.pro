##############################################
# ChannelSense – ProGuard / R8 rules
##############################################
-keepattributes SourceFile,LineNumberTable

-keep class com.mahmutalperenunal.channelsense.** extends android.app.Activity
-keepclassmembers class com.mahmutalperenunal.channelsense.** extends android.app.Activity {
    public *;
}

# --- ViewModel---
-keep class com.mahmutalperenunal.channelsense.**ViewModel { *; }
-keepclassmembers class com.mahmutalperenunal.channelsense.**ViewModel { *; }

# --- Jetpack Compose ---
-dontwarn androidx.compose.**
-keep @interface androidx.compose.ui.tooling.preview.Preview

# --- Navigation Compose ---
-dontwarn androidx.navigation.**

# --- DataStore (Preferences) ---
-dontwarn androidx.datastore.**
-dontwarn kotlinx.coroutines.flow.**

# --- Kotlin / Coroutines ---
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-keepattributes *Annotation*

# --- Wifi / System API ---
-dontwarn android.net.wifi.**
-dontwarn android.net.**