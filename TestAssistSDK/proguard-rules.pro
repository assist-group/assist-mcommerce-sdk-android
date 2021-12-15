# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/igor/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class ru.assisttech.sdk.**
-keepclassmembers class ru.assisttech.sdk.** {
    *;
}

-keep class ru.assisttech.sdk.network.**
-keepclassmembers class ru.assisttech.sdk.network.** {
    *;
}

-keep class io.card.**
-keepclassmembers class io.card.** {
    *;
}
-dontwarn com.samsung.android.sdk.samsungpay.**
-keep class com.samsung.android.sdk.** { *; }
-keep interface com.samsung.android.sdk.** { *; }