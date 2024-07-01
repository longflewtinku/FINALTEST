#!/usr/bin/env bash

set -x

#This script mimics what the Bitbucket pipelines server will do

export OFFLINE_BUILD=1
export BITBUCKET_BRANCH=$(git rev-parse --abbrev-ref HEAD)
export BUILD_SERVER=1 #Pretend to be a build server
export MASTER_BUILD=1 #Pretend to be a master build

source ./build_scripts/init_git.sh
source ./build_scripts/update_android.sh

./build_scripts/update_all_versions.sh 0

#./gradlew clean

./gradlew assembleDebug
./gradlew testDebugUnitTest

if [ "$MASTER_BUILD" = "1" ]; then
	./gradlew assembleRelease
	./gradlew testReleaseUnitTest
else
	echo "********** Not on master; Skipping assemble Demo/Release **********"
fi

#Need to kill java.exe as it holds open the version.properties files and doesn't let us discard changes
taskkill //f //im java.exe

#These scripts will not upload anything to Bitbucket (OFFLINE_BUILD variable)
./build_scripts/upload_output.sh

if [ "$MASTER_BUILD" = "1" ]; then
	source ./build_scripts/tag_and_push.sh

	./build_scripts/upload_output.sh tag

	#Increment revision
	./build_scripts/update_all_versions.sh 1
else
	echo "********** Not on master; Skipping Tag and push **********"
fi
