call ..\..\uninstall_config_only.bat

for %%i in (..\..\..\extaps\reswestpac\build\outputs\apk\debug\*.apk) do set APK_RES=%%i

echo APK_RES=%APK_RES%

..\..\adb install -r %APK_RES%

..\..\adb.exe push initialparams.xml /data/data/com.linkly.payment/files
..\..\adb.exe push overrideparams.xml /data/data/com.linkly.payment/files


..\..\adb reboot
