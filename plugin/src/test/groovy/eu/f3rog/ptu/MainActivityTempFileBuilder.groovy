package eu.f3rog.ptu

/* package */ final class MainActivityTempFileBuilder
        extends TempFileBuilder {

    /* package */ MainActivityTempFileBuilder() {
        super('src/main/java/com/example/MainActivity.java')
    }

    @Override
    public String getBody() {
        return """package com.example;

import android.app.Activity;

public class MainActivity extends Activity {}
        """
    }
}
