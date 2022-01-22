#!/bin/bash

echo '===> jiagu app...'
echo 'output:'

files=$(ls input| grep .apk)

for file in $files
do
  java -jar pack.jar -apk input/$file -key keystore/testkey.jks -kp test111 -alias jiagu -ap test111
  adb install -r output/${file%.apk}_signed.apk
done