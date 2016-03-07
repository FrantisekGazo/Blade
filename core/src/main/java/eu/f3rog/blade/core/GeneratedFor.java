package eu.f3rog.blade.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation {@link GeneratedFor}
 *
 * @author FrantisekGazo
 * @version 2016-03-07
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface GeneratedFor {

    Class value();

}
