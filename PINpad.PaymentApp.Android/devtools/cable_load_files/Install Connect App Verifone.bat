@echo off

for %%i in ("connect*.apk") do set APK_LINKLY_CONNECT=%%i

echo uninstalling all existing versions
echo ------------------------------------------------------------------------
call uninstall_all.bat

echo ------------------------------------------------------------------------
echo Installing the following
echo Linkly Connect App     = %APK_LINKLY_CONNECT%
echo ------------------------------------------------------------------------

adb install -r %APK_LINKLY_CONNECT%

timeout 5

adb reboot