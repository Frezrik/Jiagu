@echo off
xcopy bin JiaguTool\bin /S /y
xcopy output\unzip\shell\jni JiaguTool\bin /S /y
xcopy output\unzip\shell\classes.dex JiaguTool\bin /y
xcopy pack.jar JiaguTool /y

pause