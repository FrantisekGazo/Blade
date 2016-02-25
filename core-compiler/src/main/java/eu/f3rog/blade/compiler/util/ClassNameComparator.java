package eu.f3rog.blade.compiler.util;

import com.squareup.javapoet.ClassName;

import java.util.Comparator;

/**
 * Class {@link ClassNameComparator}
 *
 * @author FrantisekGazo
 * @version 2015-12-06
 */
public class ClassNameComparator implements Comparator<ClassName> {

    @Override
    public int compare(ClassName o1, ClassName o2) {
        if (o1 == null || o2 == null) return 0;
        int res = o1.simpleName().compareTo(o2.simpleName());
        if (res == 0) {
            return o1.packageName().compareTo(o2.packageName());
        } else {
            return res;
        }
    }

}
