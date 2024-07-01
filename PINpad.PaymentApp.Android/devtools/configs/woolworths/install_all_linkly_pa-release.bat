@echo off

set SECAPP_FLAVOUR=libs_set_1

call ..\..\uninstall_all.bat

for %%f in (..\..\..\extp2pe\secapp\build\outputs\apk\"%SECAPP_FLAVOUR%"\release\*.apk) do set APK_SECAPP=%%f
for %%g in (..\..\..\launcher\build\outputs\apk\release\*.apk) do set APK_POSITIVE_SVC=%%g
for %%h in (..\..\..\payment\build\outputs\apk\release\*.apk) do set APK_PAYMENT=%%h
for %%i in ("..\..\..\connect\build\outputs\apk\pa_linkly\release\*.apk") do set APK_LINKLY_CONNECT=%%i
for %%j in ("..\..\..\keyinjection\build\outputs\apk\release\*.apk") do set APK_KEY_INJECTION=%%j


echo APK_SECAPP=%APK_SECAPP%
echo APK_POSITIVE_SVC=%APK_POSITIVE_SVC%
echo APK_PAYMENT=%APK_PAYMENT%
echo APK_LINKLY_CONNECT=%APK_LINKLY_CONNECT%
echo APK_KEY_INJECTION=%APK_KEY_INJECTION%

..\..\adb install -r %APK_SECAPP%
..\..\adb install -r %APK_POSITIVE_SVC%
..\..\adb install -r %APK_PAYMENT%
..\..\adb install -r "%APK_LINKLY_CONNECT%"
..\..\adb install -r %APK_KEY_INJECTION%

call install_config_only-release.bat
echo this script will have rebooted the terminal
pause
