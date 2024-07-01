Connect App Setup
======

version 1.0
Date: 5th Feb 2021

### Table of Contents
1. [Introduction](#Introduction)
2. [Legacy Connect App](#legacy-connect-app)
3. [Local Rest API Connect App](#local-rest-api-connect-app)

---

## Introduction

The document deals with how to configure & use various flavours of Connect App.

## Legacy Connect App

- After installation of native connect app, you should see a screen saying 'POS not configured.'
- You need to get the IP Address of the Windows system where the EFT Client is running. You can do this by either checking the network settings or typing `ipconfig` on the command line.

- On the app, press 'Function' & enter 11112227 as the function number. Go to IP Settings button.
- Enter the IP Address of the Windows machine gained above and press OK.
- You should get back to the home screen and you should see 'POS Connected' text on the screen.
- If App screen says 'Connecting to POS...' for a long time, you need to review your firewall settings. You may need to add a rule to allow connections at port 2012 (default port for EFT Client)

## Local Rest API Connect App

Note: This is a temporary solution. Ideally, this should be done by having another app on the terminal.

- You need to install [Postman](https://www.postman.com/downloads/) on the Windows machine for testing. You will also have to create a free account as you need to use Workspaces under Postman.
- Open Postman. You need to import [LinklyLocalRestAPI.postman_collection.json](../connect/) file which is located in `PINpad.PaymentApp.Android\connect` folder. 
- You should see **LinklyLocalRestApi** collection with *Logon Request*, *Transaction Request*, etc under the collection.
- If you click on any of the request, you will see the http POST Command. It requires two enviroment variables: Terminal IP Address & Session ID.
- We will ignore Session ID for now.
- You need to know the Terminal IP Address. For this:
    1. Open Command Prompt in a folder with ADB.exe file
    2. Type the following command `adb shell ip route`
- In Postman, Create a new enviroment. Create a new variable in the enviroment `terminal_ip` with the Terminal IP Address as the current value. Save the environment. 
- Back to the Collection/request. You should see on the top right there is a drop down button for environments. Set the enviroment as the one you just created.
- Hover over the `terminal_ip` on the http request bar and you should see the Enviroment value.
- Send a request to the terminal and after the terminal performs the action, it should return a response. You should be able to see the whole JSON response. 