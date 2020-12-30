package net.thevpc.nuts.toolbox.nmysql.remote;

import java.io.File;

import net.thevpc.common.ssh.SshPath;
import net.thevpc.nuts.*;
import net.thevpc.common.io.FileUtils;
import net.thevpc.common.io.IOUtils;
import net.thevpc.common.ssh.SshAddress;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.toolbox.nmysql.NMySqlService;
import net.thevpc.nuts.toolbox.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.nmysql.local.LocalMysqlDatabaseConfigService;

import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.toolbox.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.nmysql.util.MysqlUtils;

public class RemoteMysqlDatabaseConfigService {

    private RemoteMysqlDatabaseConfig config;
    private NutsApplicationContext context;
    private RemoteMysqlConfigService client;
    private String name;

    public RemoteMysqlDatabaseConfigService(String name, RemoteMysqlDatabaseConfig config, RemoteMysqlConfigService client) {
        this.config = config;
        this.client = client;
        this.context = client.context;
        this.name = name;
    }

    public RemoteMysqlDatabaseConfig getConfig() {
        return config;
    }

    public RemoteMysqlDatabaseConfigService remove() {
        client.getConfig().getDatabases().remove(name);
        context.getSession().out().printf("######[%s]###### db config removed.%n", name);
        return this;

    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return getName() + "@" + client.getName();
    }

    public void write(PrintStream out) {
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(getConfig()).print(out);
    }

    public String pull(String localPath, boolean restore, boolean deleteRemote) {
        NMySqlService ms = new NMySqlService(context);
        AtName locName = new AtName(getConfig().getLocalName());
        LocalMysqlDatabaseConfigService loc = ms.loadLocalMysqlConfig(locName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR)
                .getDatabase(locName.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        if (StringUtils.isBlank(localPath)) {
            localPath = Paths.get(context.getVarFolder()).resolve(client.getName() + "-" + getName() + "-" + MysqlUtils.newDateString()).toString();
        }
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("######[%s]###### remote restore%n", name);
        }

        String remoteTempPath = execRemoteNuts(
                "net.thevpc.nuts.toolbox:nmysql",
                "backup",
                "--name",
                config.getRemoteName(),
                ""
        );
        //TODO: workaround, must fix me later
        int t = remoteTempPath.indexOf('{');
        if(t>0){
            remoteTempPath=remoteTempPath.substring(t);
        }
        Map<String,Object> resMap=context.getWorkspace().formats().element().parse(remoteTempPath.getBytes(),Map.class);
        String ppath=(String)resMap.get("path");

//        String ppath="/home/vpc/enisoinfodb-202012271551.sql.zip";
//        localPath="/home/vpc/.config/nuts/eniso-info/var/id/net/thevpc/nuts/toolbox/nmysql/0.8.1.0/default-enisoinfodb-2020-12-27-161457-685";
        if (StringUtils.isBlank(localPath)) {
//            localPath = context.getVarFolder().resolve(client.getName() + "-" + getName() + "-" + MysqlUtils.newDateString()).toString();
            localPath = Paths.get(context.getVarFolder()).resolve(Paths.get(ppath).getFileName().toString()).toString();
        }
        SshPath remoteFullFilePath = new SshAddress(prepareSshServer(cconfig.getServer())).getPath(ppath);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("######[%s]###### copy '%s' to '%s'%n", name, remoteFullFilePath.toString(), localPath);
        }
        context.getWorkspace().exec().embedded()
                .setSession(context.getSession().copy().setTrace(false))
                .addCommand("nsh",
                        "--bot",
                        "-c",
                        "cp",
                        remoteFullFilePath.toString(), localPath).setSession(context.getSession())
                .setRedirectErrorStream(true)
                .grabOutputString()
                .setFailFast(true)
                .run()
                .getOutputString();
        loc.restore(localPath);
        if (deleteRemote) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("######[%s]###### delete %s%n", name, remoteFullFilePath.toString());
            }
            execRemoteNuts(
                    "nsh",
                    "-c",
                    "rm",
                    remoteFullFilePath.getPath()
            );
        }
        return localPath;
    }

    public void push(String localPath, boolean backup) {
        NMySqlService ms = new NMySqlService(context);
        AtName locName = new AtName(getConfig().getLocalName());
        LocalMysqlDatabaseConfigService loc = ms.loadLocalMysqlConfig(locName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR)
                .getDatabase(locName.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        if (backup) {
            localPath = loc.backup(localPath).path;
        } else {
            if (StringUtils.isBlank(localPath)) {
                throw new NutsExecutionException(context.getWorkspace(), "missing local path", 2);
            }
        }
        if (!new File(localPath).isFile()) {
            throw new NutsExecutionException(context.getWorkspace(), "invalid local path " + localPath, 2);
        }
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        String remoteTempPath = null;
        final SshAddress sshAddress = new SshAddress(prepareSshServer(cconfig.getServer()));
        final String searchResultString = execRemoteNuts("search --no-color --json net.thevpc.nuts.toolbox:nmysql --display temp-folder --installed --first");
        List<Map> result = this.context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(new StringReader(searchResultString), List.class);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Mysql is not installed on the remote machine");
        }
        remoteTempPath = (String) result.get(0).get("temp-folder");

        String remoteFilePath = IOUtils.concatPath('/', remoteTempPath, "-" + MysqlUtils.newDateString() + "-" + FileUtils.getFileName(localPath));
        String remoteFullFilePath = sshAddress.getPath(remoteFilePath).getPath();

        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("######[%s]###### copy %s to %s%n", name, localPath, remoteFullFilePath);
        }
        context.getWorkspace().exec()
                .addCommand(
                        "nsh",
                        "--bot",
                        "cp",
                        localPath,
                        remoteFullFilePath
                ).setSession(context.getSession())
                .setRedirectErrorStream(true)
                .grabOutputString()
                .setFailFast(true)
                .run();
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("######[%s]###### remote restore %s%n", name, remoteFilePath);
        }
        execRemoteNuts(
                "net.thevpc.nuts.toolbox:nmysql",
                "restore",
                "--name",
                config.getRemoteName(),
                remoteFilePath
        );
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("######[%s]###### delete %s%n", name, remoteFilePath);
        }
        execRemoteNuts(
                "nsh",
                "rm",
                remoteFilePath
        );
    }

    public String execRemoteNuts(List<String> cmd) {
        return execRemoteNuts(cmd.toArray(new String[0]));
    }

    public String execRemoteNuts(String... cmd) {
        NutsExecCommand b = context.getWorkspace().exec()
                .setSession(context.getSession().copy().setTrace(false));
        if ("localhost".equals(this.config.getServer())) {
            b.addCommand("nuts");
            b.addCommand("-b");
            b.addCommand("-y");
            b.addCommand("--bot");
            b.addCommand("--trace=false");
            b.addCommand("--json");
            b.addCommand(cmd);
        } else {
            b.addCommand("nsh", "-c", "ssh");
            b.addCommand(this.config.getServer());
            b.addCommand("/home/vpc/bin/nuts");
            b.addCommand("-b");
            b.addCommand("-y");
            b.addCommand("--trace=false");
            b.addCommand("--bot");
            b.addCommand("--json");
            b.addCommand(cmd);
        }
        if (context.getSession().isPlainTrace()) {
            String ff = b.format()
                    .setEnvReplacer(envEntry -> {
                        if (envEntry.getName().toLowerCase().contains("password")
                                || envEntry.getName().toLowerCase().contains("pwd")) {
                            return "****";
                        }
                        return null;
                    })
                    .format();
            context.getSession().out().printf("[[EXEC]] %s%n", ff);
        }
        b.setRedirectErrorStream(true)
                .grabOutputString()
                .setFailFast(true);
        return b.run().getOutputString();
    }

    private String prepareSshServer(String server) {
        if (StringUtils.isBlank(server)) {
            server = "ssh://localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        return server;
    }
}
