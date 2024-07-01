echo off

echo ------------------------------------------------------------------------
echo This script will install the payment app 
echo for standalone mode operation and reboot after
echo ------------------------------------------------------------------------

for %%f in ("secapp*.apk") do set APK_SECAPP=%%f
for %%g in ("launcher*.apk") do set APK_POSITIVE_SVC=%%g
for %%h in ("payment*.apk") do set APK_PAYMENT=%%h
for %%j in ("res*.apk") do set APK_RES_PACKAGE=%%j
for %%k in ("initial*.xml") do set XML_INITIAL=%%k
for %%l in ("override*.xml") do set XML_OVERRIDE=%%l
for %%l in ("hotload*.xml") do set XML_HOTLOAD=%%l
for %%o in ("epat_*.xml") do set EPAT_FILE=%%o
for %%p in ("pkt_*.xml") do set PKT_FILE=%%p
set CFG_EMV_FILE="cfg_emv.json"
set CFG_CTLS_FILE="cfg_ctls_emv.json"
set CPAT_FILE="Default_CPAT.txt"
set CARDS_FILE="CARDS.XML" 

echo uninstalling all existing versions
echo ------------------------------------------------------------------------
call uninstall_all.bat

echo ------------------------------------------------------------------------
echo Installing the following
echo Linkly Secure App      = %APK_SECAPP%
echo Linkly Launcher        = %APK_POSITIVE_SVC%
echo Linkly Payment         = %APK_PAYMENT%
echo Customer Package       = %APK_RES_PACKAGE%
echo ------------------------------------------------------------------------

adb install -r %APK_SECAPP%
adb install -r %APK_POSITIVE_SVC%
adb install -r %APK_PAYMENT%
adb install -r %APK_RES_PACKAGE%

adb push %XML_INITIAL% /data/data/com.linkly.payment/files
adb push %XML_OVERRIDE% /data/data/com.linkly.payment/files
adb push %XML_HOTLOAD% /data/data/com.linkly.payment/files
adb push %EPAT_FILE% /data/data/com.linkly.payment/files
adb push %PKT_FILE% /data/data/com.linkly.payment/files
adb push %CPAT_FILE% /data/data/com.linkly.payment/files
adb push %CFG_CTLS_FILE% /data/data/com.linkly.payment/files
adb push %CFG_EMV_FILE% /data/data/com.linkly.payment/files
adb push %CARDS_FILE% /data/data/com.linkly.payment/files

adb reboot