package net.vpc.toolbox.mysql.remote;

import java.io.File;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.ssh.SshAddress;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlDatabaseConfig;
import net.vpc.toolbox.mysql.local.LocalMysql;
import net.vpc.toolbox.mysql.local.LocalMysqlDatabaseConfigService;

import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsCommandLineFormat;
import net.vpc.toolbox.mysql.util.MysqlUtils;

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
        context.session().out().printf("==[%s]== db config removed.%n", name);
        return this;

    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return getName() + "@" + client.getName();
    }

    public void write(PrintStream out) {
        context.getWorkspace().format().json().set(getConfig()).print(out);
    }

    public String pull(String localPath, boolean restore, boolean deleteRemote) {
        LocalMysql ms = new LocalMysql(context);
        LocalMysqlDatabaseConfigService loc = ms.loadDatabaseOrError(getConfig().getLocalName());
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        if (StringUtils.isBlank(localPath)) {
            localPath = context.getVarFolder().resolve(client.getName() + "-" + getName() + "-" + MysqlUtils.newDateString()).toString();
        }
        if (context.session().isPlainTrace()) {
            context.session().out().printf("==[%s]== remote restore%n", name);
        }
        String remoteTempPath = execRemoteNuts(
                "net.vpc.app.nuts.toolbox:mysql",
                "restore",
                "--name",
                config.getRemoteName(),
                ""
        );
        String remoteFullFilePath = new SshAddress(prepareSshServer(cconfig.getServer())).getPath(remoteTempPath).getPath();
        if (context.session().isPlainTrace()) {
            context.session().out().printf("==[%s]== copy '%s' to '%s'%n", name, remoteFullFilePath, localPath);
        }
        context.getWorkspace().exec()
                .command("nsh",
                        "cp",
                        "--no-color",
                        remoteFullFilePath, localPath).setSession(context.getSession())
                .redirectErrorStream()
                .grabOutputString()
                .failFast()
                .run()
                .getOutputString();
        loc.restore(localPath);
        if (deleteRemote) {
            if (context.session().isPlainTrace()) {
                context.session().out().printf("==[%s]== delete %s%n", name, remoteFullFilePath);
            }
            execRemoteNuts(
                    "nsh",
                    "rm",
                    remoteFullFilePath
            );
        }
        return localPath;
    }

    public void push(String localPath, boolean backup) {
        LocalMysql ms = new LocalMysql(context);
        LocalMysqlDatabaseConfigService loc = ms.loadDatabaseOrError(getConfig().getLocalName());
        if (backup) {
            localPath = loc.backup(localPath).path;
        } else {
            if (StringUtils.isBlank(localPath)) {
                throw new NutsExecutionException(context.getWorkspace(), "Missing local path", 2);
            }
        }
        if (!new File(localPath).isFile()) {
            throw new NutsExecutionException(context.getWorkspace(), "Invalid local path " + localPath, 2);
        }
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        String remoteTempPath = null;
        final SshAddress sshAddress = new SshAddress(prepareSshServer(cconfig.getServer()));
        final String searchResultString = execRemoteNuts("search --no-color --json net.vpc.app.nuts.toolbox:mysql --display temp-folder --installed --first");
        List<Map> result = this.context.getWorkspace().format().json().read(new StringReader(searchResultString), List.class);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Mysql is not installed on the remote machine");
        }
        remoteTempPath = (String) result.get(0).get("temp-folder");

        String remoteFilePath = IOUtils.concatPath('/', remoteTempPath, "-" + MysqlUtils.newDateString() + "-" + FileUtils.getFileName(localPath));
        String remoteFullFilePath = sshAddress.getPath(remoteFilePath).getPath();

        if (context.session().isPlainTrace()) {
            context.session().out().printf("==[%s]== copy %s to %s%n", name, localPath, remoteFullFilePath);
        }
        context.getWorkspace().exec()
                .command(
                        "nsh",
                        "cp",
                        "--no-color",
                        localPath,
                        remoteFullFilePath
                ).setSession(context.getSession())
                .redirectErrorStream()
                .grabOutputString()
                .failFast()
                .run();
        if (context.session().isPlainTrace()) {
            context.session().out().printf("==[%s]== remote restore %s%n", name, remoteFilePath);
        }
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:mysql",
                "restore",
                "--name",
                config.getRemoteName(),
                remoteFilePath
        );
        if (context.session().isPlainTrace()) {
            context.session().out().printf("==[%s]== delete %s%n", name, remoteFilePath);
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
                .setSession(context.getSession());
        b.addCommand("nsh", "-c", "ssh");
        b.addCommand("--nuts");
        b.addCommand(this.config.getServer());
        b.addCommand(cmd);
        if (context.session().isPlainTrace()) {
            context.session().out().printf("[[EXEC]] %s%n", b.setCommandLineFormat(new NutsCommandLineFormat() {
                @Override
                public String replaceEnvValue(String envName, String envValue) {
                    if (envName.toLowerCase().contains("password")
                            || envName.toLowerCase().contains("pwd")) {
                        return "****";
                    }
                    return null;
                }
            }).getCommandString());
        }
        b.redirectErrorStream()
                .grabOutputString()
                .failFast();
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
