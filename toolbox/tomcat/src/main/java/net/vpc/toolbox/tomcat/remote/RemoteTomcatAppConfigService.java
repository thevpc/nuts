package net.vpc.toolbox.tomcat.remote;

import net.vpc.app.nuts.NutsCommandExecBuilder;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.ssh.SshPath;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.tomcat.remote.config.RemoteTomcatAppConfig;
import net.vpc.toolbox.tomcat.remote.config.RemoteTomcatConfig;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class RemoteTomcatAppConfigService extends RemoteTomcatServiceBase{
    private RemoteTomcatAppConfig config;
    private NutsApplicationContext context;
    private RemoteTomcatConfigService client;
    private String name;

    public RemoteTomcatAppConfigService(String name, RemoteTomcatAppConfig config, RemoteTomcatConfigService client) {
        this.config = config;
        this.client = client;
        this.context = client.context;
        this.name = name;
    }

    public void install() {
        RemoteTomcatConfig cconfig = client.getConfig();
        String localWarPath = this.config.getPath();
        if (!new File(localWarPath).exists()) {
            throw new NutsExecutionException("Missing source war file " + localWarPath,2);
        }
        String remoteTempPath = cconfig.getRemoteTempPath();
        if (StringUtils.isEmpty(remoteTempPath)) {
            remoteTempPath = "/tmp";
        }
        String remoteFilePath = IOUtils.concatPath('/', remoteTempPath + "/" + FileUtils.getFileName(localWarPath));
        String server = cconfig.getServer();
        if (StringUtils.isEmpty(server)) {
            server = "localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        SshPath srvp = new SshPath(server);
        context.getWorkspace().
                createExecBuilder()
                .setCommand(
                        "nsh",
                        "cp",
                        "--verbose",
                        "--mkdir",
                        localWarPath,
                        srvp.setPath(remoteFilePath).toString()
                ).setSession(context.getSession())
                .exec();
        String v = config.getVersionCommand();
        if (StringUtils.isEmpty(v)) {
            v = "nsh file-version --no-colors %file";
        }
        List<String> cmd = Arrays.asList(StringUtils.parseCommandline(v));
        boolean fileAdded = false;
        for (int i = 0; i < cmd.size(); i++) {
            if ("%file".equals(cmd.get(i))) {
                cmd.set(i, config.getPath());
                fileAdded = true;
            }
        }
        if (!fileAdded) {
            cmd.add(config.getPath());
        }
        NutsCommandExecBuilder s = context.getWorkspace()
                .createExecBuilder()
                .setRedirectErrorStream()
                .grabOutputString()
                .setCommand(cmd).exec();
        if (s.getResult() == 0) {
            client.execRemoteNuts(
                    "net.vpc.app.nuts.toolbox:tomcat",
                    "install",
                    "--instance",
                    client.getRemoteInstanceName(),
                    "--app",
                    name,
                    "--version",
                    s.getOutputString().trim(),
                    "--file",
                    remoteFilePath
            );
            client.execRemoteNuts(
                    "nsh",
                    "rm",
                    remoteFilePath
            );
        } else {
            throw new NutsExecutionException("Unable to detect file version of " + localWarPath + ".\n" + s.getOutputString(),2);
        }
    }

    public void deploy(String version) {
        client.execRemoteNuts(
                "net.vpc.app.nuts.toolbox:tomcat",
                "deploy",
                "--instance",
                client.getRemoteInstanceName(),
                "--app",
                name,
                "--version",
                StringUtils.trim(version)
        );
    }

    public RemoteTomcatAppConfig getConfig() {
        return config;
    }

    public RemoteTomcatAppConfigService remove() {
        client.getConfig().getApps().remove(name);
        context.out().printf("==[%s]== app removed.\n", name);
        return this;

    }

    public String getName() {
        return name;
    }

    public RemoteTomcatAppConfigService write(PrintStream out) {
        TomcatUtils.writeJson(out, getConfig(), context.getWorkspace());
        return this;
    }

    public RemoteTomcatConfigService getTomcat() {
        return client;
    }
}
