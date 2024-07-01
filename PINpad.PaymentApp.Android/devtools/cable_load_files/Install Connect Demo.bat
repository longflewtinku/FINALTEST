@echo off
set CFG_CONNECT="cfg_connect.xml"

for %%i in ("connect*demo*.apk") do set APK_LINKLY_CONNECT=%%i

echo uninstalling all existing versions
echo ------------------------------------------------------------------------
adb uninstall com.linky.connect.linkly
adb uninstall com.linky.connect.demo
adb uninstall com.linky.connect.vfi

echo ------------------------------------------------------------------------
echo Installing the following
echo Linkly Connect App     = %APK_LINKLY_CONNECT%
echo ------------------------------------------------------------------------

adb install -r %APK_LINKLY_CONNECT%

adb push %CFG_CONNECT% /data/data/com.linkly.connect.demo/files

adb reboot