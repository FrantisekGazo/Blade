package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Enum {@link GCN}
 *
 * @author FrantisekGazo
 * @version 2015-10-16
 */
public enum GCN {

    MIDDLE_MAN("MiddleMan"),
    HELPER("%s_Helper"),
    FRAGMENT_FACTORY("F"),
    ACTIVITY_NAVIGATOR("I"),
    STATE_MANAGER("StateManager"),
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
