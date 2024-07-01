@ECHO off

REM This script is called by uninstall_all.bat and uninstall_config_only.bat
REM A pre-requisite of this working is for a file 'installed_packages.txt'
REM This is created with 'adb.exe shell cmd package list packages > installed_packages.txt'

echo uninstalling %~1
rem if exist installed_packages.txt (
rem 	(find /c "%~1" installed_packages.txt > nul) && (adb.exe uninstall %~1) || (echo - %~1 not found on device)
rem ) else (
	echo "Forced uninstall of %~1"

    adb shell pm list packages %1 > packages.txt

    for /F "tokens=2 delims=:" %%a in (packages.txt) do echo "removing package %%a"
    for /F "tokens=2 delims=:" %%a in (packages.txt) do adb shell pm uninstall %%a

    if exist installed_packages.txt (
        del installed_packages.txt
    )

rem )
