Live Group Setup Notes
======================

### Table of Contents
1. [Introduction](#introduction)
2. [Release Dependencies](#release-dependencies)
3. [Installation](#installation)
5. [POS configuration](#pos-configuration)

## Introduction

This folder includes load files for the Demo customer.

Online transactions are simulated. Response code for financial transactions will be taken from Total Amount cents. To get transaction approval use values "00" or "08" (for ex. $6.00, $15.08).

Target (MVP project phase) modes of operation are:
- Local REST/MPOS
- Cloud integrated
- On-premises integrated

## Release Dependencies

This release requires a specific minimum OS version for Pax A920 of PayDroid_7.1.2_Aquarius_V02.5.25_20220301 or newer
This can be found on sharepoint here:
https://pceftpos.sharepoint.com/:u:/s/IAAS/EXStv46oslBDryyC4Zq0U9wBl3gmVQNlhoYb1Hkiq35hzQ?e=G9EIyS

Note: if running on A77, A920 Pro etc, the OS date must be at least 1 March 2022 (the date is part of the release version, labelled year, month, day in YYYYMMdd format)

## Installation

For installation of packages from the build pipelines, look [here](./devtools/cable_load_files/ReadMe.md)

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