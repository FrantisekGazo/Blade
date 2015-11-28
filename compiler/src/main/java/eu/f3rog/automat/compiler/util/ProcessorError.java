package eu.f3rog.automat.compiler.util;

import javax.lang.model.element.Element;

import eu.f3rog.automat.compiler.ErrorMsg;

/**
 * Class {@link ProcessorError}
 *
 * @author FrantisekGazo
 * @version 2015-10-15
 */
public class ProcessorError extends Exception {

    private final Element mElement;

    public ProcessorError(Element e, ErrorMsg msg, Object... args) {
        super(String.format(msg.toString(), args));
        mElement = e;
    }

    public Element getElement() {
        return mElement;
    }
}
