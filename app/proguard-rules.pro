# ═══════════════════════════════════════════════════════════════════
# Feder Messenger — ProGuard rules for R8
# ═══════════════════════════════════════════════════════════════════

# ─── Kotlin ───
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# ─── Compose ───
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─── Room ───
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# ─── Gson (важно для десериализации) ───
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.feder.compose.data.** { *; }
-keep class com.feder.compose.ui.screen.MsgItem { *; }
-keep class com.feder.compose.ui.screen.Reaction { *; }
-keep class com.feder.compose.ChatItem { *; }
-keep class com.feder.compose.LoginRequest { *; }
-keep class com.feder.compose.LoginResponse { *; }
-keep class com.feder.compose.stories.StoryApi** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ─── OkHttp ───
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ─── Coil ───
-keep class coil.** { *; }
-dontwarn coil.**

# ─── Media3 / ExoPlayer ───
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ─── YouTube Extractor ───
-keep class com.github.HaarigerHarald.** { *; }
-dontwarn com.github.HaarigerHarald.**

# ─── Jsoup ───
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# ─── JSON / org.json ───
-keep class org.json.** { *; }

# ─── Наши модели данных ───
-keep class com.feder.compose.data.entity.** { *; }
-keep class com.feder.compose.repository.** { *; }

# ─── JS интерфейсы ───
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ─── Enum ───
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ─── Parcelable ───
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
