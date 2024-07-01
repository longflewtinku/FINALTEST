@echo off

echo ------------------------------------------------------------------------
echo This script will install all the XML files 
echo and the customer package. 
echo It will reboot the terminal after
echo ------------------------------------------------------------------------

for %%j in ("res*.apk") do set APK_RES_PACKAGE=%%j
for %%k in ("initial*.xml") do set XML_INITIAL=%%k
for %%l in ("override*.xml") do set XML_OVERRIDE=%%l 
for %%o in ("epat_*.xml") do set EPAT_FILE=%%o
for %%p in ("pkt_*.xml") do set PKT_FILE=%%p
for %%l in ("hotload*.xml") do set XML_HOTLOAD=%%l
set CFG_EMV_FILE="cfg_emv.json"
set CFG_CTLS_FILE="cfg_ctls_emv.json"
set CFG_CARDPRODUCT="cardproduct.json"
set CPAT_FILE="Default_CPAT.txt"
set CARDS_FILE="CARDS.XML"
set CFG_CONNECT_FILE="cfg_connect.xml"
set CFG_BLACKLIST_FILE="blacklist.json"
set SSL_CERTIFICATE_FILE="stca.crt"

echo ------------------------------------------------------------------------
echo Installing the following
echo Customer Package       = %APK_RES_PACKAGE%
echo XML files              = %XML_INITIAL% and %XML_OVERRIDE% and %XML_HOTLOAD%
echo ------------------------------------------------------------------------

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
adb push %CFG_CARDPRODUCT% /data/data/com.linkly.payment/files
adb push %CFG_BLACKLIST_FILE% /data/data/com.linkly.payment/files
adb push %CFG_CONNECT_FILE% /data/data/com.linkly.connect.linkly/files
adb push %SSL_CERTIFICATE_FILE% /data/data/com.linkly.payment/files

adb reboot