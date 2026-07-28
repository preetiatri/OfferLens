# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# ========================================
# KOTLIN
# ========================================
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ========================================
# KOTLINX COROUTINES
# ========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ========================================
# JETPACK COMPOSE
# ========================================

# Keep all Composable functions
-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Composable interface * { *; }

# ========================================
# FIREBASE
# ========================================
# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.Timestamp { *; }

# Firebase Functions
-keep class com.google.firebase.functions.** { *; }
-keepclassmembers class com.google.firebase.functions.** { *; }

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }

# ========================================
# ROOM DATABASE
# ========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room DAO methods
-keepclassmembers,allowobfuscation class * extends androidx.room.RoomDatabase {
    public abstract ** *Dao();
}

# Keep entity classes
-keep @androidx.room.Entity class * { *; }
-keep class com.offerlens.data.local.** { *; }

# ========================================
# HILT / DAGGER
# ========================================
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **_Provide** { *; }

# Keep Hilt annotations
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# ========================================
# DATA MODELS
# ========================================
# Keep all data classes used with Firebase.
# Full keeps (including methods): Firestore's deserializer discovers property names
# through getters, so the wildcard fields+constructor rule below is NOT enough -
# R8 renaming a model's getters makes its documents silently deserialize empty in
# release builds only.
-keep class com.offerlens.data.Offer { *; }
-keep class com.offerlens.data.OfferTier { *; }
-keep class com.offerlens.data.User { *; }
-keep class com.offerlens.data.PaymentMethod { *; }

# Keep all fields in data classes
-keepclassmembers class com.offerlens.data.** {
    <fields>;
    <init>(...);
}

# Keep PropertyName annotations for Firestore
-keepattributes *Annotation*
-keep class com.google.firebase.firestore.PropertyName
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# ========================================
# TIMBER
# ========================================
-dontwarn org.jetbrains.annotations.**
-keep class timber.log.** { *; }

# ========================================
# GSON (if used)
# ========================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ========================================
# KOTLIN SERIALIZATION (if used)
# ========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========================================
# GENERAL ANDROID
# ========================================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========================================
# REMOVE LOGGING IN RELEASE
# ========================================
# Remove all debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep Timber error and warning logs even in release
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ========================================
# OPTIMIZATION
# ========================================
# Enable optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# ========================================
# WARNINGS TO IGNORE
# ========================================
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ========================================
# DATASTORE
# ========================================
-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.** { *; }

# ========================================
# SQLCIPHER
# ========================================
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-keep class net.sqlcipher.database.SQLiteDatabase { *; }
-keepattributes *Annotation*
-keepclassmembers class net.sqlcipher.** { *; }
-keepclassmembers class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**
-dontwarn androidx.sqlite.db.SupportSQLite*

# ========================================
# ADMOB & GOOGLE PLAY SERVICES
# ========================================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep public class com.google.android.gms.ads.MobileAds {
    public *;
}

# ========================================
# GOOGLE PLAY BILLING
# ========================================
-keep class com.android.billingclient.api.** { *; }
-keep class com.google.android.gms.internal.play_billing.** { *; }

# ========================================
# COIL IMAGE LOADER
# ========================================
-keep class coil.** { *; }
-keepclassmembers class coil.** { *; }
