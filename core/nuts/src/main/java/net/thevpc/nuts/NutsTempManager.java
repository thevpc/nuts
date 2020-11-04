package net.thevpc.nuts;

import java.nio.file.Path;

public interface NutsTempManager {

    /**
     * create temp file in the workspace's temp folder
     *
     * @param name file name
     * @return new file path
     */
    Path createTempFile(String name);

    /**
     * create temp file in the repository's temp folder
     *
     * @param name       file name
     * @param repository repository
     * @return new file path
     */
    Path createTempFile(String name, NutsRepository repository);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return new folder path
     */
    Path createTempFolder(String name);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name       folder name
     * @param repository repository
     * @return new folder path
     */
    Path createTempFolder(String name, NutsRepository repository);
}
