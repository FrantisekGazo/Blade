package eu.f3rog.automat.compiler;

import eu.f3rog.automat.Arg;
import eu.f3rog.automat.Extra;

/**
 * Enum {@link ErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public enum ErrorMsg {

    Invalid_class_with_Extra("Only Activity subclass can contain @%s annotations.", Extra.class.getSimpleName()),
    Invalid_Extra("Field annotated with @%s cannot be private or protected nor final", Extra.class.getSimpleName()),
    Invalid_class_with_Arg("Only Fragment subclass can contain @%s annotations.", Arg.class.getSimpleName()),
    Invalid_Arg("Field annotated with @%s cannot be private or protected nor final", Arg.class.getSimpleName()),
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
