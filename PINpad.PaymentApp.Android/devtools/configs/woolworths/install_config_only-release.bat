call ..\..\uninstall_config_only.bat

for %%i in (..\..\..\extaps\res\build\outputs\apk\woolworths\release\*.apk) do set APK_RES=%%i

echo APK_RES=%APK_RES%

..\..\adb install -r %APK_RES%

..\..\adb.exe push initialparams.xml /data/data/com.linkly.payment/files
..\..\adb.exe push overrideparams.xml /data/data/com.linkly.payment/files
..\..\adb.exe push hotloadparams.xml /data/data/com.linkly.payment/files
..\..\adb.exe push keysets.json /data/data/com.linkly.keyinjection/files
..\..\adb.exe push 100140.pkt /data/data/com.linkly.payment/files
..\..\adb.exe push epat_100004.xml /data/data/com.linkly.payment/files
..\..\adb.exe push CPAT_WP10_32011_APCA.txt /data/data/com.linkly.payment/files
..\..\adb.exe push CARDS.XML /data/data/com.linkly.payment/files
..\..\adb.exe push cfg_connect.xml /data/data/com.linkly.connect.linkly/files
..\..\adb.exe push wpaycacerts.pem /data/data/com.linkly.payment/files
..\..\adb.exe push wpayclientcert.pem /data/data/com.linkly.payment/files
..\..\adb.exe push wpayprivatekey.pem /data/data/com.linkly.payment/files

..\..\adb reboot
