package eu.f3rog.blade.compiler.util;

import javax.lang.model.element.Element;

/**
 * Class {@link ProcessorError}
 *
 * @author FrantisekGazo
 * @version 2015-10-15
 */
public class ProcessorError extends Exception {

    private final Element mElement;

    public ProcessorError(Element e, String msg, Object... args) {
        super(String.format(msg, args));
        mElement = e;
    }

    public Element getElement() {
        return mElement;
    }
}
