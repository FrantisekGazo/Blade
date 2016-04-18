package eu.f3rog.blade.compiler.parcel.p;

/**
 * Class {@link CallFormat}
 *
 * @author FrantisekGazo
 * @version 2016-04-18
 */
public class CallFormat {

    public enum Arg {
        TARGET_GETTER,
        PARCEL,
        TYPE,
        RAW_TYPE,
        CLASS_LOADER_OR_NULL
    }

    private final String format;
    private final Arg[] args;

    public CallFormat(String format, Arg... args) {
        this.format = format;
        this.args = args;
    }

    public String getFormat() {
        return format;
    }

    public Arg[] getArgs() {
        return args;
    }
}
