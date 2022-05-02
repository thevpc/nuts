package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.io.NutsPath;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsPaths extends NutsComponent {
    static NutsPaths of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsPaths.class, true, session);
    }

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @param session session
     * @return expanded path
     */
    NutsPath createPath(String path, NutsSession session);

    NutsPath createPath(File path, NutsSession session);

    NutsPath createPath(Path path, NutsSession session);

    NutsPath createPath(URL path, NutsSession session);

    NutsPath createPath(String path, ClassLoader classLoader, NutsSession session);
    NutsPath createPath(NutsPathSPI path, NutsSession session);

    NutsPaths addPathFactory(NutsPathFactory pathFactory, NutsSession session);

    NutsPaths removePathFactory(NutsPathFactory pathFactory, NutsSession session);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NutsPath createTempFile(String name,NutsSession session);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NutsPath createTempFile(NutsSession session);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NutsPath createTempFolder(String name,NutsSession session);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    NutsPath createTempFolder(NutsSession session);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NutsPath createRepositoryTempFile(String name,String repository,NutsSession session);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NutsPath createRepositoryTempFile(String repository,NutsSession session);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NutsPath createRepositoryTempFolder(String name,String repository,NutsSession session);

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    NutsPath createRepositoryTempFolder(String repository,NutsSession session);
}
