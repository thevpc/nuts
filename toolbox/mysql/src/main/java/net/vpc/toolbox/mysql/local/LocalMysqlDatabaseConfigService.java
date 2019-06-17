package net.vpc.toolbox.mysql.local;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.common.io.ProcessBuilder2;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.mysql.local.config.LocalMysqlDatabaseConfig;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.vpc.app.nuts.NutsApplicationContext;

public class LocalMysqlDatabaseConfigService {

    private static ProcessBuilder2.CommandStringFormatterAdapter SECURE_COMMAND_FORMATTER = new ProcessBuilder2.CommandStringFormatterAdapter() {
        @Override
        public String replaceEnvValue(String envName, String envValue) {
            if ("CMD_PWD".equals(envName)) {
                return "****";
            }
            return null;
        }
    };
    private String name;
    private LocalMysqlDatabaseConfig config;
    private LocalMysqlConfigService mysql;
    private NutsApplicationContext context;

    public LocalMysqlDatabaseConfigService(String name, LocalMysqlDatabaseConfig config, LocalMysqlConfigService mysql) {
        this.name = name;
        this.config = config;
        this.mysql = mysql;
        this.context = mysql.getMysqlServer().getContext();
    }

    public LocalMysqlDatabaseConfig getConfig() {
        return config;
    }

    public LocalMysqlConfigService getMysql() {
        return mysql;
    }

    public LocalMysqlDatabaseConfigService remove() {
        mysql.getConfig().getDatabases().remove(name);
        context.session().out().printf("==[%s]== app removed.%n", getFullName());
        return this;
    }

    public String getFullName() {
        return getName() + "@" + mysql.getName();
    }

    public String getName() {
        return name;
    }

    public LocalMysqlDatabaseConfigService write(PrintStream out) {
        context.getWorkspace().format().json().set(getConfig()).print(out);
        return this;
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

    public ArchiveResult backup(String path) {
        try {
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
            if (path.endsWith(".sql")) {
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("==[%s]== create archive %s%n", getDatabaseName(), path);
                }
                ProcessBuilder2 p;
                p = new ProcessBuilder2().setCommand("sh", "-c",
                        "\"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p\"$CMD_PWD\" --databases \"$CMD_DB\" > \"$CMD_FILE\""
                )
                        .setEnv("CMD_FILE", path)
                        .setEnv("CMD_USER", getConfig().getUser())
                        .setEnv("CMD_PWD", getConfig().getPassword())
                        .setEnv("CMD_DB", getDatabaseName())
                        .grabOutputString()
                        .setRedirectErrorStream(true)
                        .start().waitFor();

                int result = p.getResult();
                if (result == 0) {
                    return new ArchiveResult(path, result, false);
                } else {
                    if (new File(path).exists()) {
                        new File(path).delete();
                    }
                    throw new NutsExecutionException(context.getWorkspace(), p.getOutputString(), 2);
                }
            } else {
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("==[%s]== create archive %s%n", getDatabaseName(), path);
                }
                ProcessBuilder2 p = new ProcessBuilder2().setCommand("sh", "-c",
                        "set -o pipefail && \"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p\"$CMD_PWD\" --databases \"$CMD_DB\" | gzip > \"$CMD_FILE\""
                )
                        .setEnv("CMD_FILE", path)
                        .setEnv("CMD_USER", getConfig().getUser())
                        .setEnv("CMD_PWD", getConfig().getPassword())
                        .setEnv("CMD_DB", getDatabaseName())
                        //                    .inheritIO()
                        .grabOutputString()
                        .setRedirectErrorStream(true)
                        .start().waitFor();
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("==[%s]==    [EXEC] %s%n", getDatabaseName(), p.getCommandString(SECURE_COMMAND_FORMATTER));
                }
                int result = p.getResult();
                if (result == 0) {
                    return new ArchiveResult(path, result, false);
                } else {
                    if (new File(path).exists()) {
                        new File(path).delete();
                    }
                    throw new NutsExecutionException(context.getWorkspace(), p.getOutputString(), 2);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public RestoreResult restore(String path) {
//        if(!path.endsWith(".sql") && !path.endsWith(".sql.zip") && !path.endsWith(".zip")){
//            path=path+
//        }
        try {
            if (path.endsWith(".sql")) {
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("==[%s]== restore archive %s%n", getDatabaseName(), path);
                }
                int result = new ProcessBuilder2().setCommand("sh", "-c",
                        "cat \"$CMD_FILE\" | " + "\"" + mysql.getMysqlCommand() + "\" -h \"$CMD_HOST\" -u \"$CMD_USER\" \"-p$CMD_PWD\" \"$CMD_DB\""
                )
                        .setEnv("CMD_FILE", path)
                        .setEnv("CMD_USER", getConfig().getUser())
                        .setEnv("CMD_PWD", getConfig().getPassword())
                        .setEnv("CMD_DB", getDatabaseName())
                        .setEnv("CMD_HOST", "localhost")
                        .inheritIO()
                        .start().waitFor().getResult();
                return new RestoreResult(path, result, false);
            } else {
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("==[%s]== restore archive %s%n", getDatabaseName(), path);
                }
                int result = new ProcessBuilder2().setCommand("sh", "-c",
                        "gunzip -c \"$CMD_FILE\" | \"" + mysql.getMysqlCommand() + "\" -h \"$CMD_HOST\" -u \"$CMD_USER\" \"-p$CMD_PWD\" \"$CMD_DB\""
                )
                        .setEnv("CMD_FILE", path)
                        .setEnv("CMD_USER", getConfig().getUser())
                        .setEnv("CMD_PWD", getConfig().getPassword())
                        .setEnv("CMD_DB", getDatabaseName())
                        .setEnv("CMD_HOST", "localhost")
                        .start()
                        .inheritIO()
                        .waitFor().getResult();
                return new RestoreResult(path, result, true);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public String getDatabaseName() {
        String s = getConfig().getDatabaseName();
        if (StringUtils.isBlank(s)) {
            s = name;
        }
        return s;
    }
}
