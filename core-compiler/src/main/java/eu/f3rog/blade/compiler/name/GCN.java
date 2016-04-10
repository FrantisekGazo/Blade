package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Enum {@link GCN}
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
        StringBuilder name = new StringBuilder();
        ClassName className = arg;

        while (className != null) {
            if (className != arg) {
                name.insert(0, "_");
            }
            name.insert(0, className.simpleName());

            className = className.enclosingClassName();
        }

        return String.format(mName, name.toString());
    }
}
