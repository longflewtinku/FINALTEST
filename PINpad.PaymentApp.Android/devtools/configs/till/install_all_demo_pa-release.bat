rem call ..\..\uninstall_all.bat

for %%i in ("..\..\..\connect\build\outputs\apk\pa_demo\release\*.apk") do set APK_LINKLY_CONNECT=%%i


echo APK_LINKLY_CONNECT=%APK_LINKLY_CONNECT%

..\..\adb install -r "%APK_LINKLY_CONNECT%"

..\..\adb reboot
rem this script will have rebooted the terminal
