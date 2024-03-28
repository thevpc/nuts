package net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote;

import java.io.File;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMySqlService;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.LocalMysqlDatabaseConfigService;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.MysqlUtils;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

public class RemoteMysqlDatabaseConfigService {

    private final RemoteMysqlDatabaseConfig config;
    private final NSession session;
    private final RemoteMysqlConfigService client;
    private final String name;

    public RemoteMysqlDatabaseConfigService(String name, RemoteMysqlDatabaseConfig config, RemoteMysqlConfigService client) {
        this.config = config;
        this.client = client;
        this.session = client.session;
        this.name = name;
    }

    public RemoteMysqlDatabaseConfig getConfig() {
        return config;
    }

    public RemoteMysqlDatabaseConfigService remove() {
        client.getConfig().getDatabases().remove(name);
        session.out().println(NMsg.ofC("%s db config removed.", getBracketsPrefix(name)));
        return this;

    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return getName() + "@" + client.getName();
    }

    public void write(PrintStream out) {
        NElements.of(session).json().setValue(getConfig())
                .setNtf(false).print(out);
    }

    public String pull(String localPath, boolean restore, boolean deleteRemote) {
        CachedMapFile lastRun = new CachedMapFile(session, "pull-" + getName());
        if (lastRun.exists()) {
            if (!session.getTerminal().ask()
                    .resetLine()
                    .forBoolean(
                            NMsg.ofPlain("a previous pull has failed. would you like to resume (yes) or ignore and re-run the pull (no).")
                    )
                    .getBooleanValue()
            ) {
                lastRun.reset();
            }
        }
        NMySqlService ms = new NMySqlService(session);
        AtName locName = new AtName(getConfig().getLocalName());
        LocalMysqlDatabaseConfigService loc = ms.loadLocalMysqlConfig(locName.getConfigName(), NOpenMode.OPEN_OR_ERROR)
                .getDatabase(locName.getDatabaseName(), NOpenMode.OPEN_OR_ERROR);
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("%s remote restore", getBracketsPrefix(name)));
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
        NElements elem = NElements.of(session);
        Map<String, Object> resMap = elem.parse(remoteTempPath.getBytes(), Map.class);
        String ppath = (String) resMap.get("path");

        if (NBlankable.isBlank(localPath)) {
            localPath = session.getAppVarFolder()
                    .resolve("pull-backups")
                    .resolve(client.getName() + "-" + getName())
                    .resolve(/*MysqlUtils.newDateString()+"-"+*/Paths.get(ppath).getFileName().toString())
                    .toString();
        }
        NPath remoteFullFilePath = NPath.of(prepareSshServer(cconfig.getServer()) + "/" + ppath, session);
        NTexts text = NTexts.of(session);
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("%s copy '%s' to '%s'", getBracketsPrefix(name),
                    text.ofStyled(remoteFullFilePath.toString(), NTextStyle.path()),
                    text.ofStyled(localPath, NTextStyle.path())
            ));
        }
        if (lastRun.get("localPath") != null) {
            String s = lastRun.get("localPath");
            NCp.of(session).from(NPath.of(s, session)).to(NPath.of(localPath, session)).run();
        } else {
            if (Paths.get(localPath).getParent() != null) {
                try {
                    Files.createDirectories(Paths.get(localPath).getParent());
                } catch (IOException e) {
                    throw new NIOException(session, e);
                }
            }
            NExecCmd.of(session.copy()).setExecutionType(NExecutionType.EMBEDDED)
                    .addCommand("nsh",
                            "--bot",
                            "-c",
                            "cp",
                            remoteFullFilePath.toString(), localPath)
                    .failFast()
                    .run()
                    .getGrabbedAllString();

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
                session.out().println(NMsg.ofC("%s delete %s", getBracketsPrefix(name),
                        remoteFullFilePath));
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
        NMySqlService ms = new NMySqlService(session);
        AtName locName = new AtName(getConfig().getLocalName());
        LocalMysqlDatabaseConfigService loc = ms.loadLocalMysqlConfig(locName.getConfigName(), NOpenMode.OPEN_OR_ERROR)
                .getDatabase(locName.getDatabaseName(), NOpenMode.OPEN_OR_ERROR);
        if (backup) {
            localPath = loc.backup(localPath).path;
        } else {
            if (NBlankable.isBlank(localPath)) {
                throw new NExecutionException(session, NMsg.ofPlain("missing local path"), NExecutionException.ERROR_2);
            }
        }
        if (!new File(localPath).isFile()) {
            throw new NExecutionException(session, NMsg.ofC("invalid local path %s", localPath), NExecutionException.ERROR_2);
        }
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        String remoteTempPath = null;
        final String searchResultString = execRemoteNuts("search --!color --json net.thevpc.nuts.toolbox:nmysql --display temp-folder --installed --first");
        List<Map> result = NElements.of(session).json().parse(new StringReader(searchResultString), List.class);
        if (result.isEmpty()) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("Mysql is not installed on the remote machine"));
        }
        remoteTempPath = (String) result.get(0).get("temp-folder");

        String remoteFilePath = "/" + remoteTempPath + "-" + MysqlUtils.newDateString() + "-" + MysqlUtils.getFileName(localPath);
        NPath remoteFullFilePath = NPath.of(prepareSshServer(cconfig.getServer()) + "/" + remoteFilePath, session);
        NTexts text = NTexts.of(session);
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("%s copy %s to %s", getBracketsPrefix(name),
                    text.ofStyled(localPath, NTextStyle.path()),
                    remoteFullFilePath
            ));
        }
        NExecCmd.of(session)
                .addCommand(
                        "nsh",
                        "--bot",
                        "cp",
                        localPath,
                        remoteFullFilePath.getLocation()
                )
                .grabAll()
                .failFast()
                .run();
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("%s remote restore %s",
                    getBracketsPrefix(name),
                    remoteFullFilePath
            ));
        }
        execRemoteNuts(
                "net.thevpc.nuts.toolbox:nmysql",
                "restore",
                "--name",
                config.getRemoteName(),
                remoteFilePath
        );
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("%s delete %s",
                    getBracketsPrefix(name), remoteFilePath));
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
        NExecCmd b = NExecCmd.of(session.copy());
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
            b.addCommand(NdbUtils.getDefaultUserHome(System.getProperty("user.name")) + "/bin/nuts");
            b.addCommand("-b");
            b.addCommand("-y");
            b.addCommand("--trace=false");
            b.addCommand("--bot");
            b.addCommand("--json");
            b.addCommand(cmd);
        }
        if (session.isPlainTrace()) {
            NString ff = b.formatter()
                    .setEnvReplacer(envEntry -> {
                        if (envEntry.getName().toLowerCase().contains("password")
                                || envEntry.getName().toLowerCase().contains("pwd")) {
                            return "****";
                        }
                        return null;
                    })
                    .format();
            session.out().println(NMsg.ofC("[exec] %s", ff));
        }
        b.grabAll().failFast();
        return b.run().getGrabbedOutString();
    }

    private String prepareSshServer(String server) {
        if (NBlankable.isBlank(server)) {
            server = "ssh://localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        return server;
    }

    public NString getBracketsPrefix(String str) {
        return NTexts.of(session).ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }

}
