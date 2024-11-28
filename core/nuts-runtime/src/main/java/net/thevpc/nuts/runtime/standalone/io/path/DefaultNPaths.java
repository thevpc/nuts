package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.FilePath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.io.NPaths;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNPaths implements NPaths {
    private final NWorkspace workspace;

    public DefaultNPaths(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NPath createPath(String path) {
        return createPath(path, null);
    }

    @Override
    public NPath createPath(File path) {
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path.toPath(), workspace));
    }

    @Override
    public NPath createPath(Path path) {
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path, workspace));
    }

    @Override
    public NPath createPath(URL path) {
        if (path == null) {
            return null;
        }
        return createPath(new URLPath(path, workspace));
    }

    @Override
    public NPath createPath(String path, ClassLoader classLoader) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        NPath p = getModel().resolve(path, classLoader);
        if (p == null) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve path from %s", path));
        }
        return p;
    }

    @Override
    public NPath createPath(NPathSPI path) {
        if (path == null) {
            return null;
        }
        return new NPathFromSPI(workspace, path);
    }

    @Override
    public NPaths addPathFactory(NPathFactorySPI pathFactory) {
        getModel().addPathFactory(pathFactory);
        return this;
    }

    @Override
    public NPaths removePathFactory(NPathFactorySPI pathFactory) {
        getModel().removePathFactory(pathFactory);
        return this;
    }

    private DefaultNWorkspaceConfigModel getModel() {
        return ((DefaultNConfigs) NConfigs.of()).getModel();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
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


    public NPath ofTempRepositoryFile(String name, NRepository repository) {
        return createAnyTempFile(name, false, repository);
    }

    @Override
    public NPath ofTempRepositoryFolder(String name, NRepository repository) {
        return createAnyTempFile(name, true, repository);
    }

    @Override
    public NPath ofTempRepositoryFile(NRepository repository) {
        return createAnyTempFile(null, false, repository);
    }

    @Override
    public NPath ofTempRepositoryFolder(NRepository repository) {
        return createAnyTempFile(null, true, repository);
    }


    public NPath createAnyTempFile(String name, boolean folder, NRepository repositoryId) {
        NPath rootFolder = null;
        NRepository repositoryById = null;
        NSession session = workspace.currentSession();
        if (repositoryId == null) {
            rootFolder = NLocations.of().getStoreLocation(NStoreType.TEMP);
        } else {
            repositoryById = repositoryId;
            rootFolder = repositoryById.config().getStoreLocation(NStoreType.TEMP);
        }
        NId appId = NApp.of().getId().orElseGet(()->session.getWorkspace().getRuntimeId());
        if (appId != null) {
            rootFolder = rootFolder.resolve(NConstants.Folders.ID).resolve(NLocations.of().getDefaultIdBasedir(appId));
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
                        return NPath.of(temp.toPath())
                                .setUserTemporary(true);
                    }
                } catch (IOException ex) {
                    //
                }
            }
            throw new NIOException(NMsg.ofC("could not create temp directory: %s*%s", rootFolder + File.separator + prefix, ext));
        } else {
            try {
                return NPath.of(File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().get()).toPath())
                        .setUserTemporary(true);
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
    }

}
