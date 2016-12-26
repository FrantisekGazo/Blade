package eu.f3rog.ptu


public abstract class TempFileBuilder {

    private final String mName

    public TempFileBuilder(final String name) {
        mName = name
    }

    public String getName() {
        return mName
    }

    public abstract String getBody()

    public TempFileBuilder[] getRelatedFiles() {
        return []
    }
}
