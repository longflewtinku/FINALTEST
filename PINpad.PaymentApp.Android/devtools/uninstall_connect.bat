echo off

call do_uninstall com.linkly.connect.demo
call do_uninstall com.linkly.connect.linkly

adb reboot

echo this script will have rebooted the terminal
