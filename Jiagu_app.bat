@echo off

echo ===^> clean...
call gradlew clean >NUL 2>&1

echo ===^> build app...
call gradlew app:assembleRelease >NUL 2>&1

xcopy app\build\outputs\apk\release\app-release.apk input /y

pause