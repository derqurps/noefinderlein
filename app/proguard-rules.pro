# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/roman/androiddev/sdk/tools/proguard/proguard-android.txt
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

# Workaround for Samsung Android 4.2 bug
# https://code.google.com/p/android/issues/detail?id=78377
# https://code.google.com/p/android/issues/detail?id=78377#c188
-keepattributes **
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class !android.support.v7.internal.view.menu.**,** {*;}
-dontpreverify
-dontoptimize
-dontshrink
-dontwarn **
-dontnote **
-keepattributes Signature
-keepattributes *Annotation*

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn okio.**