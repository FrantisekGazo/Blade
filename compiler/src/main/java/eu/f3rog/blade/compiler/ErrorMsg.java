package eu.f3rog.blade.compiler;

import blade.Arg;
import blade.Extra;

/**
 * Enum {@link ErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public enum ErrorMsg {

    Invalid_place("Annotation not allowed here."),
    Invalid_class_with_Extra("Only Activity subclass can contain @" + Extra.class.getSimpleName() + " annotations."),
    Invalid_class_with_Arg("Only Fragment subclass can contain @" + Arg.class.getSimpleName() + " annotations."),
    Invalid_field_with_annotation("Field annotated with @%s cannot be private, protected nor final"),;

    private String mMessage;

    ErrorMsg(String msgFormat) {
        this.mMessage = msgFormat;
    }

    @Override
    public String toString() {
        return mMessage;
    }

    public String toString(Object... args) {
        return String.format(mMessage, args);
    }

}
