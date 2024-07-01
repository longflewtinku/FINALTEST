@ECHO OFF

set RETURN_DIR=%CD%
cd /D "%~dp0"

if not exist installed_packages.txt (
	adb.exe shell pm list packages -f > installed_packages.txt
)

rem ----- new wildcard type usage, deletes any app with pkg name beginning com.linkly -----
call do_uninstall com.linkly

rem call uninstall_config_only.bat

if exist installed_packages.txt (
	del installed_packages.txt
)
cd /D %RETURN_DIR%
