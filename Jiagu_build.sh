#!/bin/bash

echo '===> clean...'
./gradlew jiagu:clean >/dev/null 2>&1
./gradlew pack:clean >/dev/null 2>&1

echo '===> build jiagu...'
./gradlew jiagu:assemble >/dev/null 2>&1

echo '===> build pack...'
./gradlew pack:assemble >/dev/null 2>&1

cp ./pack/build/libs/pack.jar .