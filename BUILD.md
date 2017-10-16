# Build steps:

1. Add build info to `CHANGELOG.md`
2. Update version name in `gradle.properties` and `plugin/src/main/groovy/BladePlugin.groovy`
3. Clean & build the project `./gradlew clean install`
4. Upload to Bintray `./gradlew bintrayUpload`
