Till Setup Notes
======================

### Table of Contents
1. [Introduction](#introduction)
2. [Release Dependencies](#release-dependencies)
3. [Installation](#installation)
4. [Till host configurations](#till-host-configurations)
5. [POS configuration](#pos-configuration)

## Introduction

This folder includes load files for the Till customer.

Target (MVP project phase) modes of operation are:
- Cloud integrated
- On-premises integrated

Till acquirer/switch is Connex
Till bank communications (as2805 requests/responses) are not routed via Linkly gateways - they go direct from the terminal to Connex.

See section [Till host configurations](#till-host-configurations) below for details on Till test link options and configuration.    

## Release Dependencies

This release requires a specific minimum OS version for Pax A920 of PayDroid_7.1.2_Aquarius_V02.5.25_20220301 or newer
This can be found on sharepoint here:
https://pceftpos.sharepoint.com/:u:/s/IAAS/EXStv46oslBDryyC4Zq0U9wBl3gmVQNlhoYb1Hkiq35hzQ?e=G9EIyS

Note: if running on A77, A920 Pro etc, the OS date must be at least 1 March 2022 (the date is part of the release version, labelled year, month, day in YYYYMMdd format)

## Installation

For installation of packages from the build pipelines, look [here](./devtools/cable_load_files/ReadMe.md)

## Key Injection
- Currently validated with "Till 1024" and "Till 2048"

Note: The "keysets.json" file is used to update the PPID and the test keys. The Keys used are the test keys shared with FIS

## Till host configurations

TNS FQDN for FIS Connex host is ssltest.tnsi.com.au and port 47650

Note: Till host is available to perform logons and basic transactions. All Issuer links are not available.

#### Linkly Internal Host Emulator(BP-Sim)

- this is a linkly hosted Test tool to Emulate Till messages as per the spec. Its hosted on remote machine with IP 10.222.3.4
- IP address/port is 20.92.92.59, port 2009
- this test host is accessible via the open internet, so WiFi can be used. No SIM card is necessary. You can leave the SIM inserted, but just turn on WiFi. WiFi has priority over the SIM card
- Use a unique TID - any TID can be used, there is no validation on this.

### Changing target host

#### For Paxstore loaded 
BP-Sim
- Under the Acquirer tab, set "Host Primary" and "Host Secondary" to desired endpoint 
- "Route Bank msgs through POS" must be "N"
- "SSL Enabled" must be "N"
- "Disable Security"(paymentSwitch_disableSecurity) must be "Y"

Connex FIS(Enabled by Default)
- "SSL Enabled"(paymentSwitch_useSsl) must be "Y"
- SSL cert to be used is "stca.crt"
- "Disable Security"(paymentSwitch_disableSecurity) must be "N"

#### For Cable loaded
- edit initialparams.xml for TID/MID configuration
- edit "keysets.json" file to update the PPID
- edit overrideparams.xml file to comment/uncomment the desired payment switch settings
- run 'Install Standalone Only.bat' for standalone only installation
- run "Install All Apps.bat" for complete installation including connect app to operate in integrated mode 
- run 'Install config.bat' for only config update

## POS configuration

The Linkly Connect app can work in one of two modes. Note that (currently) it doesn't support both modes concurrently On-premises/Cloud integration. 

#### External POS integration mode
This is the traditional cloud or on-premises integration method
