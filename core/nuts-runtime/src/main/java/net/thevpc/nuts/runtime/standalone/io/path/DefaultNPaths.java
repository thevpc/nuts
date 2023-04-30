package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.FilePath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NPathFactory;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NStringBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNPaths implements NPaths {
    private final NSession session;

    public DefaultNPaths(NSession session) {
        this.session = session;
    }

    @Override
    public NPath createPath(String path) {
        checkSession(session);
        return createPath(path, null);
    }

    @Override
    public NPath createPath(File path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path.toPath(), session));
    }

    @Override
    public NPath createPath(Path path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path, session));
    }

    @Override
    public NPath createPath(URL path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return createPath(new URLPath(path, session));
    }

    @Override
    public NPath createPath(String path, ClassLoader classLoader) {
        checkSession(session);
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        NPath p = getModel().resolve(path, session, classLoader);
        if (p == null) {
            throw new NIllegalArgumentException(session, NMsg.ofC("unable to resolve path from %s", path));
        }
        return p;
    }

    @Override
    public NPath createPath(NPathSPI path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return new NPathFromSPI(path);
    }

    @Override
    public NPaths addPathFactory(NPathFactory pathFactory) {
        getModel().addPathFactory(pathFactory);
        return this;
    }

    @Override
    public NPaths removePathFactory(NPathFactory pathFactory) {
        getModel().removePathFactory(pathFactory);
        return this;
    }

    private void checkSession(NSession session) {
        NSessionUtils.checkSession(this.session.getWorkspace(), session);
    }

    private DefaultNWorkspaceConfigModel getModel() {
        return ((DefaultNConfigs) NConfigs.of(session)).getModel();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }


    public NPath ofTempFile(String name) {
        return createAnyTempFile(name, false, null);
    }

    @Override
    public NPath ofTempFolder(String name) {
        return createAnyTempFile(name, true, null);
    }

    @Override
    public NPath ofTempFile() {
        return createAnyTempFile(null, false, null);
    }

    @Override
    public NPath ofTempFolder() {
        return createAnyTempFile(null, true, null);
    }


    public NPath ofTempRepositoryFile(String name, String repository) {
        return createAnyTempFile(name, false, repository);
    }

    @Override
    public NPath ofTempRepositoryFolder(String name, String repository) {
        return createAnyTempFile(name, true, repository);
    }

    @Override
    public NPath ofTempRepositoryFile(String repository) {
        return createAnyTempFile(null, false, repository);
    }

    @Override
    public NPath ofTempRepositoryFolder(String repository) {
        return createAnyTempFile(null, true, repository);
    }


    public NPath createAnyTempFile(String name, boolean folder, String repositoryId) {
        NPath rootFolder = null;
        NRepository repositoryById = null;
        if (repositoryId == null) {
            rootFolder = NLocations.of(session).getStoreLocation(NStoreType.TEMP);
        } else {
            repositoryById = NRepositories.of(session).setSession(session).findRepository(repositoryId).get();
            rootFolder = repositoryById.config().setSession(session).getStoreLocation(NStoreType.TEMP);
        }
        NId appId = session.getAppId();
        if (appId == null) {
            appId = session.getWorkspace().getRuntimeId();
        }
        if (appId != null) {
            rootFolder = rootFolder.resolve(NConstants.Folders.ID).resolve(NLocations.of(session).getDefaultIdBasedir(appId));
        }
        if (name == null) {
            name = "";
        }
        rootFolder.mkdirs();
        NStringBuilder ext = new NStringBuilder(CoreIOUtils.getFileExtension(name, false, true));
        NStringBuilder prefix = new NStringBuilder((ext.length() > 0) ? name.substring(0, name.length() - ext.length()) : name);
        if (ext.isEmpty() && prefix.isEmpty()) {
            prefix.append("nuts-");
            if (!folder) {
                ext.append(".tmp");
            }
        } else if (ext.isEmpty()) {
            if (!folder) {
                ext.append("-tmp");
            }
        } else if (prefix.isEmpty()) {
            prefix.append(ext);
            ext.clear();
            ext.append("-tmp");
        }
        if (!prefix.endsWith("-")) {
            prefix.append('-');
        }
        if (prefix.length() < 3) {
            if (prefix.length() < 3) {
                prefix.append('A');
                if (prefix.length() < 3) {
                    prefix.append('B');
                }
            }
        }

        if (folder) {
            for (int i = 0; i < 15; i++) {
                File temp = null;
                try {
                    temp = File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().get());
                    if (temp.delete() && temp.mkdir()) {
                        return NPath.of(temp.toPath(), session)
                                .setUserTemporary(true);
                    }
                } catch (IOException ex) {
                    //
                }
            }
            throw new NIOException(session, NMsg.ofC("could not create temp directory: %s*%s", rootFolder + File.separator + prefix, ext));
        } else {
            try {
                return NPath.of(File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().get()).toPath(), session)
                        .setUserTemporary(true);
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
        }
    }

}
