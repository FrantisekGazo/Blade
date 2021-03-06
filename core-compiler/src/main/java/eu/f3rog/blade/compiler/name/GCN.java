package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Enum {@link GCN} represents a Generated Class Name.
 *
 * @author FrantisekGazo
 * @version 2015-10-16
 */
public enum GCN {

    HELPER("%s_Helper"),
    FRAGMENT_FACTORY("F"),
    INTENT_MANAGER("I"),
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

    public String formatName(final ClassName arg) {
        return String.format(mName, NameUtils.getNestedName(arg));
    }
}
