@echo off

echo ------------------------------------------------------------------------
echo This script will uninstall all Linkly applications 
echo and reboot after
echo ------------------------------------------------------------------------

echo "Forced uninstall of com.linkly"

adb shell pm list packages com.linkly > packages.txt

for /F "tokens=2 delims=:" %%a in (packages.txt) do echo "removing package %%a"
for /F "tokens=2 delims=:" %%a in (packages.txt) do adb shell pm uninstall %%a

if exist packages.txt (
    del packages.txt
)