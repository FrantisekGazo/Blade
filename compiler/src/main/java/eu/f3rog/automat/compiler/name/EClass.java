package eu.f3rog.automat.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Class {@link EClass}.
 *
 * @author Frantisek Gazo
 * @version 2015-09-26
 */
public enum EClass {

    // Android support lib classes
    AppCompatActivity(ClassName.get("android.support.v7.app", "AppCompatActivity")),
    SupportFragment(ClassName.get("android.support.v4.app", "Fragment")),
    ;

    private ClassName mClassName;

    EClass(ClassName className) {
        mClassName = className;
    }

    public ClassName getName() {
        return mClassName;
    }

}
