# LingUBible ProGuard / R8 Rules

# Preserve Kotlin Reflection & Serialization metadata
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Appwrite's models use map conversion, not reflection-based model decoding.
# Domain models and Vico are directly referenced. Serialization and coroutines
# supply consumer rules; retaining whole packages blocks R8 unnecessarily.

# Query.toString() uses Gson reflection on these unannotated wire fields.
# Renaming/removing them produces invalid queries even though the app compiles.
-keep class io.appwrite.Query { *; }
-keepclassmembers class io.appwrite.Query {
    java.lang.String method;
    java.lang.String attribute;
    java.util.List values;
}

# Operator.toString() uses the same reflection-based wire format.
-keep class io.appwrite.Operator { *; }
-keepclassmembers class io.appwrite.Operator {
    java.lang.String method;
    java.util.List values;
}

-keep class io.appwrite.models.** { *; }
-keepclassmembers class io.appwrite.models.** { *; }

# AppwriteClientProvider customizes this field via getDeclaredField("http").
-keepclassmembers class io.appwrite.Client {
    okhttp3.OkHttpClient http;
}
