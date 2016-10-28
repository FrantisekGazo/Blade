package eu.f3rog.javassist.inserts;

/**
 * Represents point in method body, where new code will be inserted.
 *
 * @author FrantisekGazo
 */
public final class InsertionPoint {

    private final InsertionType mType;
    private final String mCall;

    public InsertionPoint(InsertionType type, String call) {
        mType = type;
        mCall = call;
    }

    public InsertionPoint(InsertionType type) {
        this(type, null);
    }

    public InsertionType getType() {
        return mType;
    }

    public String getCall() {
        return mCall;
    }

    @Override
    public String toString() {
        if (mCall != null) {
            return mType + " : " + mCall;
        } else {
            return mCall;
        }
    }
}
