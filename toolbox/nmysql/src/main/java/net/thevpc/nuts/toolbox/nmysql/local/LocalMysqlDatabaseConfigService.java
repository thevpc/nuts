package net.thevpc.nuts.toolbox.nmysql.local;

import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlDatabaseConfig;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalMysqlDatabaseConfigService {
    private String name;
    private LocalMysqlDatabaseConfig config;
    private LocalMysqlConfigService mysql;
    private NutsApplicationContext context;

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

    public NutsString getBracketsPrefix(String str) {
        return context.getWorkspace().formats().text().builder()
                .append("[")
                .append(str,NutsTextNodeStyle.primary(5))
                .append("]");
    }

    public String getFullName() {
        return getName() + "@" + mysql.getName();
    }

    public String getName() {
        return name;
    }

    public LocalMysqlDatabaseConfigService write(PrintStream out) {
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(getConfig()).print(out);
        return this;
    }

    public ArchiveResult backup(String path) {
        if (StringUtils.isBlank(path)) {
            String databaseName = getConfig().getDatabaseName();
            if (StringUtils.isBlank(databaseName)) {
                databaseName = name;
            }
            path = databaseName + "-" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".sql.zip";
        }
        if (!path.endsWith(".sql.zip") && !path.endsWith(".zip") && !path.endsWith(".sql")) {
            path = path + ".sql.zip";
        }
        path= Paths.get(path).toAbsolutePath().normalize().toString();
        String password = getConfig().getPassword();
        char[] credentials = context.getWorkspace().security().getCredentials(password.toCharArray(), context.getSession());
        password = new String(credentials);
        if (path.endsWith(".sql")) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("%s create archive %s%n", getDatabaseName(), path);
            }

            NutsExecCommand cmd = context.getWorkspace().exec()
                    .setExecutionType(NutsExecutionType.USER_CMD)
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
                throw new NutsExecutionException(context.getWorkspace(), cmd.getOutputString(), 2);
            }
        } else {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("%s create archive %s%n", getBracketsPrefix(getDatabaseName()), context.getWorkspace().formats().text()
                        .factory().styled(path,NutsTextNodeStyle.path()));
            }
//                ProcessBuilder2 p = new ProcessBuilder2().setCommand("sh", "-c",
//                        "set -o pipefail && \"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p\"$CMD_PWD\" --databases \"$CMD_DB\" | gzip > \"$CMD_FILE\""
//                )
            NutsExecCommand cmd = context.getWorkspace().exec()
                    .setExecutionType(NutsExecutionType.USER_CMD)
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
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("%s    [EXEC] %s%n", getBracketsPrefix(getDatabaseName()),
                        cmd.format().setEnvReplacer(envEntry -> {
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
                throw new NutsExecutionException(context.getWorkspace(), cmd.getOutputString(), 2);
            }
        }
    }

    public RestoreResult restore(String path) {
//        if(!path.endsWith(".sql") && !path.endsWith(".sql.zip") && !path.endsWith(".zip")){
//            path=path+
//        }
        char[] password = context.getWorkspace().security().getCredentials(getConfig().getPassword().toCharArray(),context.getSession());

        if (path.endsWith(".sql")) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("%s restore archive %s%n", getBracketsPrefix(getDatabaseName()), path);
            }
            int result = context.getWorkspace().exec()
                    .setExecutionType(NutsExecutionType.USER_CMD)
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
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("%s restore archive %s%n", getBracketsPrefix(getDatabaseName()), path);
            }

            int result = context.getWorkspace().exec()
                    .setExecutionType(NutsExecutionType.USER_CMD).setCommand("sh", "-c",
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
        if (StringUtils.isBlank(s)) {
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
