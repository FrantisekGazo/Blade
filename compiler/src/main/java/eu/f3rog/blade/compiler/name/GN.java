package eu.f3rog.blade.compiler.name;

import com.squareup.javapoet.ClassName;

/**
 * Class {@link GN}
 *
 * @author FrantisekGazo
 * @version 2015-11-21
 */
public class GN {

    public static ClassName className(GCN genClassName, String arg, GPN... packages) {
        return ClassName.get(GPN.toString(packages), genClassName.formatName(arg));
    }

    public static ClassName className(GCN genClassName, GCN arg, GPN... packages) {
        return ClassName.get(GPN.toString(packages), genClassName.formatName(arg.getName()));
    }

    public static ClassName className(GCN genClassName, GPN... packages) {
        return ClassName.get(GPN.toString(packages), genClassName.getName());
    }

    public static String fqn(GCN genClassName, String arg, GPN... packages) {
        return GPN.toString(packages) + "." + genClassName.formatName(arg);
    }

    public static String fqn(GCN genClassName, GPN... packages) {
        return GPN.toString(packages) + "." + genClassName.getName();
    }

}
