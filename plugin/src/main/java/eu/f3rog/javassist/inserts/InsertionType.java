package eu.f3rog.javassist.inserts;

/**
 * Represents type of point in method body, where new code will be inserted.
 *
 * @author FrantisekGazo
 */
public enum InsertionType {

    BEFORE_BODY, AFTER_BODY,
    BEFORE, AFTER
}
