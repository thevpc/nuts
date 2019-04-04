package net.vpc.toolbox.mysql.remote;

import net.vpc.app.nuts.NutsCommandExecBuilder;
import net.vpc.app.nuts.NutsCommandStringFormatterAdapter;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.ssh.SShConnection;
import net.vpc.common.ssh.SshAddress;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlDatabaseConfig;
import net.vpc.toolbox.mysql.local.LocalMysql;
import net.vpc.toolbox.mysql.local.LocalMysqlConfigService;
import net.vpc.toolbox.mysql.local.LocalMysqlDatabaseConfigService;

import java.io.PrintStream;
import java.util.List;

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
        context.out().printf("==[%s]== db config removed.\n", name);
        return this;

    }

    public String getName() {
        return name;
    }

    public void write(PrintStream out) {
        context.getWorkspace().io().writeJson(getConfig(), out, true);
    }

    public int pull() {
        return 1;
    }

    public int push() {
        LocalMysql ms = new LocalMysql(context);
        LocalMysqlConfigService loc = ms.loadOrCreateMysqlConfig(getConfig().getLocalInstance());
        String localDatabase = getConfig().getLocalDatabase();
        if (StringUtils.isEmpty(localDatabase)) {
            throw new NutsExecutionException("Missing local database name",2);
        }
        LocalMysqlDatabaseConfigService.ArchiveResult archiveResult = loc.getDatabase(localDatabase).archive(null);
        if (archiveResult.execResult != 0) {
            return archiveResult.execResult;
        }
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        String remoteTempPath = cconfig.getRemoteTempPath();
        String server = cconfig.getServer();
        if (StringUtils.isEmpty(server)) {
            server = "ssh://localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        if (StringUtils.isEmpty(remoteTempPath)) {
            String home = null;
            try (SShConnection c = new SShConnection(new SshAddress(server))
                 .addListener(SShConnection.LOGGER)
            ) {
                if (c.grabOutputString()
                        .exec("echo","$HOME") == 0) {
                    home = c.getOutputString().trim();
                } else {
                    throw new NutsExecutionException("Unable to detect user remote home : " + c.getOutputString().trim(),2);
                }
            }
            remoteTempPath = home + "/tmp";
        }

        String remoteFilePath = (IOUtils.concatPath('/', remoteTempPath, FileUtils.getFileName(archiveResult.path)));
        String remoteFullFilePath = new SshAddress(server).getPath(remoteFilePath).getPath();

        context.out().printf("==[%s]== copy %s to %s\n", name, archiveResult.path, remoteFullFilePath);
        context.getWorkspace().
                createExecBuilder()
                .setCommand(
                        "nsh",
                        "cp",
                        "--no-color",
                        archiveResult.path,
                        remoteFullFilePath
                ).setSession(context.getSession())
                .setRedirectErrorStream()
                .grabOutputString()
                .setFailFast()
                .exec();
        context.out().printf("==[%s]== remote restore %s\n", name, remoteFilePath);
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:mysql",
                "restore",
                "--instance",
                client.getName(),
                "--db",
                name,
                remoteFilePath
        );
        context.out().printf("==[%s]== delete %s\n", name, remoteFilePath);
        return execRemoteNuts(
                "nsh",
                "rm",
                remoteFilePath
        );
    }


    public int execRemoteNuts(List<String> cmd) {
        return execRemoteNuts(cmd.toArray(new String[0]));
    }

    public int execRemoteNuts(String... cmd) {
        NutsCommandExecBuilder b = context.getWorkspace().createExecBuilder()
                .setSession(context.getSession());
        b.addCommand("nsh", "ssh");
        b.addCommand("--nuts");
        b.addCommand(this.config.getServer());
        b.addCommand(cmd);
        context.out().printf("[[EXEC]] %s\n", b.getCommandString(new NutsCommandStringFormatterAdapter() {
            @Override
            public String replaceEnvValue(String envName, String envValue) {
                if (
                        envName.toLowerCase().contains("password")
                                || envName.toLowerCase().contains("pwd")
                ) {
                    return "****";
                }
                return null;
            }
        }));
        b.setRedirectErrorStream()
                .setFailFast();
        return b.exec().getResult();

    }

}
