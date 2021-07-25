package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.string.StringBuilder2;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class DefaultTempAction implements NutsTempAction {
    private NutsWorkspace ws;
    private String repositoryId;
    private NutsSession session;

    public DefaultTempAction(NutsWorkspace ws) {
        this.ws = ws;
    }


    @Override
    public String getRepositoryId() {
        return repositoryId;
    }

    @Override
    public NutsTempAction setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsTempAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public String createTempFile(String name) {
        return createTempFile(name, false);
    }

    @Override
    public String createTempFolder(String name) {
        return createTempFile(name, true);
    }

    public String createTempFile(String name, boolean folder) {
        File rootFolder = null;
        NutsRepository repositoryById = null;
        if (repositoryId == null) {
            rootFolder = Paths.get(ws.locations().setSession(getSession()).getStoreLocation(NutsStoreLocation.TEMP)).toFile();
        } else {
            repositoryById = ws.repos().setSession(session).getRepository(repositoryId);
            rootFolder = Paths.get(repositoryById.config().setSession(session).getStoreLocation(NutsStoreLocation.TEMP)).toFile();
        }
        NutsId appId = session.getAppId();
        if (appId == null) {
            appId = session.getWorkspace().getRuntimeId();
        }
        if (appId != null) {
            rootFolder = new File(
                    rootFolder,
                    NutsConstants.Folders.ID + File.separator
                            + ws.locations().setSession(session).getDefaultIdBasedir(appId)
            );
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
                    temp = File.createTempFile(prefix.toString(), ext.toString(), rootFolder);
                    if (temp.delete() && temp.mkdir()) {
                        return (temp.toPath()).toString();
                    }
                } catch (IOException ex) {
                    //
                }
            }
            throw new NutsIOException(session, NutsMessage.cstyle("could not create temp directory: %s*%s", rootFolder + File.separator + prefix, ext));
        } else {
            try {
                return File.createTempFile(prefix.toString(), ext.toString(), rootFolder).toPath().toString();
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
    }

}
