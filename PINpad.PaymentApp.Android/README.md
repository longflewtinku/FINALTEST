Linkly Android App
==================

### Table of Contents
1. [Introduction](#Introduction)
2. [Command Line Build](#command-line-build)
3. [Release Procedure](#release-procedure)
4. [Production Build Deployment](#production-build-deployment)
5. [PAX Store App Deployment Procedure](#pax-store-app-deployment-procedure)
6. [Installation](#installation)
7. [Connect App Setup](#connect-app-setup)
8. [Release Debugging](#Release-Debugging)
## Introduction

This repository contains source code for Android Payment App, Android Connect App. Android Secure App is a submodule of this repository. 

## Command Line Build

DevOps uses Ubuntu environment & Java 8 to build APKs. To replicate the builds on a local machine, the following items would be needed:

1. AdaptOpenJDK [8](https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u292-b10/OpenJDK8U-jdk_x64_windows_hotspot_8u292b10.msi)
2. Gradle installed. 
3. Make sure that JAVA_HOME enviroment variable is pointing to OpenJDK 8 installation folder. 

To start builds:

1. Open command line in the base folder. 
2. Run `gradlew task` to see a list of all possible gradle tasks
3. For Debug build, run `gradlew assembleDebug`
4. For Release build, run `gradlew assembleRelease`

## Release Procedure

Use the following steps to create a release for Android device. This is the current procedure for SunCorp releases and will be updated in the future. 

- On the commit on Linkly, create a tag with the name `vX.Y.Z.[hotfix number]` and push to origin. **No digit should be zero padded and no alpha characters are to be used**. Note that [hotfix number] is optional and will be defaulted to 0 if not provided. 
- This should produce Debug, Release (UAT) & Production versions of the app. The difference is:
    - Debug builds have no obfuscation, no optimization. Perfect for testing on the desk. They have UAT config, therefore can accept test cards and will talk to test hosts
    - Release builds are optimized & obfuscated. They have UAT config, therefore can accept test cards and will talk to test hosts.
    - Prod builds have production config. They will try to production Linkly Cloud, production cloud and will reject test cards. This build type is to be only used when making a production release. 
- This should trigger a new build based on the tag you just created. It takes about 50mins to complete.
- On SharePoint or a locally synced [Integrated As A Service\Software Releases](https://pceftpos.sharepoint.com/sites/IAAS/Shared%20Documents/Forms/AllItems.aspx?viewid=1fdb53c3%2D218d%2D470a%2D9ebe%2D4f21e3ff2907&id=%2Fsites%2FIAAS%2FShared%20Documents%2FSoftware%20Releases) folder & create a new folder. The name of the folder should follow the current convention. Eg: *suncorp_v0.00.06_load_files*
- While the release build pipeline is running, go to [Jira](https://linkly.atlassian.net/secure/RapidBoard.jspa?rapidView=1), make sure that each development story to be released has a Fix Version
- Go to [Releases](https://linkly.atlassian.net/projects/IAAS?selectedItem=com.atlassian.jira.jira-projects-plugin:release-page), Select the unreleased version. Click on Release Notes.
- Copy the release notes. Edit the release_notes.html file in SharePoint folder and paste all copied contents into the folder. 
- After the release pipeline is successfully completed, you will need to download the artifacts from that release. You should have a received an email saying Build Succeeded. Click on View Results. Click on '1 published' on the Results page. Download the whole 'drop' folder. 
- Copy the customer folder being released and paste them into the SharePoint release folder you created at step 4. 
- Notify QA Team that a release has been made in the SharePoint folder via email

## Production Build Deployment

Currently the process to deploy to production is manual. To deploy to Production, the following steps need to be done on every build:

- Download the production artifacts onto SharePoint & follow the same convention as shown in Release [Procedure](#release-procedure).
- Each APK that needs to be deployed needs to be signed by [PPN](https://ppn.paxengine.com/). 
- The signed APK will have the format `{APK}_release_unsigned_signed.apk`.
- Traditionally, Linkly have been uploading release builds with the file format: `{APK_NAME}_release_unsigned.apk` & PAXStore won't accept changed PPN signed package names. You need to rename the signed APKs to resemble the old format. 
- Follow steps of [Pax Store app deployment procedure](#pax-store-app-deployment-procedure)

## PAX Store App Deployment Procedure

Check out [Pax Store App Deployment](./docs/Paxstore_app_deploy_procedure.md)


## Installation

For installation of packages from the build pipelines, look [here](./devtools/cable_load_files/ReadMe.md)

## Connect App Setup

Refer [this](./docs/Connect_App_Setup.md) document for detailed documentation on how to setup Connect app after installation.

## Release Debugging

Debugging in release builds is tougher but we can deobfuscate crash logs from Release builds. We need:

#### Release build crash log. There are two ways we can get logs

- Android Studio/ADB
- Using PAXStore. This requires access to the Administrator Center of PAXStore & the terminal to be registered under a merchant. 

#### Map file

Find the map file for each apk in the Release folder/MapFiles.zip

#### Android SDK

We need Proguard's deobfuscation tool. We need to navigate to Android Studio's SDK root folder. 

- SDK Root folder is usually in the folder: `c:\users\Joe\AppData\Local\Android\sdk`
- Navigate to the corresponding folder on your machine.
- In SDK Root folder, go to `tools\proguard\bin` & you should see the following batch files: Proguard.bat, ProguardGUI.bat & Retrace.bat
- For deobfuscating: we can either use ProguardGUI or Retrace.bat file. This example will use the batch script. 

### Deobfuscation

- You need to have the stacktrace of the crash log in a file. 
- Call retrace.bat script in command line by: `retrace.bat <mapping_file> <stacktrace_file>`