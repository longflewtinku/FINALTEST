call ..\..\uninstall_all.bat

for %%f in (..\..\..\extp2pe\secapp\build\outputs\apk\debug\*.apk) do set APK_SECAPP=%%f
for %%g in (..\..\..\launcher\build\outputs\apk\debug\*.apk) do set APK_POSITIVE_SVC=%%g
for %%h in (..\..\..\payment\build\outputs\apk\debug\*.apk) do set APK_PAYMENT=%%h
for %%i in ("..\..\..\connect\build\outputs\apk\pa_linkly\debug\*.apk") do set APK_LINKLY_CONNECT=%%i


echo APK_SECAPP=%APK_SECAPP%
echo APK_POSITIVE_SVC=%APK_POSITIVE_SVC%
echo APK_PAYMENT=%APK_PAYMENT%
echo APK_LINKLY_CONNECT=%APK_LINKLY_CONNECT%

..\..\adb install -r %APK_SECAPP%
..\..\adb install -r %APK_POSITIVE_SVC%
..\..\adb install -r %APK_PAYMENT%
..\..\adb install -r "%APK_LINKLY_CONNECT%"

call install_config_only-debug.bat
rem this script will have rebooted the terminal
