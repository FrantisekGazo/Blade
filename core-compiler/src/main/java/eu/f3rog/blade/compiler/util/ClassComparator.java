package eu.f3rog.blade.compiler.util;

import java.util.Comparator;


public final class ClassComparator implements Comparator<Class> {

    @Override
    public int compare(Class o1, Class o2) {
        if (o1 == null || o2 == null) return 0;
        return o1.getCanonicalName().compareTo(o2.getCanonicalName());
    }
}
