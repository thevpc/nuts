package net.vpc.toolbox.tomcat.client;

import net.vpc.common.io.IOUtils;
import net.vpc.toolbox.tomcat.client.config.TomcatClientAppConfig;
import net.vpc.toolbox.tomcat.client.config.TomcatClientConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

public class TomcatClientAppConfigService {
    private TomcatClientAppConfig config;
    private NutsContext context;
    private TomcatClientConfigService client;
    private String name;

    public TomcatClientAppConfigService(String name,TomcatClientAppConfig config, TomcatClientConfigService client) {
        this.config = config;
        this.client = client;
        this.context = client.context;
        this.name = name;
    }

    public void install(){
        TomcatClientConfig cconfig = client.getConfig();
        String remoteFilePath = IOUtils.concatPath('/', cconfig.getServerTempPath() + "test.war");
        String serverPassword = TomcatUtils.isEmpty(cconfig.getServerPassword()) ? "" : cconfig.getServerPassword();
        String serverCertificate = TomcatUtils.isEmpty(cconfig.getServerCertificateFile()) ? "" : cconfig.getServerCertificateFile();
        context.ws.exec(
                new String[]{
                        "nsh",
                        "cp",
                        "--password",
                        serverPassword,
                        "--cert",
                        serverCertificate,
                        this.config.getPath(),
                        "ssh://" + IOUtils.concatPath('/', cconfig.getServer() ,remoteFilePath),
                }, null,null,context.session
        );
        client.execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "--install",
                "--instance",
                client.getName(),
                "--app",
                name,
                "--version",
                config.getVersion(),
                remoteFilePath
        );
        client.execRemoteNuts(
                "nsh",
                "-rm",
                remoteFilePath
        );
    }

    public TomcatClientAppConfig getConfig() {
        return config;
    }

    public TomcatClientAppConfigService remove() {
        client.getConfig().getApps().remove(name);
        context.out.printf("==[%s]== app removed.\n",name);
        return this;

    }
}
