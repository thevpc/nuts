package net.vpc.toolbox.mysql.remote;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.app.NutsAppUtils;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlConfig;
import net.vpc.toolbox.mysql.util.UserCancelException;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteMysql {

    public NutsApplicationContext context;

    public RemoteMysql(NutsApplicationContext context) {
        this.context = context;
    }

    public int runArgs(String[] args) {
        CommandLine cmd = new CommandLine(args);
        Argument a;
        while (cmd.hasNext()) {
            if ((a = cmd.readNonOption("list")) != null) {
                return list(cmd);
            } else if ((a = cmd.readNonOption("add", "set")) != null) {
                return list(cmd);
            } else if ((a = cmd.readNonOption("remove")) != null) {
                return remove(cmd);
            } else if ((a = cmd.readNonOption("push")) != null) {
                return push(cmd);
            } else if ((a = cmd.readNonOption("pull")) != null) {
                return pull(cmd);
            } else {
                cmd.unexpectedArgument("mysql --remote");
            }
        }
        return 0;
    }

    public int list(CommandLine args) {
        boolean json = false;
        String instance = null;
        String app = null;
        String property = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readBooleanOption("--json")) != null) {
                json = a.getBooleanValue();
            } else if ((a = args.readBooleanOption("--name")) != null) {
                instance = a.getStringValue();
            } else if ((a = args.readBooleanOption("--db")) != null) {
                app = a.getStringValue();
            } else if ((a = args.readBooleanOption("--property")) != null) {
                property = a.getStringValue();
            } else {
                args.unexpectedArgument("mysql --remote list");
            }
        }
        if (property == null) {
            if (app != null) {
                RemoteMysqlConfigService c = loadOrCreateMysqlConfig(instance);
                RemoteMysqlDatabaseConfigService aa = c.getDatabase(app);
                if (json) {
                    context.out().printf("[[%s]] :%n", aa.getName());
                    aa.write(context.out());
                    context.out().println();
                } else {
                    context.out().println(aa.getName());
                }
            } else {
                for (RemoteMysqlConfigService mysqlConfig : listConfig()) {
                    if (json) {
                        context.out().printf("[[%s]] :%n", mysqlConfig.getName());
                        mysqlConfig.write(context.out());
                        context.out().println();
                    } else {
                        context.out().println(mysqlConfig.getName());
                    }
                }
            }
        } else {
            RemoteMysqlConfigService c = loadOrCreateMysqlConfig(instance);
            if (app != null) {
                context.out().printf("%s%n", NutsAppUtils.getPropertyValue(c.getDatabase(app).getConfig(), property));
            } else {
                for (RemoteMysqlDatabaseConfigService aa : c.getApps()) {
                    context.out().printf("[%s] %s%n", aa.getName(), NutsAppUtils.getPropertyValue(aa.getConfig(), property));
                }
            }
        }
        return 0;
    }

    private int add(CommandLine args) {
        RemoteMysqlConfigService c = null;
        String appName = null;
        String instanceName = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                if (c == null) {
                    instanceName = a.getStringValue();
                    c = loadOrCreateMysqlConfig(instanceName);
                } else {
                    throw new NutsExecutionException("instance already defined", 2);
                }
            } else if ((a = args.readStringOption("--server")) != null) {
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                db.getConfig().setServer(a.getStringValue());
            } else if ((a = args.readStringOption("--remote-instance")) != null) {
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                db.getConfig().setRemoteInstance(a.getStringValue());
            } else if ((a = args.readStringOption("--remote-temp-path")) != null) {
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                db.getConfig().setRemoteTempPath(a.getStringValue());
            } else if ((a = args.readStringOption("--app")) != null) {
                appName = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                c.getDatabaseOrCreate(appName);
            } else if ((a = args.readStringOption("--app.path")) != null) {
                String value = a.getStringValue();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService mysqlAppConfig = c.getDatabaseOrError(appName);
                mysqlAppConfig.getConfig().setPath(value);
            } else {
                args.unexpectedArgument("mysql --remote add");
            }
        }
        if (c == null) {
            c = loadOrCreateMysqlConfig(null);
        }
        boolean ok = false;
        while (!ok) {
            try {
                ok = true;
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                if (StringUtils.isEmpty(db.getConfig().getServer())) {
                    ok = false;
                    db.getConfig().setServer(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--server").setDefautValue("ssh://login@myserver")));
                }
                if (StringUtils.isEmpty(db.getConfig().getRemoteInstance())) {
                    ok = false;
                    db.getConfig().setRemoteInstance(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--remote-instance").setDefautValue("default")));
                }
                if (StringUtils.isEmpty(db.getConfig().getRemoteTempPath())) {
                    ok = false;
                    db.getConfig().setRemoteTempPath(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--remote-temp-path").setDefautValue("/tmp")));
                }
                for (RemoteMysqlDatabaseConfigService aa : c.getApps()) {
                    if (StringUtils.isEmpty(aa.getConfig().getPath())) {
                        ok = false;
                        aa.getConfig().setPath(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?", c.getName(), aa.getName(), "-app.path")));
                    }
                }
            } catch (UserCancelException ex) {
                return 1;
            }
        }
        c.saveConfig();
        return 0;
    }

    private int remove(CommandLine args) {
        String instance = null;
        String appName = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--db")) != null) {
                appName = a.getStringValue();
            } else if ((a = args.readStringOption("--name")) != null) {
                instance = a.getStringValue();
            } else {
                args.unexpectedArgument("mysql --remote remove");
            }
        }
        if (appName == null) {
            loadMysqlConfig(instance).removeConfig();
        } else {
            RemoteMysqlConfigService c = loadMysqlConfig(instance);
            try {
                c.getDatabaseOrError(appName).remove();
                c.saveConfig();
            } catch (Exception ex) {
                //
            }
        }
        return 0;
    }

    private int pull(CommandLine args) {
        String conf = null;
        String app = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                conf = a.getStringValue();
            } else if ((a = args.readStringOption("--db")) != null) {
                app = a.getStringValue();
            } else {
                args.unexpectedArgument("mysql --remote pull");
            }
        }
        RemoteMysqlConfigService c = loadMysqlConfig(conf);
        c.getDatabase(app).pull();
        return 0;
    }

    private int push(CommandLine args) {
        String conf = null;
        String app = null;
        Argument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                conf = a.getStringValue();
            } else if ((a = args.readStringOption("--db")) != null) {
                app = a.getStringValue();
            } else {
                args.unexpectedArgument("mysql --remote push");
            }
        }
        RemoteMysqlConfigService c = loadMysqlConfig(conf);
        c.getDatabase(app).push();
        return 0;
    }

    public RemoteMysqlConfigService[] listConfig() {
        List<RemoteMysqlConfigService> all = new ArrayList<>();
        try (DirectoryStream<Path> configFiles = Files.newDirectoryStream(context.getConfigFolder(), x -> x.getFileName().toString().endsWith(".config"))) {
            for (Path file1 : configFiles) {
                try {
                    String nn = file1.getFileName().toString();
                    RemoteMysqlConfigService c = loadMysqlConfig(nn.substring(0, nn.length() - ".config".length()));
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return all.toArray(new RemoteMysqlConfigService[0]);
    }

    public RemoteMysqlConfigService loadMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public RemoteMysqlConfigService createMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
        t.setConfig(new RemoteMysqlConfig());
        return t;
    }

    public RemoteMysqlConfigService loadOrCreateMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new RemoteMysqlConfig());
        }
        return t;
    }
}
