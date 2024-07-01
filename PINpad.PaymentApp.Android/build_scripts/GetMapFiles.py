import os
import shutil

mapFile = "mapping.txt"
def copyAndRenameFile( filePath, newFileName ):
    pathFolder = r"MapFiles"
    if not os.path.isdir( pathFolder ):
        os.mkdir( pathFolder )

    if os.path.isfile( filePath ):
        print( filePath + " exists" )
        shutil.copyfile( filePath, pathFolder + "/" + newFileName + '.txt' )
    else: 
        print( filePath + " doesn't exist" )


mapPath = "/build/outputs/mapping/"


copyAndRenameFile( r"../payment" + mapPath + "release/" + mapFile, r"payment" )
copyAndRenameFile( r"../launcher" + mapPath + "release/" + mapFile, r"launcher" )
copyAndRenameFile( r"../extp2pe/secapp" + mapPath + "release/" + mapFile, r"secapp")
# Connect App & variants
copyAndRenameFile( r"../connect" + mapPath + "pa_demoRelease/" + mapFile, r"connect_pa_demo" )
copyAndRenameFile( r"../connect" + mapPath + "pa_linklyRelease/" + mapFile, r"connect_pa_linkly" )
copyAndRenameFile( r"../connect" + mapPath + "pa_vfiRelease/" + mapFile, r"connect_pa_vfi" )
copyAndRenameFile( r"../connect" + mapPath + "pa_zellerRelease/" + mapFile, r"connect_pa_zeller" )
