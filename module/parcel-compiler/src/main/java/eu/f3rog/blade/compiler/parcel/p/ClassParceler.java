package eu.f3rog.blade.compiler.parcel.p;

/**
 * Class {@link ClassParceler}
 *
 * @author FrantisekGazo
 * @version 2016-01-24
 */
interface ClassParceler {

    Class type();

    /**
     * Returns format of write call.
     */
    CallFormat writeCall();

    /**
     * Returns format of write call.
     */
    CallFormat readCall();
}
