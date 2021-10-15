@echo off &setlocal enabledelayedexpansion

echo ===^> jiagu app...
echo output:

for %%i in (input\*.apk) do (
    set name=%%~nxi
    java -jar pack.jar -apk %%i -key keystore/testkey.jks -kp test111 -alias jiagu -ap test111
    adb install -r output/!name:~0,-4!_signed.apk
    )

pause