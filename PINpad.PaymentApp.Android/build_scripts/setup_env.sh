#!/bin/bash

#Navigate to root dir if needed
if [ ! -f "bitbucket-pipelines.yml" ]; then
	cd ..
	if [ ! -f "bitbucket-pipelines.yml" ]; then
		echo "Project root not found, unable to update version"
		exit 1
	fi 	
fi

CUR_DIR=$(pwd)
ORIGIN_DIR=$(cd "${CUR_DIR}/.."; pwd)

case $(uname -s) in
  Linux)
    OS_TYPE="linux"
  ;;
  Darwin)
    OS_TYPE="darwin"
  ;;
  CYGWIN*|MINGW*)
    OS_TYPE="windows"
	CMD_WGET=build_scripts/wget
	CMD_SDK_MAN=sdkmanager.bat
  ;;
  *)
    echo "!! Unsupported OS $(uname -s)"
    exit 1
  ;;
esac

export OS_TYPE
export ANDROID_HOME="${ORIGIN_DIR}/android-sdk-${OS_TYPE}"
export PATH="${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:${PATH}"
