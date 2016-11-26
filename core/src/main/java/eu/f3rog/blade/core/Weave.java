package eu.f3rog.blade.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Weave {

    String WEAVE_FIELD = "<FIELD>";
    String WEAVE_CONSTRUCTOR = "";

    String into();

    String[] args() default {};

    String statement();

}
