package eu.f3rog.blade.core;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Weave {

    String WEAVE_FIELD = "<FIELD>";
    String WEAVE_CONSTRUCTOR = "";

    String into();

    String[] args() default {};

    String statement();

}
