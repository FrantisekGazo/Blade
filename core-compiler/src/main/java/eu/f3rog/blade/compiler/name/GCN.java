package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Class {@link GCN} for Generated Class Names
 *
 * @author FrantisekGazo
 * @version 2016-08-09
 */
public class GCN {

    /** HELPER */
    public static final GCN HELPER = new GCN("%s_Helper");

    private String mName;

    public GCN(String mName) {
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
