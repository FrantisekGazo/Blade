package eu.f3rog.blade.compiler;

/**
 * Enum {@link ErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public interface ErrorMsg {

    String Invalid_place = "Annotation not allowed here.";
    String Invalid_field_with_annotation = "Field annotated with @%s cannot be private, protected nor final";

}
