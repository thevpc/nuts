package net.thevpc.nuts.toolbox.ndb.nmysql.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlDatabaseConfig;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalMysqlDatabaseConfigService {
    private String name;
    private LocalMysqlDatabaseConfig config;
    private LocalMysqlConfigService mysql;
    private NApplicationContext context;

    public LocalMysqlDatabaseConfigService(String name, LocalMysqlDatabaseConfig config, LocalMysqlConfigService mysql) {
        this.name = name;
        this.config = config;
        this.mysql = mysql;
        this.context = mysql.getContext();
    }

    public LocalMysqlDatabaseConfig getConfig() {
        return config;
    }

    public LocalMysqlConfigService getMysql() {
        return mysql;
    }

    public LocalMysqlDatabaseConfigService remove() {
        mysql.getConfig().getDatabases().remove(name);
        context.getSession().out().printf("%s app removed.%n", getBracketsPrefix(getFullName()));
        return this;
    }

    public NString getBracketsPrefix(String str) {
        return NTexts.of(context.getSession()).ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }

    public String getFullName() {
        return getName() + "@" + mysql.getName();
    }

    public String getName() {
        return name;
    }

    public LocalMysqlDatabaseConfigService write(PrintStream out) {
        NSession session = context.getSession();
        NElements.of(session).json().setValue(getConfig()).setNtf(false).print(out);
        return this;
    }

    public ArchiveResult backup(String path) {
        if (NBlankable.isBlank(path)) {
            String databaseName = getConfig().getDatabaseName();
            if (NBlankable.isBlank(databaseName)) {
                databaseName = name;
            }
            path = databaseName + "-" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".sql.zip";
        }
        if (!path.endsWith(".sql.zip") && !path.endsWith(".zip") && !path.endsWith(".sql")) {
            path = path + ".sql.zip";
        }
        path= Paths.get(path).toAbsolutePath().normalize().toString();
        String password = getConfig().getPassword();
        NSession session = context.getSession();
        char[] credentials = NWorkspaceSecurityManager.of(session).getCredentials(password.toCharArray());
        password = new String(credentials);
        if (path.endsWith(".sql")) {
            if (session.isPlainTrace()) {
                session.out().printf("%s create archive %s%n", getDatabaseName(), path);
            }

            NExecCommand cmd = NExecCommand.of(session)
                    .setExecutionType(NExecutionType.SYSTEM)
                    .setCommand("sh", "-c",
                            "\"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p\"$CMD_PWD\" --databases \"$CMD_DB\" > \"$CMD_FILE\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", password)
                    .setEnv("CMD_DB", getDatabaseName())
                    .grabOutputString()
                    .setRedirectErrorStream(true);
            int result = cmd
                    .getResult();
            if (result == 0) {
                return new ArchiveResult(path, result, false);
            } else {
                if (new File(path).exists()) {
                    new File(path).delete();
                }
                throw new NExecutionException(session, NMsg.ofNtf(cmd.getOutputString()), 2);
            }
        } else {
            if (session.isPlainTrace()) {
                session.out().printf("%s create archive %s%n", getBracketsPrefix(getDatabaseName()),
                        NTexts.of(session)
                        .ofStyled(path, NTextStyle.path()));
            }
//                ProcessBuilder2 p = new ProcessBuilder2().setCommand("sh", "-c",
//                        "set -o pipefail && \"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p\"$CMD_PWD\" --databases \"$CMD_DB\" | gzip > \"$CMD_FILE\""
//                )
            NExecCommand cmd = NExecCommand.of(session)
                    .setExecutionType(NExecutionType.SYSTEM)
                    .setCommand("sh", "-c",
                            "set -o pipefail && \"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p" + password + " --databases \"$CMD_DB\" | gzip > \"$CMD_FILE\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", password)
                    .setEnv("CMD_DB", getDatabaseName())
                    //                    .inheritIO()
                    .grabOutputString()
                    .setRedirectErrorStream(true);
            if (session.isPlainTrace()) {
                session.out().printf("%s    [exec] %s%n", getBracketsPrefix(getDatabaseName()),
                        cmd.formatter().setEnvReplacer(envEntry -> {
                            if ("CMD_PWD".equals(envEntry.getName())) {
                                return "****";
                            }
                            return null;
                        }).format()
                );
            }
            int result = cmd.getResult();
            if (result == 0) {
                return new ArchiveResult(path, result, false);
            } else {
                if (new File(path).exists()) {
                    new File(path).delete();
                }
                throw new NExecutionException(session, NMsg.ofNtf(cmd.getOutputString()), 2);
            }
        }
    }

    public RestoreResult restore(String path) {
//        if(!path.endsWith(".sql") && !path.endsWith(".sql.zip") && !path.endsWith(".zip")){
//            path=path+
//        }
        NSession session = context.getSession();
        char[] password = NWorkspaceSecurityManager.of(session).getCredentials(getConfig().getPassword().toCharArray());

        if (path.endsWith(".sql")) {
            if (session.isPlainTrace()) {
                session.out().printf("%s restore archive %s%n", getBracketsPrefix(getDatabaseName()), path);
            }
            int result = NExecCommand.of(session)
                    .setExecutionType(NExecutionType.SYSTEM)
                    .setCommand("sh", "-c",
                            "cat \"$CMD_FILE\" | " + "\"" + mysql.getMysqlCommand() + "\" -h \"$CMD_HOST\" -u \"$CMD_USER\" \"-p$CMD_PWD\" \"$CMD_DB\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", new String(password))
                    .setEnv("CMD_DB", getDatabaseName())
                    .setEnv("CMD_HOST", "localhost")
                    //.inheritIO()
//                        .start().waitFor()
                    .getResult();
            return new RestoreResult(path, result, false);
        } else {
            if (session.isPlainTrace()) {
                session.out().printf("%s restore archive %s%n", getBracketsPrefix(getDatabaseName()), path);
            }

            int result = NExecCommand.of(session)
                    .setExecutionType(NExecutionType.SYSTEM).setCommand("sh", "-c",
                            "gunzip -c \"$CMD_FILE\" | \"" + mysql.getMysqlCommand() + "\" -h \"$CMD_HOST\" -u \"$CMD_USER\" \"-p$CMD_PWD\" \"$CMD_DB\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", new String(password))
                    .setEnv("CMD_DB", getDatabaseName())
                    .setEnv("CMD_HOST", "localhost")
//                        .start()
//                        .inheritIO()
//                        .waitFor()
                    .getResult();
            return new RestoreResult(path, result, true);
        }
    }

    public String getDatabaseName() {
        String s = getConfig().getDatabaseName();
        if (NBlankable.isBlank(s)) {
            s = name;
        }
        return s;
    }

    public static class ArchiveResult {

        public String path;
        public int execResult;
        public boolean zip;

        public ArchiveResult(String path, int execResult, boolean zip) {
            this.path = path;
            this.execResult = execResult;
            this.zip = zip;
        }
    }

    public static class RestoreResult {

        public String path;
        public int execResult;
        public boolean zip;

        public RestoreResult(String path, int execResult, boolean zip) {
            this.path = path;
            this.execResult = execResult;
            this.zip = zip;
        }
    }
}
