package net.thevpc.nuts.toolbox.ndb.nmysql.remote;

import java.io.File;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlService;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.LocalMysqlDatabaseConfigService;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.toolbox.ndb.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.MysqlUtils;

public class RemoteMysqlDatabaseConfigService {

    private final RemoteMysqlDatabaseConfig config;
    private final NutsApplicationContext context;
    private final RemoteMysqlConfigService client;
    private final String name;

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
        context.getSession().out().printf("%s db config removed.%n", getBracketsPrefix(name));
        return this;

    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return getName() + "@" + client.getName();
    }

    public void write(PrintStream out) {
        NutsSession session = context.getSession();
        NutsElements.of(session).json().setValue(getConfig())
                .setNtf(false).print(out);
    }

    public String pull(String localPath, boolean restore, boolean deleteRemote) {
        CachedMapFile lastRun = new CachedMapFile(context, "pull-" + getName());
        NutsSession session = context.getSession();
        if (lastRun.exists()) {
            if (!session.getTerminal().ask()
                    .resetLine()
                    .forBoolean(
                            NutsMessage.ofPlain("a previous pull has failed. would you like to resume (yes) or ignore and re-run the pull (no).")
                    )
                    .getBooleanValue()
            ) {
                lastRun.reset();
            }
        }
        NMySqlService ms = new NMySqlService(context);
        AtName locName = new AtName(getConfig().getLocalName());
        LocalMysqlDatabaseConfigService loc = ms.loadLocalMysqlConfig(locName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR)
                .getDatabase(locName.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        if (session.isPlainTrace()) {
            session.out().printf("%s remote restore%n", getBracketsPrefix(name));
        }
        String remoteTempPath = null;
        if (lastRun.get("remoteTempPath") != null) {
            remoteTempPath = lastRun.get("remoteTempPath");
        } else {
            remoteTempPath = execRemoteNuts(
                    "net.thevpc.nuts.toolbox:nmysql",
                    "backup",
                    "--name",
                    config.getRemoteName(),
                    ""
            );
            lastRun.put("remoteTempPath", remoteTempPath);
        }
        //TODO: workaround, must fix me later
        int t = remoteTempPath.indexOf('{');
        if (t > 0) {
            remoteTempPath = remoteTempPath.substring(t);
        }
        NutsElements elem = NutsElements.of(session);
        Map<String, Object> resMap = elem.parse(remoteTempPath.getBytes(), Map.class);
        String ppath = (String) resMap.get("path");

        if (NutsBlankable.isBlank(localPath)) {
            localPath = context.getVarFolder()
                    .resolve("pull-backups")
                    .resolve(client.getName() + "-" + getName())
                    .resolve(/*MysqlUtils.newDateString()+"-"+*/Paths.get(ppath).getFileName().toString())
                    .toString();
        }
        NutsPath remoteFullFilePath = NutsPath.of(prepareSshServer(cconfig.getServer()) + "/" + ppath, session);
        NutsTexts text = NutsTexts.of(session);
        if (session.isPlainTrace()) {
            session.out().printf("%s copy '%s' to '%s'%n", getBracketsPrefix(name),
                    text.ofStyled(remoteFullFilePath.toString(), NutsTextStyle.path()),
                    text.ofStyled(localPath, NutsTextStyle.path())
            );
        }
        if (lastRun.get("localPath") != null) {
            String s = lastRun.get("localPath");
            NutsCp.of(session).from(NutsPath.of(s, session)).to(NutsPath.of(localPath, session)).run();
        } else {
            if (Paths.get(localPath).getParent() != null) {
                try {
                    Files.createDirectories(Paths.get(localPath).getParent());
                } catch (IOException e) {
                    throw new NutsIOException(session, e);
                }
            }
            context.getSession().exec().setExecutionType(NutsExecutionType.EMBEDDED)
                    .setSession(session.copy())
                    .addCommand("nsh",
                            "--bot",
                            "-c",
                            "cp",
                            remoteFullFilePath.toString(), localPath)
                    .setRedirectErrorStream(true)
                    .grabOutputString()
                    .setFailFast(true)
                    .run()
                    .getOutputString();

            lastRun.put("localPath", localPath);
        }
        if (lastRun.get("restored") != null) {
            String s = lastRun.get("restored");
        } else {
            loc.restore(localPath);
            lastRun.put("restored", "true");
        }

        if (deleteRemote) {
            if (session.isPlainTrace()) {
                session.out().printf("%s delete %s%n", getBracketsPrefix(name),
                        remoteFullFilePath);
            }
            if (!lastRun.is("deleted")) {
                execRemoteNuts(
                        "nsh",
                        "-c",
                        "rm",
                        remoteFullFilePath.getLocation()
                );
                lastRun.put("deleted", "true");
            }
        }
        lastRun.dispose();
        return localPath;
    }

    public void push(String localPath, boolean backup) {
        NMySqlService ms = new NMySqlService(context);
        AtName locName = new AtName(getConfig().getLocalName());
        LocalMysqlDatabaseConfigService loc = ms.loadLocalMysqlConfig(locName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR)
                .getDatabase(locName.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        NutsSession session = context.getSession();
        if (backup) {
            localPath = loc.backup(localPath).path;
        } else {
            if (NutsBlankable.isBlank(localPath)) {
                throw new NutsExecutionException(session, NutsMessage.ofPlain("missing local path"), 2);
            }
        }
        if (!new File(localPath).isFile()) {
            throw new NutsExecutionException(session, NutsMessage.ofCstyle("invalid local path %s", localPath), 2);
        }
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        String remoteTempPath = null;
        final String searchResultString = execRemoteNuts("search --!color --json net.thevpc.nuts.toolbox:nmysql --display temp-folder --installed --first");
        List<Map> result = NutsElements.of(session).json().parse(new StringReader(searchResultString), List.class);
        if (result.isEmpty()) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("Mysql is not installed on the remote machine"));
        }
        remoteTempPath = (String) result.get(0).get("temp-folder");

        String remoteFilePath = "/" + remoteTempPath + "-" + MysqlUtils.newDateString() + "-" + MysqlUtils.getFileName(localPath);
        NutsPath remoteFullFilePath = NutsPath.of(prepareSshServer(cconfig.getServer()) + "/" + remoteFilePath, session);
        NutsTexts text = NutsTexts.of(session);
        if (session.isPlainTrace()) {
            session.out().printf("%s copy %s to %s%n", getBracketsPrefix(name),
                    text.ofStyled(localPath, NutsTextStyle.path()),
                    remoteFullFilePath
            );
        }
        session.exec()
                .addCommand(
                        "nsh",
                        "--bot",
                        "cp",
                        localPath,
                        remoteFullFilePath.getLocation()
                ).setSession(session)
                .setRedirectErrorStream(true)
                .grabOutputString()
                .setFailFast(true)
                .run();
        if (session.isPlainTrace()) {
            session.out().printf("%s remote restore %s%n",
                    getBracketsPrefix(name),
                    remoteFullFilePath
            );
        }
        execRemoteNuts(
                "net.thevpc.nuts.toolbox:nmysql",
                "restore",
                "--name",
                config.getRemoteName(),
                remoteFilePath
        );
        if (session.isPlainTrace()) {
            session.out().printf("%s delete %s%n",
                    getBracketsPrefix(name), remoteFilePath);
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
        NutsSession session = context.getSession();
        NutsExecCommand b = session.exec()
                .setSession(session.copy());
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
            b.addCommand("/home/" + System.getProperty("user.name") + "/bin/nuts");
            b.addCommand("-b");
            b.addCommand("-y");
            b.addCommand("--trace=false");
            b.addCommand("--bot");
            b.addCommand("--json");
            b.addCommand(cmd);
        }
        if (session.isPlainTrace()) {
            NutsString ff = b.formatter()
                    .setEnvReplacer(envEntry -> {
                        if (envEntry.getName().toLowerCase().contains("password")
                                || envEntry.getName().toLowerCase().contains("pwd")) {
                            return "****";
                        }
                        return null;
                    })
                    .format();
            session.out().printf("[exec] %s%n", ff);
        }
        b.setRedirectErrorStream(true)
                .grabOutputString()
                .setFailFast(true);
        return b.run().getOutputString();
    }

    private String prepareSshServer(String server) {
        if (NutsBlankable.isBlank(server)) {
            server = "ssh://localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        return server;
    }

    public NutsString getBracketsPrefix(String str) {
        return NutsTexts.of(context.getSession()).ofBuilder()
                .append("[")
                .append(str, NutsTextStyle.primary5())
                .append("]");
    }

}
