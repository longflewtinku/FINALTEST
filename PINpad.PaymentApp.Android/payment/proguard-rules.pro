# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\root3\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# MalConfig classes as they use reflection
-keepattributes InnerClasses

-keep,includedescriptorclasses class com.linkly.libui.IUIDisplay** { *; }
-keep,includedescriptorclasses class com.linkly.libui.IUIDisplay.SCREEN_DEF** { *; }
-keep,includedescriptorclasses class com.linkly.libsecapp.CardholderDataElement** { *; }
-keep,includedescriptorclasses class com.linkly.libsecapp.EncryptResult** { *; }
-keep,includedescriptorclasses class com.linkly.libsecapp.DecryptResult** { *; }
-keep,includedescriptorclasses class com.linkly.libsecapp.IP2PCtls** { *; }

-keep,includedescriptorclasses class com.linkly.secapp.config.WhitelistCfg** { *; }
-keep,includedescriptorclasses class com.linkly.secapp.config.P2PEncryptCfg** { *; }
-keep,includedescriptorclasses class com.linkly.secapp.config.P2PEmvCfg** { *; }
-keep,includedescriptorclasses class com.linkly.secapp.config.P2PCtlsCfg** { *; }

-keep,includedescriptorclasses class com.linkly.libconfig.DownloadCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.MalCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.MenusCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.ProfileCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.OrientationString** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.OverrideParameters** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.HotLoadParameters** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.cpat.CardProductList** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.cpat.CardProductCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libconfig.cpat.legacy.LegacyCardProductCfg** { *; }

-keep,includedescriptorclasses class com.linkly.libengine.config.BinCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libengine.config.BinRangesCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libengine.config.Config** { *; }
-keep,includedescriptorclasses class com.linkly.libengine.config.CupCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libengine.config.MaestroCfg** { *; }

-keep,includedescriptorclasses class com.linkly.libengine.config.PayCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libengine.config.PayParamCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libengine.config.BlacklistCfg { *; }
-keep,includedescriptorclasses class com.linkly.libengine.action** { *; }
-keep,includedescriptorclasses class com.linkly.payment.workflows** { *; }

-keep,includedescriptorclasses class com.linkly.libsecapp.CtlsCfg** { *; }
-keep,includedescriptorclasses class com.linkly.libsecapp.EmvCfg** { *; }

-keep,includedescriptorclasses class com.linkly.libmal.global.config.** { *; }
-keep,includedescriptorclasses class com.linkly.libpositive.events.** { *; }
-keep,includedescriptorclasses class com.linkly.libpositive.wrappers.** { *; }
-keep,includedescriptorclasses class com.linkly.libpositive.PosIntegrate** { *; }

-keep,includedescriptorclasses class com.linkly.libmal.global.config.** { *; }
-keep public interface com.linkly.libmal.IMal { *; }
-keep public interface com.linkly.libmal.IMalHardware { *; }
-keep,includedescriptorclasses class com.linkly.libmal.MalFactory** { *; }

# Gson uses generic type information stored in a class file when working with
# fields. Proguard removes such information by default, keep it.
-keepattributes Signature

# This is also needed for R8 in compat mode since multiple
# optimizations will remove the generic signature such as class
# merging and argument removal. See:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#troubleshooting-gson-gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Optional. For using GSON @Expose annotation
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations

-keep,includedescriptorclasses class * implements android.os.Parcelable {
 public static final android.os.Parcelable$Creator *;
}

#------- XML pull conflicts ----------------------
-dontwarn org.xmlpull.v1.**
-dontwarn org.kxml2.io.**
-dontwarn android.content.res.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }
#------------------------------------------------

-keep,includedescriptorclasses class javax** { *; }
-keep,includedescriptorclasses class com.pax** { *; }
-keep,includedescriptorclasses class com.google** { *; }
-keep,includedescriptorclasses class net.sqlcipher** { *; }
-keep,includedescriptorclasses class gnu.trove.THashMap** { *; }
-keep,includedescriptorclasses class android.app.ActivityThread** { *; }
-keep,includedescriptorclasses class org.kobjects** { *; }
-keep,includedescriptorclasses class sun.misc.BASE64Encoder* { *; }
-keep,includedescriptorclasses class com.creditcall** { *; }
-keep,includedescriptorclasses class java.beans** { *; }

-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * extends com.linkly.payment.menus.Menu {
    public static ** getInstance();
}

-dontwarn com.pax.**
-dontwarn net.sqlcipher.**
-dontwarn org.apache.**
-dontwarn android.net.**
-dontwarn android.os.**
-dontwarn org.dom4j.**
-dontwarn com.fasterxml.**
-dontwarn com.xml.**
-dontwarn org.xml.**
-dontwarn org.xmlpull.**
-dontwarn com.google.**
-dontwarn sun.misc.**
-dontwarn javax**

-dontnote org.apache.**
-dontnote android.net.**
-dontnote android.os.**
-dontnote javax.xml.**
-dontnote com.google.**
-dontnote org.w3c.**
-dontnote org.xml.**
-dontnote org.dom4j.**
-dontnote org.xmlpull.**
-dontnote com.pax**
-dontnote javax**


#remove some of the pax debugging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-dontwarn org.w3c.dom.**

#Gson
-dontwarn com.google.gson.**
-keep class sun.misc.Unsafe.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.android.** { *; }

#JJWT
-keepnames class com.fasterxml.jackson.databind.** { *; }
-dontwarn com.fasterxml.jackson.databind.*
-keepattributes InnerClasses
-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }
-dontwarn io.jsonwebtoken.impl.Base64Codec
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames interface com.fasterxml.jackson.** { *; }
-keepnames class org.w3c.dom.** { *; }
-keepnames class org.xml.** { *; }
-keepnames class com.google.android.** { *; }
-keepnames class org.dom4j.** { *; }
-keepnames class javax.xml.** { *; }


#dom4j
-dontwarn org.dom4j.**
-keep class org.dom4j.**{*;}
-dontwarn org.xml.sax.**
-keep class org.xml.sax.**{*;}
-dontwarn com.fasterxml.jackson.**
-keep class com.fasterxml.jackson.**{*;}
-dontwarn com.pax.market.api.sdk.java.base.util.**
-keep class com.pax.market.api.sdk.java.base.util.**{*;}
-dontwarn org.w3c.dom.**
-keep class org.w3c.dom.**{*;}
-dontwarn javax.xml.**
-keep class javax.xml.**{*;}
-keep class org.apache.xerces.**{*;}

#dto
-dontwarn com.pax.market.api.sdk.java.base.dto.**
-keep class com.pax.market.api.sdk.java.base.dto.**{*;}


-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-keepnames class sun.misc.** { *; }

-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

#androidx
-dontwarn android.databinding.**
-keep class android.databinding.**{*;}
-dontwarn com.sun.mail.**
-keep class com.sun.mail.**{*;}

# Kotlin null check error removal, used as this causes unobfuscated parameter name to still be displayed.
# AKA a security risk
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void checkNotNull(...);
	public static void checkExpressionValueIsNotNull(...);
	public static void checkNotNullExpressionValue(...);
	public static void checkParameterIsNotNull(...);
	public static void checkNotNullParameter(...);
	public static void checkReturnedValueIsNotNull(...);
	public static void checkFieldIsNotNull(...);
	public static void throwUninitializedPropertyAccessException(...);
	public static void throwNpe(...);
	public static void throwJavaNpe(...);
	public static void throwAssert(...);
	public static void throwIllegalArgument(...);
	public static void throwIllegalState(...);
}

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.