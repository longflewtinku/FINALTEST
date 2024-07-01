#!/bin/bash

function remove_whitespace {
	echo `echo $1 | sed -e 's/^[[:space:]]*//'`
}

function get_value {
	#Extract data from line after '='
	TEMP=`cut -d "=" -f 2 <<< $1`
	#Remove any whitespace from parsed data
	TEMP=$(remove_whitespace $TEMP)
	#Convert to decimal (0 prefix is interpreted as octal numbers)
	TEMP=$((10#$TEMP))

	echo $TEMP
}

#Display usage if incorrectly used
if [ "$#" -ne 2 ]; then
	echo "ERROR: Invalid parameters"
	echo
	echo "Update Version script."
	echo " - Updates (and exports as environment variables) all version"
	echo "   components contained in the [PROJECT_PATH]'s version.properties file "
	echo " - Compares current source to previous tag"
	echo " - Automatically updates the REVISION, BUILD and SUFFIX components as required"
	echo " -- REVISION: Incremented by one if [INCREMENT_REVISION]=1 and built from master"
	echo " -- BUILD   : On master builds, set to the number of commits in repo (otherwise '0001')"
	echo " -- SUFFIX  : On non-master builds, set to '{HASH_WHEN_BRANCHED}_{NUM_BRANCH_COMMITS}_{CURRENT_HASH}'"
	echo
	echo "Usage:"
	echo "update_version.sh [PROJECT_PATH] [INCREMENT_REVISION]"
	echo "  PROJECT_PATH       - Path to target project directory where version.properties resides (e.g. './payment')"
	echo "  INCREMENT_REVISION - Indicates whether to increase the REVISION by 1; used when tagging (e.g. '0'/'1'"
	exit 1
fi

PROJECT_PATH=$1
echo "====== Update ${PROJECT_PATH^^} Version ====="

#Navigate to root dir if needed
if [ ! -f "bitbucket-pipelines.yml" ]; then
	cd ..
	if [ ! -f "bitbucket-pipelines.yml" ]; then
		echo "Project root not found, unable to update version"
		exit 1
	fi 	
fi

#Check file path parameter is valid
PROPS_FILE=version.properties
TMP_FILE=$PROPS_FILE.tmp
FULL_PATH=$PROJECT_PATH/$PROPS_FILE
if [ ! -f $FULL_PATH ]; then
	echo "File '$FULL_PATH' doesn't exist, terminating"
	exit 1
fi

pushd $PROJECT_PATH

#Get increment revision parameter (defaulting to disabled)
INCREMENT_REVISION=$2
if [ "$INCREMENT_REVISION" != "0" ]; then
	#TODO RPAW: Sort out incrementing revision
	echo "INCREMENT_REVISION NOT YET IMPLEMENTED"
	INCREMENT_REVISION=0
fi

#If not already set, determine whether this is a master build
if [ "$MASTER_BUILD" != "1" ]; then
	MASTER_BUILD=0
	if [ "$(git rev-parse --abbrev-ref HEAD)" = "master" ]; then
		echo "Detected master build"
		MASTER_BUILD=1
	elif [ "$INCREMENT_REVISION" = "1" ]; then
		echo "Not a master build, overriding INCREMENT_REVISION"
		INCREMENT_REVISION=0
	fi
fi

#Determine the base count for the BUILD component
BUILD_BASE=0
	# The following constants were determined by `git rev-list --count origin_linkly`
if [[ $PROJECT_PATH == extp2pe/* ]]; then
	BUILD_BASE=431;
else
	BUILD_BASE=4127;
fi

#Remove temp file
rm -f $TMP_FILE

#Determine whether there have been changes
# - calculate current hash (i.e. of all files except the version.properties file)
PROJECT_HASH=$(git ls-files -s . | grep -v "$PROPS_FILE" | git hash-object --stdin)
# - extract previous saved has value from version.
printf -v PREV_REV_HASH '%s' $(sed -nr '/^'"PREV_REV_HASH"'=/ s/.*^'"PREV_REV_HASH"'=([^"]+).*/\1/p' $PROPS_FILE)
# - compare calculated and saved hash values to determine whether there's any changes
NO_CHANGES=0
if [ "$PREV_REV_HASH" = "$PROJECT_HASH" ]; then
	#Project hash is identical
	NO_CHANGES=1
elif [ "$INCREMENT_REVISION" = "1" ]; then
	#Update hash to new value
	PREV_REV_HASH=$PROJECT_HASH
fi

echo "BUILD_SERVER      : '$BUILD_SERVER'"
echo "FULL_PATH         : '$FULL_PATH'"
echo "INCREMENT_REVISION: '$INCREMENT_REVISION'"
echo "MASTER_BUILD      : '$MASTER_BUILD'"
echo "NO_CHANGES        : '$NO_CHANGES'"

#Set sensible defaults
MAJOR=0
MINOR=0
REVISION=0
BUILD=0001
SUFFIX=

#Loop through input file, parsing/modifying version components and outputting to temp file
while read p; do
	OUT_LINE=$p
	if [[ $p != "#"* ]]; then
		ORIGINAL_LINE=$(remove_whitespace $p)
		OUT_LINE=$ORIGINAL_LINE

		#Parse version components and update as necessary
		if [[ $ORIGINAL_LINE == MAJOR* ]]; then
			MAJOR=$(get_value $ORIGINAL_LINE)
		elif [[ $ORIGINAL_LINE == MINOR* ]]; then
			MINOR=$(get_value $ORIGINAL_LINE)
			MINOR=`printf "%d" $MINOR`
		elif [[ $ORIGINAL_LINE == REVISION* ]]; then
			REVISION=$(get_value $ORIGINAL_LINE)
			if [ "$INCREMENT_REVISION" = "$NO_CHANGES" ]; then
				# if INCREMENT_REVISION
				# - prevent increment if there are no changes
				# else
				# - prevent decrement if there are changes
				echo "no change required to '${ORIGINAL_LINE}'"
			elif [ "$INCREMENT_REVISION" = "1" ]; then
				let REVISION++
			elif [ "$NO_CHANGES" = "1" ]; then
				let REVISION--
			fi
			REVISION=`printf "%2d" $REVISION`
			OUT_LINE="REVISION=${REVISION}"
		elif [[ $ORIGINAL_LINE == BUILD* ]]; then
			#TODO RPAW: This has been disabled - build server will always do this
			if [[ "$BUILD_SERVER" == "1" ]]; then
				#Set Build component to number of commits (master/develop only)
				printf -v BUILD "%04u" $(expr $(git rev-list --count HEAD) - $BUILD_BASE)
			else
				BUILD="0001"
			fi
			OUT_LINE="BUILD=${BUILD}"
		elif [[ $ORIGINAL_LINE == SUFFIX* ]]; then
			#TODO RPAW: This has been disabled - build server will always do this
			if [[ "$BUILD_SERVER" != "1" ]]; then
				#Set Suffix component (branch only)
				# The hash of the commit when branched from master
				HASH_WHEN_BRANCHED=`expr substr $(git merge-base origin/master HEAD) 1 4`
				# Number of commits since branch from master
				NUMBER_COMMITS_SINCE_BRANCH=$(git rev-list --count HEAD ^origin/master)
				# Current branch commit hash
				CURRENT_BRANCH_HASH=`expr substr $(git rev-parse HEAD) 1 4`

				printf -v SUFFIX "%s_%s_%s" $HASH_WHEN_BRANCHED $NUMBER_COMMITS_SINCE_BRANCH $CURRENT_BRANCH_HASH
			fi
			OUT_LINE="SUFFIX=${SUFFIX}"
		elif [[ $ORIGINAL_LINE == PREV_REV_HASH* ]]; then
			OUT_LINE="PREV_REV_HASH=${PREV_REV_HASH}"
		fi

		if [[ "$ORIGINAL_LINE" != "$OUT_LINE" ]]; then
			TEMP="- ${ORIGINAL_LINE} > ${OUT_LINE}"
			echo $TEMP
		fi
	fi

	echo $OUT_LINE >> $TMP_FILE
done < $PROPS_FILE

#Replace original properties file with newly constructed one
rm -f $PROPS_FILE
mv $TMP_FILE $PROPS_FILE

#Construct version strings
SHORT_VERSION_NAME="${MAJOR}.${MINOR}.${REVISION}"
FULL_VERSION_NAME="${SHORT_VERSION_NAME}.${BUILD}"
if [ "$SUFFIX" != "" ]; then
	FULL_VERSION_NAME="${FULL_VERSION_NAME}-${SUFFIX}"
fi
TAG_VER="v${SHORT_VERSION_NAME}"

#Output version components/strings
echo " MAJOR   =$MAJOR"
echo " MINOR   =$MINOR"
echo " REVISION=$REVISION"
echo " BUILD   =$BUILD"
echo " SUFFIX  =$SUFFIX"
echo "---------------------------"
echo " SHORT_VERSION_NAME=$SHORT_VERSION_NAME"
echo " FULL_VERSION_NAME =$FULL_VERSION_NAME"
echo " TAG_VER           =$TAG_VER"
echo "==========================="

#export version components/strings
export MAJOR
export MINOR
export REVISION
export BUILD
export SUFFIX
export SHORT_VERSION_NAME
export FULL_VERSION_NAME
export TAG_VER
export NO_CHANGES

#Write version strings to files
echo $SHORT_VERSION_NAME > ver_short.tmp
echo $FULL_VERSION_NAME > ver_full.tmp
echo $TAG_VER > ver_tag.tmp

popd
