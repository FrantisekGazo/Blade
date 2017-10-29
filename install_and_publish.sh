#!/usr/bin/env bash

./gradlew clean install
./gradlew bintrayUpload
