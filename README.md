[中文文档](https://github.com/Frezrik/Jiagu/blob/main/README_CN.md)

---

# APK reinforcement
* Support Android 5.0 and above (tested on 5.1, 7.1, 8.1, 10.0, 11.0 real machines)
* Support multi-dex reinforcement
* Adopt no-landing loading dex scheme

## I. Module description
### Tool development debugging use
* app: the source code for test
* jiagu: used to generate shell dex
* pack: java executable for packaging the hardened app
### Release tools
* JigguTool: package of hardening tools, if you only need to use the hardening function, you can directly use the tools in this directory

## II. Script description
### Tool development debugging use
* Jiagu_app.bat: compile and package the app, the packaged application is used for hardening test
* Jiagu_build.bat: compile and generate hardening tools
* Jiagu_input.bat: harden the apk under input directory
### Release tool
* Jiagu_update.bat: update the tools compiled by Jiagu_build to the JiaguTool directory

## III. Instructions for use
### Method 1 (tool development and debugging use).
* 1. Execute the script Jiagu_build.bat to generate the hardening tool
* 2. Put the application to be reinforced into the input folder, execute the script Jiagu_input.bat to reinforce it, and the reinforced application will be exported to the output folder.
### Method 2 (Release tool).
* The hardening environment will be packaged to JiaguTool, you can use jiaguTool to harden the application directly
* If you modify the code of the reinforcement tool, you can execute Jiagu_updata.bat to repackage the reinforcement tool

## IV. Hardening principle
### 1. Hardening tool processing
* Get the application name of the app
* Modify the application of the app's AndroidManifest.xml to the dex shell application
* Shell dex and source dex are spliced into one dex in the following format.
    * shell dex + (1 byte application name length + app's application name + 4 bytes source dex size + source dex) + 4 bytes source dex2 size + [source dex2] + 4 bytes source dex3 size + [source dex3] + ... + 4-byte shell dex size
    * AES encryption for the first 512 bytes of data in parentheses
    * dex header for data in middle brackets (i.e., first 112 bits for iso-or)
* Packaged signed app

### 2. Shell dex processing
* decrypt the application name and dex of the app
* Load the app's dex
    * Android8.0 below use call libart's OpenMemory function to load dex in memory
    * Android8.0 and above use the InMemoryDexClassLoader provided by the system to load the dex in memory
* replace shell application with application of app


