@echo off

echo -------------------------------------------
echo Install Key Injection
echo -------------------------------------------

for %%j in ("..\..\..\keyinjection\build\outputs\apk\debug\*.apk") do set APK_KEY_INJECTION=%%j

echo APK_KEY_INJECTION=%APK_KEY_INJECTION%

..\..\adb install -r %APK_KEY_INJECTION%

..\..\adb.exe push keysets.json /data/data/com.linkly.keyinjection/files

..\..\adb reboot

echo rebooting the terminal

