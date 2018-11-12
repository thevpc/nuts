package net.vpc.toolbox.tomcat.client.config;

public class TomcatClientAppConfig {
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

}
