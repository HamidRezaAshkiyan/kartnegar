# Keep Tesseract4Android and Leptonica JNI bindings intact under R8
-keep class com.googlecode.tesseract.** { *; }
-keep class com.googlecode.leptonica.** { *; }
-dontwarn com.googlecode.tesseract.**
-dontwarn com.googlecode.leptonica.**

# Keep Room entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
