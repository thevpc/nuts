package net.thevpc.nuts.toolbox.nbackup;

import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private String localPath;
    private String remoteServer;
    private String remoteUser;
    private List<DecoratedPath> paths = new ArrayList<>();


    public String getLocalPath() {
        return localPath;
    }

    public Config setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

    public String getRemoteServer() {
        return remoteServer;
    }

    public Config setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
        return this;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public Config setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
        return this;
    }

    public List<DecoratedPath> getPaths() {
        return paths;
    }

    public void copyFrom(Config paramConfig) {
        if (NBlankable.isBlank(paramConfig.localPath)) {
            this.localPath = paramConfig.localPath;
        }
        if (NBlankable.isBlank(paramConfig.remoteServer)) {
            this.remoteServer = paramConfig.remoteServer;
        }
        if (NBlankable.isBlank(paramConfig.remoteUser)) {
            this.remoteUser = paramConfig.remoteUser;
        }
        if (paramConfig.paths != null) {
            for (DecoratedPath path : paramConfig.paths) {
                if (NBlankable.isBlank(path)) {
                    if (this.paths == null) {
                        this.paths = new ArrayList<>();
                    }
                    if (!this.paths.contains(path)) {
                        this.paths.add(path);
                    }
                }
            }
        }
    }

}
