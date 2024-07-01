from jproperties import Properties
from git import Repo
import sys
import re

## Global variable used to store the tagName for future use
tagName = ""

## Will check if the current commit is a tag
def isTagCommit():
    global tagName
    repo = Repo('../')
    
    for tag in repo.tags:
        if((str(repo.head.commit)))==(str(tag.commit)):
            tagName = str(tag)
            print(tagName + " is a Tag")
            return True
        
    print( "Is not a Tag")
    return False

## Checks to make sure the tag is in the format vX.Y.Z
## Where    X => Major Version number
##          Y => Minor Version number
##          Z => Revision Number
def isTagValid():
    global tagName
    # match digit . digit . digit [any other chars]
    r = re.compile('v\d.\d.\d.*')
    valid = r.match(tagName) is not None

    if( valid ):
        print( tagName + " is a valid tag" )
    else: 
        print( tagName + " is not valid")

    return valid

if(isTagCommit() and isTagValid()):
    ## if true, we will use the tagName to fill out major, minor & revision fields
    configs = Properties()
    with open('../version.properties', 'rb+') as config_file:
        configs.load(config_file,"utf-8")
        
        # Remove the 'v' character
        version = tagName[1:].split('.')
        print("version = " + str(version) )

        configs["MAJOR"]=version[0]
        configs["MINOR"]=version[1]
        configs["REVISION"]=version[2]

        # Enforce our rule for all tag versioning to be numeric. This is due to some vendors are unable to handle alpha characters.
        for number in version:
            if(number.isdigit() == False):
                # debug the error then let stderr result in our tagging to break
                print("Invalid version value:" + number + " - Numeric values only!")
                sys.stderr.write("Invalid version value:" + number + " - Numeric values only!")

        # If a hotfix value has been set append it (otherwise it will default to 0)
        if(len(version) > 3):
            configs["HOTFIX"]=version[3]

        if( len(sys.argv) > 1 ):
            versionType = str(sys.argv[1])
            print("suffix is " + versionType )
            configs["SUFFIX"] = versionType
            buildNumber = configs["BUILD"]
            if( versionType == 'P' ):
                configs["BUILD"] = buildNumber.data + "1"
            elif ( versionType == 'R' ):
                configs["BUILD"] = buildNumber.data + "0"
        

        config_file.seek(0)
        config_file.truncate(0)
        configs.store(config_file,"utf-8")
