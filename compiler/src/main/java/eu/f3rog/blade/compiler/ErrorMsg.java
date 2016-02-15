package eu.f3rog.blade.compiler;

import blade.Arg;
import blade.Extra;
import blade.Presenter;
import blade.State;
import blade.mvp.IPresenter;
import blade.mvp.IView;

/**
 * Enum {@link ErrorMsg}
 *
 * @author FrantisekGazo
 * @version 2015-10-17
 */
public enum ErrorMsg {

    Invalid_place("Annotation not allowed here."),
    Invalid_class_with_Extra("Only Activity or Service subclass can contain @" + Extra.class.getSimpleName() + "."),
    Invalid_class_with_Arg("Only Fragment subclass can contain @" + Arg.class.getSimpleName() + "."),
    Invalid_field_with_annotation("Field annotated with @%s cannot be private, protected nor final"),
    View_cannot_implement_state_methods("View subclass containing @" + State.class.getSimpleName()
            + " cannot implement 'onSaveInstanceState()' nor 'onRestoreInstanceState()' methods."
            + " These methods will be implemented by Blade library."),
    Invalid_class_with_Presenter("Only View subclass that implements " + IView.class.getCanonicalName() + " can contain @" + Presenter.class.getSimpleName() + "."),
    Invalid_Presenter_class("@" + Presenter.class.getSimpleName() + " has to be non-abstract class that implements " + IPresenter.class.getCanonicalName()),
    Inconsistent_Presenter_parameter_classes("All @" + Presenter.class.getSimpleName() + "s has to implement " + IPresenter.class.getCanonicalName() + " with the same parameter type."),
    Presenter_class_cannot_be_parametrized("@" + Presenter.class.getSimpleName() + " class cannot e parametrized."),
    Presenter_class_missing_default_constructor("Presenter class has to contain default constructor.");

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
