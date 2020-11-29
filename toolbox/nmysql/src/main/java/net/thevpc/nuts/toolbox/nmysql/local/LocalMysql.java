package net.thevpc.nuts.toolbox.nmysql.local;

import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlConfig;
import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlDatabaseConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsSession;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.toolbox.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.nmysql.util.MysqlUtils;
import net.thevpc.nuts.NutsCommandLine;

public class LocalMysql {
    private static final Logger LOG=Logger.getLogger(LocalMysql.class.getName());
    private NutsApplicationContext context;

    public LocalMysql(NutsApplicationContext ws) {
        this.setContext(ws);
    }

    public void runArgs(String[] args) {
        NutsCommandLine cmd = context.getWorkspace().commandLine().create(args)
                .setCommandName("mysql --local");
        while (cmd.hasNext()) {
            if (context.configureFirst(cmd)) {
                //
            } else {
                NutsArgument val = cmd.requireNonOption().next();
                switch (val.getString()) {
                    case "list":
                    case "ls":
                        list(cmd);
                        cmd.unexpectedArgument();
                        return;
                    case "add":
                    case "create":
                        create(cmd);
                        cmd.unexpectedArgument();
                        return;
                    case "set":
                    case "update":
                        update(cmd);
                        cmd.unexpectedArgument();
                        return;
                    case "remove":
                    case "delete":
                        remove(cmd);
                        cmd.unexpectedArgument();
                        return;
                    case "backup":
                        backup(cmd);
                        cmd.unexpectedArgument();
                        return;
                    case "restore":
                        restore(cmd);
                        cmd.unexpectedArgument();
                        return;
                    default:
                        cmd.unexpectedArgument();
                }
            }
        }
        cmd.required("missing command (list,add,set,remove,backup or restore)");
    }

    public void list(NutsCommandLine args) {
        args.setCommandName("mysql --local list");
        AtName name = null;
        while (args.hasNext()) {
            if (context.configureFirst(args)) {
                //
            } else if (args.peek().getStringKey().equals("--name")) {
                name = AtName.nextConfigOption(args);
            } else if (name == null && args.peek().isNonOption()) {
                name = AtName.nextConfigNonOption(args);
            } else {
                args.unexpectedArgument();
            }
        }
        LinkedHashMap<String, LocalMysqlConfig> result = new LinkedHashMap<>();
        if (name != null) {
            LocalMysqlConfigService c = loadMysqlConfigOrCreate(name.getConfigName());
            result.put(c.getName(), c.getConfig());
        } else {
            for (LocalMysqlConfigService c : listConfig()) {
                result.put(c.getName(), c.getConfig());
            }
        }
        NutsSession session = context.getSession();
        if (session.isIterableOut()) {
            try {
                session.getIterableOutput().start();
                for (Map.Entry<String, LocalMysqlConfig> cnf : result.entrySet()) {
                    for (Map.Entry<String, LocalMysqlDatabaseConfig> db : cnf.getValue().getDatabases().entrySet()) {
                        session.getIterableOutput().next(new Object[]{db.getKey(), cnf.getKey()});
                    }
                }
            } finally {
                session.getIterableOutput().complete();
            }
        } else {
            switch (session.getOutputFormat()) {
                case PLAIN: {
                    for (Map.Entry<String, LocalMysqlConfig> cnf : result.entrySet()) {
                        for (Map.Entry<String, LocalMysqlDatabaseConfig> db : cnf.getValue().getDatabases().entrySet()) {
                            getContext().getSession().out().printf("%s\\@#####%s#####%n", db.getKey(), cnf.getKey());
                        }
                    }
                    break;
                }
                default: {
                    context.getSession().formatObject(result).println();
                }
            }
        }
    }

    public void create(NutsCommandLine args) {
        createOrUpdate(args, true);
    }

    public void update(NutsCommandLine args) {
        createOrUpdate(args, false);
    }

    private void createOrUpdate(NutsCommandLine commandLine, boolean add) {
        commandLine.setCommandName("mysql --local " + (add ? "add" : "set"));
        AtName name = null;

        NutsArgument a;
        Integer c_shutdown_wait_time = null;
        Integer c_startup_wait_time = null;
        Boolean c_kill = null;
        String c_archive_folder = null;
        String c_running_folder = null;
        String c_log_file = null;
        String c_mysql_command = null;
        String c_mysqldump_command = null;
        String user = null;
        String password = null;
        String dbname = null;
        boolean askPassword = false;
        while (commandLine.hasNext()) {
            if (context.configureFirst(commandLine)) {
                //
            } else if (commandLine.peek().isOption()) {
                switch (commandLine.peek().getStringKey()) {
                    case "--name": {
                        if (name == null) {
                            name = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("Already defined");
                        }
                        break;
                    }
                    case "--shutdown-wait-time": {
                        c_shutdown_wait_time = commandLine.nextString().getArgumentValue().getInt();
                        break;
                    }
                    case "--startup-wait-time": {
                        c_startup_wait_time = commandLine.nextString().getArgumentValue().getInt();
                        break;
                    }
                    case "--backup-folder": {
                        c_archive_folder = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--running-folder": {
                        c_running_folder = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--log-file": {
                        c_log_file = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--mysql-command": {
                        c_mysql_command = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--mysqldump-command": {
                        c_mysqldump_command = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--kill": {
                        c_kill = commandLine.nextBoolean().getBooleanValue();
                        break;
                    }
                    case "--user": {
                        user = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--password": {
                        password = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--ask-password": {
                        askPassword = commandLine.nextBoolean().getBooleanValue();
                        break;
                    }
                    case "--db": {
                        dbname = commandLine.nextString().getStringValue();
                        break;
                    }
                    default: {
                        if (name == null) {
                            name = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("Already defined");
                        }
                        break;
                    }
                }
            } else {
                if (name == null) {
                    name = AtName.nextAppNonOption(commandLine);
                } else {
                    commandLine.unexpectedArgument();
                }
            }
        }
        if (name == null) {
            name = new AtName("");
        }
        if (commandLine.isExecMode()) {
            LocalMysqlConfigService c = loadMysqlConfigOrCreate(name.getConfigName());
            boolean overrideExisting = false;
            if (add) {
                if (name.getDatabaseName().isEmpty()) {
                    if (c.getDatabaseOrNull(name.getDatabaseName()) != null) {
                        overrideExisting = true;
                        if (!context.getSession().getTerminal().ask()
                                .forBoolean("Already exists ####%s####. override?", name)
                                .defaultValue(false).getBooleanValue()) {
                            throw new NutsExecutionException(context.getWorkspace(), "Already exists " + name, 2);
                        }
                    }
                } else {
                    if (c.getDatabaseOrNull(name.getDatabaseName()) != null) {
                        overrideExisting = true;
                        if (!context.getSession().getTerminal().ask()
                                .forBoolean("Already exists ####%s####. override?", name)
                                .defaultValue(false).getBooleanValue()) {
                            throw new NutsExecutionException(context.getWorkspace(), "Already exists " + name, 2);
                        }
                    }
                }
            } else {
                if (name.getDatabaseName().isEmpty()) {
                    if (c.getDatabaseOrNull(name.getDatabaseName()) == null) {
                        throw new NutsExecutionException(context.getWorkspace(), "Not found " + name, 2);
                    }
                } else {
                    if (c.getDatabaseOrNull(name.getDatabaseName()) == null) {
                        throw new NutsExecutionException(context.getWorkspace(), "Not found  " + name, 2);
                    }
                }
            }
            boolean someUpdates = false;
            if (name.getDatabaseName().isEmpty()) {
                if (c_shutdown_wait_time != null) {
                    someUpdates = true;
                    c.getConfig().setShutdownWaitTime(c_shutdown_wait_time);
                }
                if (c_shutdown_wait_time != null) {
                    someUpdates = true;
                    c.getConfig().setStartupWaitTime(c_startup_wait_time);
                }
                if (c_archive_folder != null) {
                    someUpdates = true;
                    c.getConfig().setBackupFolder(c_archive_folder);
                }
                if (c_log_file != null) {
                    someUpdates = true;
                    c.getConfig().setLogFile(c_log_file);
                }
                if (c_running_folder != null) {
                    someUpdates = true;
                    c.getConfig().setRunningFolder(c_running_folder);
                }
                if (c_mysql_command != null) {
                    someUpdates = true;
                    c.getConfig().setMysqlCommand(c_mysql_command);
                }
                if (c_mysqldump_command != null) {
                    someUpdates = true;
                    c.getConfig().setMysqldumpCommand(c_mysqldump_command);
                }
                if (c_kill != null) {
                    someUpdates = true;
                    c.getConfig().setKill(c_kill);
                }
                if (context.getSession().isPlainTrace()) {
                    if (add) {
                        if (overrideExisting) {
                            context.getSession().out().printf("adding config (with override) ####%s####%n", StringUtils.coalesce(name.getConfigName(), "default"));
                        } else {
                            context.getSession().out().printf("adding config ####%s####%n", StringUtils.coalesce(name.getConfigName(), "default"));
                        }
                    } else {
                        if (overrideExisting) {
                            context.getSession().out().printf("updating config (with override) ####%s####%n", StringUtils.coalesce(name.getConfigName(), "default"));
                        } else {
                            context.getSession().out().printf("updating config ####%s####%n", StringUtils.coalesce(name.getConfigName(), "default"));
                        }
                    }
                }
            } else {
                LocalMysqlDatabaseConfigService r = c.getDatabaseOrCreate(name.getDatabaseName());
                if (user != null) {
                    someUpdates = true;
                    r.getConfig().setUser(user);
                }
                if (password != null) {
                    someUpdates = true;
                    r.getConfig().setPassword(
                            new String(context.getWorkspace().security().createCredentials(password.toCharArray(), true,
                                    null, context.getSession()))
                    );
                }
                if (add && dbname == null) {
                    dbname = name.getDatabaseName();
                }
                if (dbname != null) {
                    someUpdates = true;
                    r.getConfig().setDatabaseName(dbname);
                }
                if (askPassword || (!add && password == null)) {
                    r.getConfig().setPassword(
                            new String(context.getWorkspace().security()
                                    .createCredentials(context.getSession().getTerminal().readPassword("Password"), true,
                                            null,
                                            context.getSession())
                            )
                    );
                }
                if (r.getConfig().getUser() == null) {
                    throw new NutsExecutionException(context.getWorkspace(), "Missing --user", 2);
                }
                if (r.getConfig().getPassword() == null) {
                    throw new NutsExecutionException(context.getWorkspace(), "Missing --password", 2);
                }
                if (r.getConfig().getDatabaseName() == null) {
                    throw new NutsExecutionException(context.getWorkspace(), "Missing --name", 2);
                }
                if (context.getSession().isPlainTrace()) {
                    if (add) {
                        if (overrideExisting) {
                            context.getSession().out().printf("adding db (with override) ####%s####%n", r.getFullName());
                        } else {
                            context.getSession().out().printf("adding db ####%s####%n", r.getFullName());
                        }
                    } else {
                        if (overrideExisting) {
                            context.getSession().out().printf("updating db (with override) ####%s####%n", r.getFullName());
                        } else {
                            context.getSession().out().printf("updating db ####%s####%n", r.getFullName());
                        }
                    }
                }
            }
            if (!someUpdates) {
                throw new NutsExecutionException(context.getWorkspace(), "Nothing to save", 2);
            }

            c.saveConfig();
        }
    }

    public void remove(NutsCommandLine commandLine) {
        commandLine.setCommandName("mysql --local remove");
        AtName name = null;
        NutsArgument a;
        while (commandLine.hasNext()) {
            if (context.configureFirst(commandLine)) {
                //
            } else if (commandLine.peek().isOption()) {
                switch (commandLine.peek().getStringKey()) {
                    case "--name": {
                        if (name == null) {
                            name = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("Already defined");
                        }
                        break;
                    }
                    default: {
                        commandLine.unexpectedArgument();
                    }
                }
            } else {
                if (name == null) {
                    name = AtName.nextAppNonOption(commandLine);
                } else {
                    commandLine.unexpectedArgument("Already defined");
                }
                commandLine.unexpectedArgument();
            }
        }
        if (name == null) {
            name = new AtName("");
        }
        if (name.getDatabaseName().isEmpty()) {
            loadMysqlConfig(name.getConfigName()).removeConfig();
        } else {
            LocalMysqlConfigService c = loadMysqlConfig(name.getConfigName());
            c.getDatabaseOrError(name.getDatabaseName()).remove();
            c.saveConfig();
        }
    }

    private void backupOrRestore(NutsCommandLine commandLine, String command) {
        commandLine.setCommandName("mysql --local " + command);
        AtName name = null;
        String path = null;
        NutsArgument a;
        while (commandLine.hasNext()) {
            if (context.configureFirst(commandLine)) {
                //
            } else if (commandLine.peek().isOption()) {
                switch (commandLine.peek().getStringKey()) {
                    case "--name": {
                        if (name == null) {
                            name = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("Already defined");
                        }
                        break;
                    }
                    case "--path": {
                        if (path == null) {
                            path = commandLine.nextString().getStringValue();
                        } else {
                            commandLine.unexpectedArgument("Already defined");
                        }
                        break;
                    }
                }
            } else {
                if (name == null) {
                    name = AtName.nextAppNonOption(commandLine);
                } else if (path == null) {
                    path = commandLine.next().getString();
                } else {
                    commandLine.unexpectedArgument();
                }
            }
        }
        if (name == null) {
            name = new AtName("");
        }
        LocalMysqlConfigService c = loadMysqlConfig(name.getConfigName());
        LocalMysqlDatabaseConfigService d = c.getDatabaseOrError(name.getDatabaseName());
        switch (command) {
            case "backup": {
                if (path == null) {
                    path = d.getDatabaseName() + "-" + MysqlUtils.newDateString();
                }
                d.backup(path);
                break;
            }
            case "restore": {
                if (path == null) {
                    commandLine.required("missing --path");
                }
                d.restore(path);
                break;
            }
        }
    }

    public void restore(NutsCommandLine commandLine) {
        backupOrRestore(commandLine, "restore");
    }

    public void backup(NutsCommandLine commandLine) {
        backupOrRestore(commandLine, "backup");
    }

    public void reset() {
        for (LocalMysqlConfigService mysqlConfig : listConfig()) {
            mysqlConfig.removeConfig();
        }
    }

    public LocalMysqlConfigService[] listConfig() {
        List<LocalMysqlConfigService> all = new ArrayList<>();
        if(Files.isDirectory(getContext().getSharedConfigFolder())) {
            try (DirectoryStream<Path> configFiles = Files.newDirectoryStream(getContext().getSharedConfigFolder(), pathname -> pathname.getFileName().toString().endsWith(LocalMysqlConfigService.SERVER_CONFIG_EXT))) {
                for (Path file1 : configFiles) {
                    try {
                        LocalMysqlConfigService c = loadMysqlConfig(file1);
                        all.add(c);
                    } catch (Exception ex) {
                        LOG.log(Level.FINE,"Error loading config url : "+file1,ex);//e.printStackTrace();
                        //ignore
                    }
                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
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

    public LocalMysqlConfigService loadMysqlConfigOrCreate(String name) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new LocalMysqlConfig());
        }
        return t;
    }

    public LocalMysqlConfigService loadMysqlConfigOrError(String name) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            throw new IllegalArgumentException("No such config " + name);
        }
        return t;
    }

    public LocalMysqlDatabaseConfigService loadDatabaseOrError(String name) {
        AtName atname = new AtName(name);
        return loadMysqlConfigOrError(atname.getConfigName()).getDatabaseOrError(atname.getDatabaseName());
    }

    public NutsApplicationContext getContext() {
        return context;
    }

    public void setContext(NutsApplicationContext context) {
        this.context = context;
    }

}
