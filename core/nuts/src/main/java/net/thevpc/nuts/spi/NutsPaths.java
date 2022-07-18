package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.util.NutsUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

@NutsComponentScope(NutsComponentScopeType.SESSION)
public interface NutsPaths extends NutsComponent {
    static NutsPaths of(NutsSession session) {
        NutsUtils.requireSession(session);
        return session.extensions().createSupported(NutsPaths.class, true, session);
    }

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @return expanded path
     */
    NutsPath createPath(String path);

    NutsPath createPath(File path);

    NutsPath createPath(Path path);

    NutsPath createPath(URL path);

    NutsPath createPath(String path, ClassLoader classLoader);
    NutsPath createPath(NutsPathSPI path);

    NutsPaths addPathFactory(NutsPathFactory pathFactory);

    NutsPaths removePathFactory(NutsPathFactory pathFactory);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NutsPath createTempFile(String name);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NutsPath createTempFile();

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NutsPath createTempFolder(String name);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    NutsPath createTempFolder();

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NutsPath createRepositoryTempFile(String name,String repository);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NutsPath createRepositoryTempFile(String repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NutsPath createRepositoryTempFolder(String name,String repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    NutsPath createRepositoryTempFolder(String repository);
}
