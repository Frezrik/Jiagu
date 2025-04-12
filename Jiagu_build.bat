@echo off

echo ===^> clean...
call gradlew jiagu:clean >NUL 2>&1
call gradlew pack:clean >NUL 2>&1

echo ===^> build jiagu...
call gradlew jiagu:assemble >NUL 2>&1

echo ===^> build pack...
call gradlew pack:assemble >NUL 2>&1

copy .\pack\build\libs\pack.jar

pause