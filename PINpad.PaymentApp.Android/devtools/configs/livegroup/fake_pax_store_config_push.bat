@echo off
set folderName=/data/data/com.linkly.payment/files/EFT/paxstore

adb push initialparams.xml %folderName%
adb push hotloadparams.xml %folderName%
adb push overrideparams.xml %folderName%
adb push cardproduct.json %folderName%
adb push cfg_ctls_emv.json %folderName%
adb push cfg_emv.json %folderName%
adb push stca.crt %folderName%

adb shell am startservice -n com.linkly.payment/com.linkly.libpositivesvc.paxstore.DownloadParamService
