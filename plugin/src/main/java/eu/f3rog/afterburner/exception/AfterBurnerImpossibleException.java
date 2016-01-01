package eu.f3rog.afterburner.exception;

@SuppressWarnings("serial")
public class AfterBurnerImpossibleException extends Exception {

    public AfterBurnerImpossibleException() {
        super();
    }

    public AfterBurnerImpossibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public AfterBurnerImpossibleException(String message) {
        super(message);
    }

    public AfterBurnerImpossibleException(Throwable cause) {
        super(cause);
    }

}
