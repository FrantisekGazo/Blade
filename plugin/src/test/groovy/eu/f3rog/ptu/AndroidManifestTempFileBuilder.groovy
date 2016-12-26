package eu.f3rog.ptu


/* package */ final class AndroidManifestTempFileBuilder
        extends TempFileBuilder {

    /* package */ AndroidManifestTempFileBuilder() {
        super('src/main/AndroidManifest.xml')
    }

    @Override
    public String getBody() {
        return """<?xml version="1.0" encoding="utf-8"?>
    <manifest package="com.example"
              xmlns:android="http://schemas.android.com/apk/res/android">

        <application>
            <activity android:name=".MainActivity">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN"/>

                    <category android:name="android.intent.category.LAUNCHER"/>
                </intent-filter>
            </activity>
        </application>
</manifest>
        """
    }
}
