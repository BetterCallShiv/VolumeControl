# Ultra-aggressive ProGuard rule
# Block all Kotlin standard library classes
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn kotlin.jvm.**
-dontwarn kotlin.internal.**
-dontnote kotlin.**

# Remove all unused Kotlin classes completely
-assumenosideeffects class kotlin.** { *; }
-assumenosideeffects class kotlinx.** { *; }

# Remove debug and logging code
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Keep main application class
-keep class com.bettercallshiv.volumecontrol.MainActivity {
    public *;
}

# Keep essential Android framework classes
-keep class android.app.Application
-keep class android.app.Activity
-keep class androidx.appcompat.app.AppCompatActivity

# Keep Material Design components we actually use
-keep class com.google.android.material.slider.Slider {
    public *;
}
-keep class com.google.android.material.card.MaterialCardView {
    public *;
}
-keep class com.google.android.material.navigation.NavigationView {
    public *;
}
-keep class com.google.android.material.button.MaterialButton {
    public *;
}

# Strip out unused AndroidX components
-dontwarn androidx.constraintlayout.**
-dontwarn androidx.recyclerview.**
-dontwarn androidx.cardview.**
-dontwarn androidx.fragment.**
-dontwarn androidx.lifecycle.**
-dontwarn androidx.savedstate.**
-dontwarn androidx.activity.**
-dontwarn androidx.arch.core.**

# Remove unused Google Play Services
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**

# Enable all optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-renamesourcefileattribute SourceFile

# Remove unused resources and code
-dontnote **
-dontwarn **
