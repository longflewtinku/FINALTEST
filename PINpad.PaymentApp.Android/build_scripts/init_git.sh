#!/bin/bash

#Navigate to root dir if needed
if [ ! -f "bitbucket-pipelines.yml" ]; then
	cd ..
	if [ ! -f "bitbucket-pipelines.yml" ]; then
		echo "Project root not found, unable to update version"
		exit 1
	fi 	
fi

P2PE_DIR=extp2pe
BACKUP_DIR=temp_extp2pe

if [ "$OFFLINE_BUILD" = "1" ]; then
	echo "Offline build; Skipping git remote OAuth token setup"
else
	git config --global user.email $(git config user.email)
	git config --global user.name $(git config user.name)

	#Set up Bitbucket authorisation
	#Get OAuth token
	export ACCESS_TOKEN=$(curl -s -X POST -u "${CLIENT_ID}:${CLIENT_SECRET}" https://bitbucket.org/site/oauth2/access_token -d grant_type=client_credentials -d scopes="repository" | awk -F '[{}": ,]*' '{print $14}')

	#Set up git origin to use retrieved OAuth token
	git remote remove origin
	git remote add origin "https://x-token-auth:${ACCESS_TOKEN}@bitbucket.org/${BITBUCKET_REPO_OWNER}/${BITBUCKET_REPO_SLUG}"
	git fetch origin

	#Make backup of loaded artifacts
	if [ -e $P2PE_DIR ] && [ "$(ls -A $P2PE_DIR)" ]; then
		echo "Retaining artifacts..."
		mv $P2PE_DIR $BACKUP_DIR
	else
		echo "No artifacts to retain"
	fi

	#Pull down submodules
	git submodule update --init
fi

#Check out a branch for the submodule commit being pointed to
pushd $P2PE_DIR

#Get branches that contain the current commit
RELEVANT_BRANCHES=$(git branch -r --contains $(git rev-parse HEAD))
echo "List of RELEVANT_BRANCHES:"
echo $RELEVANT_BRANCHES | tr -s " " "\n"

#Get the remote branch name to check out
if [ "$(echo $RELEVANT_BRANCHES | tr -s " " "\n" | grep origin/master)" != "" ]; then
	echo "Master Build"
	BRANCH_TO_TRACK="origin/master"
elif [ "$(echo $RELEVANT_BRANCHES | tr -s " " "\n" | grep origin/dev_paxuk)" != "" ]; then
	echo "Development Build"
	BRANCH_TO_TRACK="origin/dev_paxuk"
else
	echo "Branch Build"
	BRANCH_TO_TRACK=$(git branch -r --contains $(git rev-parse HEAD) | head -n 1)
fi

echo "Checking out branch '${BRANCH_TO_TRACK}' for submodule '$P2PE_DIR'"
git checkout --track $BRANCH_TO_TRACK || git checkout ${BRANCH_TO_TRACK#"origin/"}
popd

#Move back artifacts and delete temp folder
if [ -e $BACKUP_DIR ]; then
	echo "Restoring artifacts..."
	cp -R $BACKUP_DIR/* $P2PE_DIR/
	rm -r $BACKUP_DIR
fi
