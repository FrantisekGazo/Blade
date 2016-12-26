package eu.f3rog.ptu

import org.junit.rules.TemporaryFolder


public final class TempProjectFolder
        extends TemporaryFolder {

    private final Set<String> mSubFolderPaths

    public TempProjectFolder() {
        mSubFolderPaths = new HashSet<>()
    }

    private void checkFolder(final String fileName) {
        int lastSeparator = fileName.lastIndexOf(File.separator)

        // if file is not in a folder => do nothing
        if (lastSeparator == -1) {
            return
        }

        final String folderPath = fileName.substring(0, lastSeparator)

        if (!mSubFolderPaths.contains(folderPath)) {
            mSubFolderPaths.add(folderPath)
            final String[] folders = folderPath.split(File.separator)
            this.newFolder(folders)
        }
    }

    public TempProjectFolder addFile(final String name, final String body) {
        checkFolder(name)

        final File file = this.newFile(name)
        file << body

        System.out.println("Adding new file: " + name)
        System.out.println(body)
        System.out.println("---------------------------------------------------")

        return this
    }

    public TempProjectFolder addFile(final TempFileBuilder fileBuilder) {
        final String name = fileBuilder.getName()
        final String body = fileBuilder.getBody()
        final TempFileBuilder[] relatedFileBuilders = fileBuilder.getRelatedFiles()
        for (final TempFileBuilder relatedFileBuilder : relatedFileBuilders) {
            this.addFile(relatedFileBuilder)
        }
        return this.addFile(name, body)
    }

    public TempProjectFolder addGradleFile(final GradleConfig gradleConfig) {
        return this.addFile(new GradleTempFileBuilder(gradleConfig))
    }

    public TempProjectFolder addBladeFile(final BladeTempFileBuilder.FileType type, final List<String> bladeModules) {
        return this.addFile(new BladeTempFileBuilder(type, bladeModules))
    }
}
