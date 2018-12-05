package eu.f3rog.blade.compiler.parcel.p;

import com.squareup.javapoet.TypeName;

/**
 * Class {@link BaseParceler}
 *
 * @author FrantisekGazo
 */
interface BaseParceler {

    TypeName type();

    /**
     * Returns format of write call.
     */
    CallFormat writeCall();

    /**
     * Returns format of write call.
     */
    CallFormat readCall();
}
