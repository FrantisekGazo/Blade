package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Class {@link ClassNames}.
 *
 * @author Frantisek Gazo
 * @version 2015-09-26
 */
public interface ClassNames {

    // Android support lib classes
    ClassName AppCompatActivity = ClassName.get("android.support.v7.app", "AppCompatActivity");
    ClassName SupportFragment = ClassName.get("android.support.v4.app", "Fragment");

}
