package net.vpc.toolbox.tomcat.client.config;


import java.util.HashMap;
import java.util.Map;

public class TomcatClientConfig {
    private String remoteInstance;
    private String server;
    private String serverCertificateFile;
    private String serverPassword;
    private String remoteTempPath;
    private Map<String,TomcatClientAppConfig> apps=new HashMap<>();

    public String getRemoteInstance() {
        return remoteInstance;
    }

    public void setRemoteInstance(String remoteInstance) {
        this.remoteInstance = remoteInstance;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServerCertificateFile() {
        return serverCertificateFile;
    }

    public void setServerCertificateFile(String serverCertificateFile) {
        this.serverCertificateFile = serverCertificateFile;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    public String getRemoteTempPath() {
        return remoteTempPath;
    }

    public void setRemoteTempPath(String remoteTempPath) {
        this.remoteTempPath = remoteTempPath;
    }

    public Map<String, TomcatClientAppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, TomcatClientAppConfig> apps) {
        this.apps = apps;
    }
}
