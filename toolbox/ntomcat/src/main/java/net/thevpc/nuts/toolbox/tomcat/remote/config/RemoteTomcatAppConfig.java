package net.thevpc.nuts.toolbox.tomcat.remote.config;

import net.thevpc.nuts.toolbox.tomcat.util.TomcatUtils;

public class RemoteTomcatAppConfig {

    private String path;
    private String versionCommand;

    public String getPath() {
        return path;
    }

    public void setPath(String appPath) {
        this.path = appPath;
    }

    public String getVersionCommand() {
        return versionCommand;
    }

    public void setVersionCommand(String appVersion) {
        this.versionCommand = appVersion;
    }

    @Override
    public String toString() {
        return "{"
                + "path=" + TomcatUtils.toJsonString(path)
                + ", versionCommand=" + TomcatUtils.toJsonString(versionCommand)
                + '}';
    }
}
