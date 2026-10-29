# San Siri ProGuard / R8 Rules
# Package: com.anil.sansiri

# ------------------------------------------------------------------------------
# 1. San Siri App Data Models & ViewModels
# ------------------------------------------------------------------------------

# Keep data models used for Firestore / Serialization / Reflection
-keep class com.anil.sansiri.data.model.** { *; }
-keepclassmembers class com.anil.sansiri.data.model.** { *; }

# Keep ViewModels and UI state classes
-keep class com.anil.sansiri.ui.** { *; }
-keepclassmembers class com.anil.sansiri.ui.** { *; }

# ------------------------------------------------------------------------------
# 2. Firebase & Google Play Services Rules
# ------------------------------------------------------------------------------

# Firebase Auth, Firestore, Realtime Database
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.database.** { *; }
-keep class com.google.firebase.components.** { *; }
-keep class com.google.firebase.provider.** { *; }
-dontwarn com.google.firebase.**

# Firestore data model serialization (keep fields & methods with @PropertyName)
-keepattributes Signature, *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Google Play Services
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# ------------------------------------------------------------------------------
# 3. Jetpack Compose Rules
# ------------------------------------------------------------------------------

-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Prevent shrinking Compose inspectable fields & composable Lambda classes
-keep class **$lambda** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ------------------------------------------------------------------------------
# 4. Kotlin Coroutines Rules
# ------------------------------------------------------------------------------

-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Coroutine Debugging and Exception handling
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# ------------------------------------------------------------------------------
# 5. Kotlin Reflection & Serialization Rules
# ------------------------------------------------------------------------------

# Kotlin Serialization
-keepattributes *Annotation*, ElementValueAttribute, AnnotationDefault
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class com.anil.sansiri.** {
    *** Companion;
}

# Kotlin Metadata and Reflection
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.**
