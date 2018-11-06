package net.vpc.toolbox.tomcat.client.config;

public class TomcatClientAppConfig {
    private String path;
    private String version;


    public String getPath() {
        return path;
    }

    public void setPath(String appPath) {
        this.path = appPath;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String appVersion) {
        this.version = appVersion;
    }

}
