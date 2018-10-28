#!/usr/bin/env bash

rm -Rf ~/Library/Android/sdk/extras/android/m2repository/eu/
rm -Rf ~/Library/Android/sdk/extras/android/m2repository/eu/
./gradlew clean install
mkdir ~/Library/Android/sdk/extras/android/m2repository/eu/
cp -R ~/.m2/repository/eu/ ~/Library/Android/sdk/extras/android/m2repository/eu/

#./gradlew :plugin:test
./gradlew :sample:assembleProdDebug
