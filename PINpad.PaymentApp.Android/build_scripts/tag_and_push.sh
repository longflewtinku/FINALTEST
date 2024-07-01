#!/bin/bash

#Navigate to root dir if needed
if [ ! -f "bitbucket-pipelines.yml" ]; then
	cd ..
	if [ ! -f "bitbucket-pipelines.yml" ]; then
		echo "Project root not found, unable to update version"
		exit 1
	fi 	
fi

function commit_versions_and_tag {
	TAG_NAME=$1
	PROJECT_NAME=$2

	if [ $(git tag --list | grep $TAG_NAME) ]; then
		echo "Tag already exists; Skipping tag/push of '$TAG_NAME' for '$PROJECT_NAME'"
	else
		echo "Tagging '$TAG_NAME' for '$PROJECT_NAME' and pushing to Bitbucket"
		git tag -a $TAG_NAME -m 'Bitbucket Pipelines tagging ${TAG_NAME}'
		if [ "$OFFLINE_BUILD" = "1" ]; then
			echo "Skipping push for tag '$TAG_NAME'"
		else
			git push origin : $TAG_NAME
		fi
	fi
}

#Get payment version string
PAYMENT_VER=$(cat payment/ver_tag.tmp)
#Get p2pe version string
P2PE_VER=$(cat extp2pe/secapp/ver_tag.tmp)

echo "======= Tag and Push ======"
commit_versions_and_tag $PAYMENT_VER Payment

pushd extp2pe > /dev/null
commit_versions_and_tag $P2PE_VER P2Pe
popd > /dev/null
echo "==========================="
