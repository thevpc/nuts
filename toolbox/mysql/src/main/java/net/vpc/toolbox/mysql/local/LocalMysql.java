package net.vpc.toolbox.mysql.local;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.toolbox.mysql.local.config.LocalMysqlConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

public class LocalMysql {

    private NutsApplicationContext context;

    public LocalMysql(NutsApplicationContext ws) {
        this.setContext(ws);
    }

    public int runArgs(String[] args) {
        NutsCommandLine cmd = context.getWorkspace().parser().parseCommandLine(args);
        while (cmd.hasNext()) {
            NutsArgument val = cmd.readNonOption();
            switch (val.getString()) {
                case "list":
                    return list(cmd);
                case "add":
                case "set":
                    return add(cmd);
                case "remove":
                    return remove(cmd);
                case "archive":
                    return archive(cmd);
                case "restore":
                    return restore(cmd);
                default:
                    throw new RuntimeException("Unsupported action " + val.getString());
            }
        }
        return 0;
    }

    public int list(NutsCommandLine args) {
        boolean json = false;
        String instance = null;
        String app = null;
        String property = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readBooleanOption("--json")) != null) {
                json = a.getBooleanValue();
            } else if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.readStringOption("--app")) != null) {
                app = a.getValue().getString();
            } else if ((a = args.readStringOption("--property")) != null) {
                property = a.getValue().getString();
            } else {
                args.unexpectedArgument("mysql --local list");
            }
        }
        if (property == null) {
            if (app != null) {
                LocalMysqlConfigService c = loadOrCreateMysqlConfig(instance);
                LocalMysqlDatabaseConfigService aa = c.getDatabase(app);
                if (json) {
                    getContext().out().printf("[[%s]] :%n", aa.getName());
                    aa.write(getContext().out());
                    getContext().out().println();
                } else {
                    getContext().out().println(aa.getName());
                }
            } else {
                for (LocalMysqlConfigService mysqlConfig : listConfig()) {
                    if (json) {
                        getContext().out().printf("[[%s]] :%n", mysqlConfig.getName());
                        mysqlConfig.write(getContext().out());
                        getContext().out().println();
                    } else {
                        getContext().out().println(mysqlConfig.getName());
                    }
                }
            }
        } else {
            LocalMysqlConfigService c = loadOrCreateMysqlConfig(instance);
            if (app != null) {
                getContext().out().printf("%s%n", context.getWorkspace().parser().parseExpression(c.getDatabase(app).getConfig(), property));
            } else {
                for (LocalMysqlDatabaseConfigService aa : c.getDatabases()) {
                    getContext().out().printf("[%s] %s%n", aa.getName(), context.getWorkspace().parser().parseExpression(aa.getConfig(), property));
                }
            }
        }
        return 0;
    }

    public int add(NutsCommandLine args) {
        LocalMysqlConfigService c = null;
        String appName = null;
        String instance = null;

        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(instance);
                } else {
                    throw new NutsExecutionException("Instance name already defined", 2);
                }
            } else if ((a = args.readStringOption("--shutdownWaitTime")) != null) {
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                c.getConfig().setShutdownWaitTime(a.getValue().getInt());
            } else if ((a = args.readStringOption("--db")) != null) {
                appName = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                c.getDatabaseOrCreate(appName);
            } else if ((a = args.readStringOption("--app.user")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                LocalMysqlDatabaseConfigService mysqlAppConfig = c.getDatabaseOrError(appName);
                if (mysqlAppConfig == null) {
                    throw new NutsExecutionException("Missing --app.user", 2);
                }
                mysqlAppConfig.getConfig().setUser(value);
            } else if ((a = args.readStringOption("--app.password")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                LocalMysqlDatabaseConfigService mysqlAppConfig = c.getDatabaseOrError(appName);
                if (mysqlAppConfig == null) {
                    throw new NutsExecutionException("Missing --app.password", 2);
                }
                mysqlAppConfig.getConfig().setPassword(value);
            } else {
                args.unexpectedArgument("mysql --local add");
            }
        }
        if (c != null) {
            c.saveConfig();
        }
        return 0;
    }

    public int remove(NutsCommandLine args) {
        String conf = null;
        String appName = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--db")) != null) {
                appName = a.getValue().getString();
            } else if ((a = args.readStringOption("--name")) != null) {
                conf = a.getValue().getString();
            } else {
                args.unexpectedArgument("mysql --local remove");
            }
        }
        if (appName == null) {
            loadMysqlConfig(conf).removeConfig();
        } else {
            LocalMysqlConfigService c = loadMysqlConfig(conf);
            try {
                c.getDatabaseOrError(appName).remove();
                c.saveConfig();
            } catch (Exception ex) {
                //
            }
        }
        return 0;
    }

    public int reset() {
        for (LocalMysqlConfigService mysqlConfig : listConfig()) {
            mysqlConfig.removeConfig();
        }
        return 0;
    }

    public LocalMysqlConfigService[] listConfig() {
        List<LocalMysqlConfigService> all = new ArrayList<>();
        try (DirectoryStream<Path> configFiles = Files.newDirectoryStream(getContext().getConfigFolder(), pathname -> pathname.getFileName().toString().endsWith(LocalMysqlConfigService.SERVER_CONFIG_EXT))) {
            for (Path file1 : configFiles) {
                try {
                    LocalMysqlConfigService c = loadMysqlConfig(file1);
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        ;
        return all.toArray(new LocalMysqlConfigService[0]);
    }

    public LocalMysqlConfigService loadMysqlConfig(String name) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public LocalMysqlConfigService loadMysqlConfig(Path file) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(file, this);
        t.loadConfig();
        return t;
    }

    public LocalMysqlConfigService createMysqlConfig(String name) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, this);
        t.setConfig(new LocalMysqlConfig());
        return t;
    }

    public LocalMysqlConfigService loadOrCreateMysqlConfig(String name) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new LocalMysqlConfig());
        }
        return t;
    }

    public NutsApplicationContext getContext() {
        return context;
    }

    public void setContext(NutsApplicationContext context) {
        this.context = context;
    }

    public int restore(NutsCommandLine args) {
        String instance = null;
        String db = null;
        String path = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.readStringOption("--db")) != null) {
                db = a.getValue().getString();
            } else {
                path = args.readNonOption().getString();
                args.unexpectedArgument("mysql --local restore");
            }
        }
        LocalMysqlConfigService c = loadMysqlConfig(instance);
        return c.getDatabaseOrError(db).restore(path).execResult;
    }

    public int archive(NutsCommandLine args) {
        String instance = null;
        String db = null;
        String path = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.readStringOption("--db")) != null) {
                db = a.getValue().getString();
            } else {
                path = args.readNonOption().getString();
                args.unexpectedArgument("mysql --local archive");
            }
        }
        LocalMysqlConfigService c = loadMysqlConfig(instance);
        return c.getDatabaseOrError(db).archive(path).execResult;
    }
}
