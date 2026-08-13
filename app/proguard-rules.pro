# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# WorkManager / receivers
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keep class * extends android.content.BroadcastReceiver

# JSON backup (org.json)
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

# Geofencing / Play Services Location
-keep class com.google.android.gms.location.** { *; }

# Firebase Crashlytics (when google-services.json is present)
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep line numbers for crash reports
-renamesourcefileattribute SourceFile
