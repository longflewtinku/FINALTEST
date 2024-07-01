
Linkly: How to Cable Load Linkly Applications
========================================================================

The following document will list out the ways applications can be loaded onto an Android Terminal via USB connection.
The document makes certain assumptions

1. The terminal is in Debug Mode
2. The applications do not need to be signed

If either of the two assumption is wrong, ignore the document and raise a ticket. 

========================================================================

1. Wipe Linkly Apps:
    - If the terminal needs to be wiped, call the uninstall_all.bat file which will uninstall all Linkly apps from the terminal. 
    - Ignore any exceptions/errors you see here

2. Integrated Mode
    - Wipe Linkly Apps.
    - Start the file 'Install Integrated Mode Apps.bat file'. This will ask you what flavour of connect app you wish to install
    - Choose the correct one and it will install that flavour. By default, this will be PC EFTPOS Flavour (Legacy)

3. Standalone Payment App:
    - Wipe Linkly Apps.
    - If only a payment app is needed (i.e no POS communication), run 'Install Standalone Payment App.bat' file

4. Config Changes:
    - If some config needs to be changed, for example: TID, then change the config in the relevant XML file
    - Run 'Install_config.bat' file which will install the customer package & the XML files on to the terminal.

5. Capture Logs:
    - This feature is only useful for Debug Builds & the file won't be present in the Release folder
    - If the terminal is connected via USB & that USB is not being used for Internet, Android Logs can be captured.
    - Logs will be captured in Log_{Date}_{Time} file generated in the same folder.
    - Note, this file will record all of the Android logs so it will eventually become a very large file with size

========================================================================