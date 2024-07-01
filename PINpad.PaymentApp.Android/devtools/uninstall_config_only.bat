@echo off
setlocal enabledelayedexpansion

set "searchPattern=com.linkly.res"

for /f "tokens=2 delims=:" %%i in ('adb shell pm list packages ^| findstr /c:"!searchPattern!"') do (
    set "packageName=%%i"
    echo Uninstalling package: !packageName!
    adb uninstall !packageName!
)

endlocal