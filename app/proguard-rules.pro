# NewPipeExtractor and what it parses with (JSON, HTML, JavaScript, protobuf) use reflection and resource names.
-keep class org.schabi.newpipe.extractor.** { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class org.jsoup.** { *; }
-keep class com.grack.nanojson.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn org.mozilla.javascript.tools.**
-dontwarn javax.script.**
-dontwarn java.beans.**
-dontwarn jdk.dynalink.**
-dontwarn org.jspecify.annotations.**

-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes SourceFile, LineNumberTable
