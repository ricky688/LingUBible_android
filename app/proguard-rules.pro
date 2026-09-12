# LingUBible ProGuard / R8 Rules

# Preserve Kotlin Reflection & Serialization metadata
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Appwrite Models & Serialization
-keep class io.appwrite.models.** { *; }
-keepclassmembers class io.appwrite.models.** { *; }

# Domain Models
-keep class com.lingubible.app.domain.model.** { *; }
-keepclassmembers class com.lingubible.app.domain.model.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# Vico Charting
-keep class com.patrykandpatrick.vico.** { *; }
