package eu.f3rog.automat.compiler;

import eu.f3rog.automat.Extra;

/**
 * Enum {@link ErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public enum ErrorMsg {

    Invalid_class_with_Extra("Only Activity subclass can contain @%s annotations.", Extra.class.getSimpleName()),
    Extra_cannot_be_private_or_protected("Field annotated with @%s cannot be private or protected", Extra.class.getSimpleName()),
    Extra_cannot_be_final("Field annotated with @%s cannot be final", Extra.class.getSimpleName()),
    ;

    private String mMessage;

    ErrorMsg(String msgFormat, Object... args) {
        this.mMessage = String.format(msgFormat, args);
    }

    @Override
    public String toString() {
        return mMessage;
    }
}
