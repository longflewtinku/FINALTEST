TT Fuel Setup Notes
======================

### Table of Contents
1. [Introduction](#introduction)
2. [Release Dependencies](#release-dependencies)
3. [Installation](#installation)
4. [EFTEX host configurations](#eftex-host-configurations)
5. [POS configuration](#pos-configuration)

## Introduction

This folder includes load files for the TT Fuel customer.

Target (MVP project phase) modes of operation are:
- Local REST/MPOS
- Cloud integrated
- On-premises integrated

TT Fuel acquirer/switch is EFTEX
EFTEX bank communications (as2805 requests/responses) are not routed via Linkly gateways - they go direct from the terminal to EFTEX.

See section [EFTEX test configurations](#eftex-test-configurations) below for details on EFTEX test link options and configuration.    

## Release Dependencies

This release requires a specific minimum OS version for Pax A920 of PayDroid_7.1.2_Aquarius_V02.5.25_20220301 or newer
This can be found on sharepoint here:
https://pceftpos.sharepoint.com/:u:/s/IAAS/EXStv46oslBDryyC4Zq0U9wBl3gmVQNlhoYb1Hkiq35hzQ?e=G9EIyS

Note: if running on A77, A920 Pro etc, the OS date must be at least 1 March 2022 (the date is part of the release version, labelled year, month, day in YYYYMMdd format)

## Installation

For installation of packages from the build pipelines, look [here](./devtools/cable_load_files/ReadMe.md)

## EFTEX host configurations

At time of release, we have 2 options for EFTEX host connectivity.

EFTEX BP-Node is the default host.

#### EFTEX BP-Node host

- this is a test system provided by EFTEX
- a custom TNS SIM card is required to be in the terminal
- external IP address/port is 10.232.183.153, port 6050. 
- the test host is NOT accessible by the open internet, so WiFi can't be used. Disable wifi in the terminal. 
- Use a unique TID - any TID can be used, there is no validation on this. If multiple users are using the same TID, MACcing errors and msg encrypt/decrypt errors will occur

#### Linkly Internal Host Emulator

- this is a linkly developed host emulator that is running on a server in the Auckland office, on machine EFT-Server2
- external IP address/port is 101.98.92.81, port 6050
- this test host is accessible via the open internet, so WiFi can be used. No SIM card is necessary. You can leave the SIM inserted, but just turn on WiFi. WiFi has priority over the SIM card
- Use a unique TID - any TID can be used, there is no validation on this. If multiple users are using the same TID, MACcing errors and msg encrypt/decrypt errors will occur

### Changing target host

#### For Paxstore loaded 
- Under the Acquirer tab, set "Host Primary" and "Host Secondary" to desired endpoint 
- "Comms Type" must be "TCP Direct No TPDU" for EFTEX host connections
- "Route Bank msgs through POS" must be "N"
- "SSL Enabled" must be "N"

#### For Cable loaded
- edit overrideparams.xml file to comment/uncomment the desired payment switch settings
- run 'install config.bat'

## POS configuration

The Linkly Connect app can work in one of two modes. Note that (currently) it doesn't support both modes concurrently.

By default mPOS/Local REST is enabled, and On-premises/Cloud integration won't work. 

#### mPOS/Local REST mode 
In this mode, the terminal accepts incoming REST commands conforming to the Linkly Local REST API specification:
https://www.linkly.com.au/apidoc/LocalREST/#api-specification

#### External POS integration mode
This is the traditional cloud or on-premises integration method

### Changing POS Mode

#### For Paxstore loaded 
- when pushing Linkly Connect app, set Local REST (mPOS) enabled to true for mPOS mode or false for External POS mode

#### For Cable loaded
- edit cfg_connect.xml, set <localRestEnabled> to true for mPOS mode or false for External POS mode
- run 'install config.bat'