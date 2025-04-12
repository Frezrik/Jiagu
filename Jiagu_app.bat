@echo off

echo ===^> app clean...
call gradlew app:clean >NUL 2>&1

echo ===^> build app...
call gradlew app:resguardRelease >NUL 2>&1

xcopy app\build\outputs\apk\release\app-release.apk input /y

pause