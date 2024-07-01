#!/bin/bash

WORKING_DIR=working
UPLOAD_DIR=upload
BUILD_DATE=$(date +%Y-%m-%d) #i.e. 2018-10-15

#Payment, launcher, secapp
declare -a OUTPUT_DIRS=("./payment/build/outputs" "./launcher/build/outputs" "./extp2pe/secapp/build/outputs")

#Navigate to root dir if needed
if [ ! -f "bitbucket-pipelines.yml" ]; then
	cd ..
	if [ ! -f "bitbucket-pipelines.yml" ]; then
		echo "Project root not found, unable to update version"
		exit 1
	fi 	
fi

#Get branch name (either from build system, or directly from git)
BRANCH_NAME=$BITBUCKET_BRANCH
if [ "$BRANCH_NAME" = "" ]; then
	BRANCH_NAME=$(git branch | grep \* | cut -d ' ' -f2)
	OFFLINE_BUILD=1
fi
#Replace '/' with '-'
BRANCH_NAME=${BRANCH_NAME////-}

#Get filename prefix
if [ "$1" = "tag" ]; then
    ZIP_NAME="tag_$(cat payment/ver_short.tmp)"
else
    ZIP_NAME="${BUILD_DATE}_${BRANCH_NAME}_$(cat payment/ver_full.tmp)"
fi
ZIP_NAME="${UPLOAD_DIR}/${ZIP_NAME}"
FULL_ZIP_NAME="${ZIP_NAME}.zip"

DBG_ZIP_NAME="${ZIP_NAME}_DEBUG"
FULL_DBG_ZIP_NAME="${DBG_ZIP_NAME}.zip"

rm -rf $UPLOAD_DIR
mkdir -p $UPLOAD_DIR

function archive_and_upload {
	#Create/empty-out working directory
	rm -rf $WORKING_DIR
	mkdir -p $WORKING_DIR
	
	BUILD_TYPE=$1
	if [ "$BUILD_TYPE" != "release" ]; then
		BUILD_TYPE=debug
	fi

	#Copy apk (and corresponding map) files to working dir
	for i in "${OUTPUT_DIRS[@]}"
	do
		#Find paths
		OUT_APK=$(ls -lrt ${i}/apk/$BUILD_TYPE/*.apk | tail -1 | awk -F" " '{ print $9 }')
		OUT_MAP=$(ls -lrt ${i}/mapping/$BUILD_TYPE/*.txt | tail -1 | awk -F" " '{ print $9 }')

		#Copy output files to temp folder
		[ $OUT_APK ] && cp $OUT_APK ./${WORKING_DIR}/$(basename $OUT_APK)
		[ $OUT_MAP ] && cp $OUT_MAP ./${WORKING_DIR}/$(basename $OUT_APK)_map.txt
	done

	#Upload release package
	echo "${BUILD_TYPE^} output files:"
	ls -l $WORKING_DIR
	if [ "$(ls -A $WORKING_DIR)" ]; then
		echo "Creating Archive file $3"
		python -c "import shutil;shutil.make_archive('$2','zip','$WORKING_DIR')"
		if [ "$OFFLINE_BUILD" = "1" ]; then
			echo "Offline build; skipping upload of $3"
		else
			echo "Uploading $3"
			curl -X POST "https://api.bitbucket.org/2.0/repositories/${BITBUCKET_REPO_OWNER}/${BITBUCKET_REPO_SLUG}/downloads?access_token=$ACCESS_TOKEN" --form files=@"$3"
		fi
	else
		echo "Skipping archive file $3"
	fi
}

echo "====== Upload Output ======"
echo "----- Release package -----"
archive_and_upload release $ZIP_NAME $FULL_ZIP_NAME

echo "------ Debug package ------"
archive_and_upload debug $DBG_ZIP_NAME $FULL_DBG_ZIP_NAME
echo "==========================="

#Remove temp folder
rm -rf $WORKING_DIR
