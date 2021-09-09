package net.thevpc.nuts.toolbox.ntomcat.remote.config;

import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;

import java.util.HashMap;
import java.util.Map;

public class RemoteTomcatConfig {

    private String server;
    private String remoteTempPath;
    private String remoteName;
    private Map<String, RemoteTomcatAppConfig> apps = new HashMap<>();

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getRemoteTempPath() {
        return remoteTempPath;
    }

    public void setRemoteTempPath(String remoteTempPath) {
        this.remoteTempPath = remoteTempPath;
    }

    public Map<String, RemoteTomcatAppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, RemoteTomcatAppConfig> apps) {
        this.apps = apps;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public RemoteTomcatConfig setRemoteName(String remoteName) {
        this.remoteName = remoteName;
        return this;
    }

    @Override
    public String toString() {
        return "{"
                + "server=" + TomcatUtils.toJsonString(server)
                + ", remoteTempPath=" + TomcatUtils.toJsonString(remoteTempPath)
                + ", remoteName=" + TomcatUtils.toJsonString(remoteName)
                + ", apps=" + TomcatUtils.toJsonString(apps)
                + '}';
    }
}
