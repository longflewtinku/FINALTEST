#!/bin/bash

#set -x

#Navigate to root dir if needed
if [ ! -f "bitbucket-pipelines.yml" ]; then
	cd ..
	if [ ! -f "bitbucket-pipelines.yml" ]; then
		echo "Project root not found, unable to update version"
		exit 1
	fi
fi

INCREMENT_REVISION=$1
if [ "$INCREMENT_REVISION" = "1" ]; then
	#Get version strings
	PAYMENT_VER=$(cat payment/ver_tag.tmp)
	P2PE_VER=$(cat extp2pe/secapp/ver_tag.tmp)
else
	#default to not increment version
    INCREMENT_REVISION=0
fi

#Create an array of project paths
PROJECTS=(".")

NO_SOURCE_CHANGED=1
#loop through array updating each project version
for PROJECT in "${PROJECTS[@]}"; do
	source ./build_scripts/update_version.sh $PROJECT $INCREMENT_REVISION
	if [ "$NO_CHANGES" = "0" ]; then
		NO_SOURCE_CHANGED=0
	fi
done

function git_commit_versions {
	git add *version.properties
	COMMIT_MSG="Updating version numbers for $1"
	git commit -m "${COMMIT_MSG}"
	if [ "$MASTER_BUILD" = "1" ]; then
		CURRENT_BRANCH=$(git branch | grep \* | cut -d ' ' -f2)
		#commit version changes back to dev_paxuk
		git fetch . $CURRENT_BRANCH:dev_paxuk
	fi

	if [ "$OFFLINE_BUILD" = "1" ]; then
		echo "Skipping push for '$1'"
	else
		git push --all
	fi
}

if [ "$INCREMENT_REVISION" = "1" ]; then
	#remove all temporary version files
	find . -name 'ver_*.tmp' -delete

	cd extp2pe
	git_commit_versions "SECAPP ${P2PE_VER}"
	cd ..

	git add extp2pe #also update the submodule pointer
	git_commit_versions "Payment ${PAYMENT_VER}"
fi

echo NO_SOURCE_CHANGED=$NO_SOURCE_CHANGED

export NO_SOURCE_CHANGED
