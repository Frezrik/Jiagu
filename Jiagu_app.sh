#!/bin/bash

echo '===> app clean...'
./gradlew app:clean >/dev/null 2>&1

echo '===> build app...'
./gradlew app:assembleRelease >/dev/null 2>&1

cp app/build/outputs/apk/release/app-release.apk input/