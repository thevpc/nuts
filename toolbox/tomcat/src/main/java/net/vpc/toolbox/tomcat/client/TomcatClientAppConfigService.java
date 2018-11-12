package net.vpc.toolbox.tomcat.client;

import net.vpc.app.nuts.NutsCommandExecBuilder;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.tomcat.client.config.TomcatClientAppConfig;
import net.vpc.toolbox.tomcat.client.config.TomcatClientConfig;
import net.vpc.toolbox.tomcat.util.NutsContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

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
        String remoteFilePath = IOUtils.concatPath('/', cconfig.getRemoteTempPath() + "test.war");
        String serverPassword = TomcatUtils.isEmpty(cconfig.getServerPassword()) ? "" : cconfig.getServerPassword();
        String serverCertificate = TomcatUtils.isEmpty(cconfig.getServerCertificateFile()) ? "" : cconfig.getServerCertificateFile();
        context.ws.
                createExecBuilder()
                .setCommand(
                        "nsh",
                        "cp",
                        "--password",
                        serverPassword,
                        "--cert",
                        serverCertificate,
                        this.config.getPath(),
                        "ssh://" + IOUtils.concatPath('/', cconfig.getServer() ,remoteFilePath)
                ).setSession(context.session)
                .exec();
        String v=config.getVersionCommand();
        if(StringUtils.isEmpty(v)){
            v="nsh file-version %file";
        }
        List<String> cmd = Arrays.asList(StringUtils.parseCommandline(v));
        boolean fileAdded=false;
        for (int i = 0; i < cmd.size(); i++) {
            if("%file".equals(cmd.get(i))){
                cmd.set(i,config.getPath());
                fileAdded=true;
            }
        }
        if(!fileAdded){
            cmd.add(config.getPath());
        }
        NutsCommandExecBuilder s = context.ws
                .createExecBuilder()
                .setOutAndErrStringBuffer()
                .setCommand(cmd).exec();
        if(s.getResult()==0) {
            client.execRemoteNuts(
                    "net.vpc.app.nuts.toolbox:tomcat",
                    "--install",
                    "--instance",
                    client.getName(),
                    "--app",
                    name,
                    "--version",
                    s.getOutString(),
                    remoteFilePath
            );
            client.execRemoteNuts(
                    "nsh",
                    "-rm",
                    remoteFilePath
            );
        }
    }

    public TomcatClientAppConfig getConfig() {
        return config;
    }

    public TomcatClientAppConfigService remove() {
        client.getConfig().getApps().remove(name);
        context.out.printf("==[%s]== app removed.\n",name);
        return this;

    }

    public String getName() {
        return name;
    }

    public void write(PrintStream out) {
        TomcatUtils.writeJson(out, getConfig(), context.ws);
    }
}
