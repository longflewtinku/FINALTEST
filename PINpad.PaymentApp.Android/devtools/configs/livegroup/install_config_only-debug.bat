call ..\..\uninstall_config_only.bat

for %%i in (..\..\..\extaps\res\build\outputs\apk\livegroup\debug\*.apk) do set APK_RES=%%i

echo APK_RES=%APK_RES%

..\..\adb install -r %APK_RES%

..\..\adb.exe push initialparams.xml /data/data/com.linkly.payment/files
..\..\adb.exe push overrideparams.xml /data/data/com.linkly.payment/files
..\..\adb.exe push keysets.json /data/data/com.linkly.keyinjection/files
..\..\adb.exe push hotloadparams.xml /data/data/com.linkly.payment/files
..\..\adb.exe push cardproduct.json /data/data/com.linkly.payment/files
..\..\adb.exe push cfg_ctls_emv.json /data/data/com.linkly.payment/files
..\..\adb.exe push cfg_emv.json /data/data/com.linkly.payment/files
..\..\adb.exe push stca.crt /data/data/com.linkly.payment/files

..\..\adb.exe push cfg_connect.xml /data/data/com.linkly.connect.linkly/files

..\..\adb reboot
