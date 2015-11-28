package eu.f3rog.automat.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Enum {@link GCN}
 *
 * @author FrantisekGazo
 * @version 2015-10-16
 */
public enum GCN {

    NAVIGATOR("I"),
    FRAGMENT_FACTORY("F"),
    INJECTOR("Injector"),
    CLASS_INJECTOR("%s_Injector"),
    ;

    private String mName;

    GCN(String mName) {
        this.mName = mName;
    }

    public String getName() {
        return mName;
    }

    public String formatName(String arg) {
        if (arg != null) {
            return String.format(mName, arg);
        } else {
            return mName;
        }
    }
    public String formatName(ClassName arg) {
        if (arg != null) {
            return String.format(mName, arg.simpleName());
        } else {
            return mName;
        }
    }
}
