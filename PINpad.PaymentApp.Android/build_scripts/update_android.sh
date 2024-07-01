#!/bin/bash

#Navigate to root dir if needed
if [ ! -f "bitbucket-pipelines.yml" ]; then
	cd ..
	if [ ! -f "bitbucket-pipelines.yml" ]; then
		echo "Project root not found, unable to update version"
		exit 1
	fi 	
fi

DEFAULT_ANDROID_SDK_ZIP_VERSION="4333796"
ANDROID_SDK_ZIP_VERSION=${1:-${DEFAULT_ANDROID_SDK_ZIP_VERSION}}

if [[ ! -v ANDROID_HOME ]]; then
	echo "ANDROID_HOME not set, running setup_env.sh script"
	source ./build_scripts/setup_env.sh || exit 1
	export ANDROID_HOME
	export PATH
fi

CMD_WGET=wget
CMD_SDK_MAN=sdkmanager
if [ "${OS_TYPE}" = "windows" ]; then
	CMD_WGET=build_scripts/wget
	CMD_SDK_MAN=sdkmanager.bat
fi

CMD_SDK_MAN="${ANDROID_HOME}/tools/bin/${CMD_SDK_MAN}"

if [ ! -f "${CMD_SDK_MAN}" ]; then
  # Download and unzip Android sdk
  echo "----- Downloading Android SDK '${ANDROID_SDK_ZIP_VERSION}' for '${OS_TYPE}' -----"
  $CMD_WGET "https://dl.google.com/android/repository/sdk-tools-${OS_TYPE}-${ANDROID_SDK_ZIP_VERSION}.zip"
  unzip -o "sdk-tools-${OS_TYPE}-${ANDROID_SDK_ZIP_VERSION}.zip" -d "${ANDROID_HOME}"
  rm "sdk-tools-${OS_TYPE}-${ANDROID_SDK_ZIP_VERSION}.zip"
fi

echo "----- Extracting Android Versions -----"
SDK_VER=$(awk '/compileSdkVer = /{print $NF}' build.gradle)
TOOLS_VER=$(sed -e "s/^\"//" -e "s/\"$//" <<<$(awk '/buildToolsVer = /{print $NF}' build.gradle))
echo SDK_VER   = $SDK_VER
echo TOOLS_VER = $TOOLS_VER

# Update android sdk
echo "----- Updating Android -----"
${CMD_SDK_MAN} --update
yes | ${CMD_SDK_MAN} "platforms;android-"${SDK_VER} "build-tools;"${TOOLS_VER}

${CMD_SDK_MAN} --update
${CMD_SDK_MAN} --list --verbose

# Accept all licenses (source: http://stackoverflow.com/questions/38096225/automatically-accept-all-sdk-licences)
echo "----- Accepting Licenses -----"
yes | ${CMD_SDK_MAN} --licenses

yes | ${CMD_SDK_MAN} --licenses --sdk_root=$ANDROID_HOME
