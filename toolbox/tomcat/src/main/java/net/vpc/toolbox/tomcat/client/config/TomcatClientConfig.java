package net.vpc.toolbox.tomcat.client.config;


import java.util.HashMap;
import java.util.Map;

public class TomcatClientConfig {
    private String serverInstance;
    private String server;
    private String serverCertificateFile;
    private String serverPassword;
    private String serverTempPath;
    private Map<String,TomcatClientAppConfig> apps=new HashMap<>();

    public String getServerInstance() {
        return serverInstance;
    }

    public void setServerInstance(String serverInstance) {
        this.serverInstance = serverInstance;
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

    public String getServerTempPath() {
        return serverTempPath;
    }

    public void setServerTempPath(String serverTempPath) {
        this.serverTempPath = serverTempPath;
    }

    public Map<String, TomcatClientAppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, TomcatClientAppConfig> apps) {
        this.apps = apps;
    }
}
