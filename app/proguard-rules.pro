# ===== Attributes =====
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, EnclosingMethod

# ===== kotlinx.serialization =====
# Giữ serializer sinh tự động + Companion cho mọi class @Serializable của app.
-keepclassmembers class com.example.tuvi.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.tuvi.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.tuvi.**$$serializer { *; }

-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontnote kotlinx.serialization.**

# ===== Retrofit / OkHttp =====
# (Retrofit & OkHttp đã kèm consumer rules; thêm vài keep an toàn cho coroutines/generics.)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ===== Giữ API service interfaces & DTO (đề phòng reflection của Retrofit) =====
-keep interface com.example.tuvi.data.remote.** { *; }
-keep class com.example.tuvi.data.remote.dto.** { *; }

# ===== AdMob / Google Mobile Ads / UMP =====
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.ads.**

# ===== Firebase Remote Config =====
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ===== Compose / Kotlin metadata (giữ mặc định, đa số đã có consumer rules) =====
-dontwarn org.jetbrains.annotations.**
