package net.thevpc.nuts.runtime.standalone.xtra.tmp;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.StringBuilder2;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.io.IOException;

public class DefaultNutsTmp implements NutsTmp {
    private NutsWorkspace ws;
    private String repositoryId;
    private NutsSession session;

    public DefaultNutsTmp(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String getRepositoryId() {
        return repositoryId;
    }

    @Override
    public NutsTmp setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsTmp setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NutsPath createTempFile(String name) {
        return createTempFile(name, false);
    }

    @Override
    public NutsPath createTempFolder(String name) {
        return createTempFile(name, true);
    }

    @Override
    public NutsPath createTempFile() {
        return createTempFile(null);
    }

    @Override
    public NutsPath createTempFolder() {
        return createTempFolder(null);
    }

    public NutsPath createTempFile(String name, boolean folder) {
        NutsPath rootFolder = null;
        NutsRepository repositoryById = null;
        if (repositoryId == null) {
            rootFolder = getSession().locations().setSession(getSession()).getStoreLocation(NutsStoreLocation.TEMP);
        } else {
            repositoryById = getSession().repos().setSession(getSession()).getRepository(repositoryId);
            rootFolder = repositoryById.config().setSession(getSession()).getStoreLocation(NutsStoreLocation.TEMP);
        }
        NutsId appId = getSession().getAppId();
        if (appId == null) {
            appId = getSession().getWorkspace().getRuntimeId();
        }
        if (appId != null) {
            rootFolder = rootFolder.resolve(NutsConstants.Folders.ID).resolve(getSession().locations().getDefaultIdBasedir(appId));
        }
        if (name == null) {
            name = "";
        }
        rootFolder.mkdirs();
        StringBuilder2 ext = new StringBuilder2(CoreIOUtils.getFileExtension(name, true, true));
        StringBuilder2 prefix = new StringBuilder2((ext.length() > 0) ? name.substring(0, name.length() - ext.length()) : name);
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
                    temp = File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().toFile());
                    if (temp.delete() && temp.mkdir()) {
                        return NutsPath.of(temp.toPath(),session);
                    }
                } catch (IOException ex) {
                    //
                }
            }
            throw new NutsIOException(session, NutsMessage.cstyle("could not create temp directory: %s*%s", rootFolder + File.separator + prefix, ext));
        } else {
            try {
                return NutsPath.of(File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().toFile()).toPath(),session);
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
    }

}
