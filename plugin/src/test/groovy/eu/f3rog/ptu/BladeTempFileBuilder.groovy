package eu.f3rog.ptu


public final class BladeTempFileBuilder
        extends TempFileBuilder {

    public enum FileType {
        JSON("json"), YAML("yaml")

        private final String mExtension

        FileType(final String extension) {
            mExtension = extension
        }

        String getExtension() {
            return mExtension
        }
    }

    private final List<String> mModules
    private final FileType mType

    public BladeTempFileBuilder(final FileType type, final List<String> modules) {
        super("blade." + type.getExtension())
        mType = type
        mModules = modules
    }

    @Override
    public String getBody() {
        switch (mType) {
            case FileType.JSON:
                final String modules = mModules.collect({ "\"$it\"" }).join(", ")
                return """{
    "debug": false,
    "modules": [
        ${modules}
    ]
}
                """

            case FileType.YAML:
                String body = "debug: false"
                body += "\nmodules:"
                for (final String module : mModules) {
                    body += "\n  - ${module}"
                }
                return body

            default:
                throw new AssertionError("Unsupported file type!")
        }
    }
}
