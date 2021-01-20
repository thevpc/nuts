package net.thevpc.nuts.toolbox.nmysql;

import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nmysql.local.LocalMysqlConfigService;
import net.thevpc.nuts.toolbox.nmysql.local.LocalMysqlDatabaseConfigService;
import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlConfig;

import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.nmysql.remote.RemoteMysqlConfigService;
import net.thevpc.nuts.toolbox.nmysql.remote.RemoteMysqlDatabaseConfigService;
import net.thevpc.nuts.toolbox.nmysql.remote.config.RemoteMysqlConfig;
import net.thevpc.nuts.toolbox.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.nmysql.util.MysqlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NMysqlMain extends NutsApplication {


    public static void main(String[] args) {
        new NMysqlMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine commandLine = context.getCommandLine();
        NMySqlService service=new NMySqlService(context);
        while(commandLine.hasNext()){
            switch (commandLine.peek().getStringKey()){
                case "add":
                case "create":{
                    commandLine.skip();
                    createOrUpdate(commandLine,true,service);
                    return;
                }
                case "update":{
                    commandLine.skip();
                    createOrUpdate(commandLine,false,service);
                    return;
                }
                case "remove":{
                    commandLine.skip();
                    runRemove(commandLine,service);
                    return;
                }
                case "list":{
                    commandLine.skip();
                    runList(commandLine,service,false);
                    return;
                }
                case "desc":{
                    commandLine.skip();
                    runList(commandLine,service,true);
                    return;
                }
                case "backup":{
                    commandLine.skip();
                    runBackupOrRestore(commandLine,true,service);
                    return;
                }
                case "restore":{
                    commandLine.skip();
                    runBackupOrRestore(commandLine,false,service);
                    return;
                }
                case "pull":{
                    commandLine.skip();
                    runPushOrPull(commandLine,true,service);
                    return;
                }
                case "push":{
                    commandLine.skip();
                    runPushOrPull(commandLine,false,service);
                    return;
                }
                default:{
                    context.configureLast(commandLine);
                }
            }
        }
        runList(commandLine,service,false);
//        String[] args = context.getArguments();
//        if (args.length == 0) {
//            throw new NutsExecutionException(context.getWorkspace(), "Expected --remote or --local", 2);
//        }
//        if (args[0].equals("--remote") || args[0].equals("-c")) {
//            RemoteMysql m = new RemoteMysql(context);
//            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
//        } else if (args[0].equals("--local") || args[0].equals("-s")) {
//            LocalMysql m = new LocalMysql(context);
//            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
//        } else {
//            LocalMysql m = new LocalMysql(context);
//            m.runArgs(args);
//        }
    }


    private void runPushOrPull(NutsCommandLine commandLine, boolean pull, NMySqlService service) {
        commandLine.setCommandName("mysql --remote " + (pull?"pull":"push"));
        AtName name = null;
        String path = null;
        NutsArgument a;
        while (commandLine.hasNext()) {
            if (commandLine.peek().isOption()) {
                switch (commandLine.peek().getStringKey()) {
                    case "--name": {
                        if (name == null) {
                            name = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    case "--path": {
                        if (path == null) {
                            path = commandLine.nextString().getStringValue();
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    default:{
                        service.getContext().configureLast(commandLine);
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
        RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(name.getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
        RemoteMysqlDatabaseConfigService d = c.getDatabase(name.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        if(pull){
            d.pull(path, true, true);
        }else{
            d.push(path, true);
        }
    }

    private void runBackupOrRestore(NutsCommandLine commandLine, boolean backup, NMySqlService service) {
        commandLine.setCommandName("mysql " + (backup?"backup":"restore"));
        AtName name = null;
        String path = null;
        NutsArgument a;
        while (commandLine.hasNext()) {
            if (commandLine.peek().isOption()) {
                switch (commandLine.peek().getStringKey()) {
                    case "--name": {
                        if (name == null) {
                            name = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    case "--path": {
                        if (path == null) {
                            path = commandLine.nextString().getStringValue();
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    default: {
                        service.getContext().configureLast(commandLine);
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
        LocalMysqlConfigService c = service.loadLocalMysqlConfig(name.getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
        LocalMysqlDatabaseConfigService d = c.getDatabase(name.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        if (backup) {
            if (path == null) {
                path = d.getDatabaseName() + "-" + MysqlUtils.newDateString();
            }
            LocalMysqlDatabaseConfigService.ArchiveResult result = d.backup(path);
            service.getContext().getSession().formatObject(result).println();
        }else{
            if (path == null) {
                commandLine.required("missing --path");
            }
            LocalMysqlDatabaseConfigService.RestoreResult result = d.restore(path);
            service.getContext().getSession().formatObject(result).println();
        }
    }



    private void createOrUpdate(NutsCommandLine commandLine, boolean add,NMySqlService service) {
        commandLine.setCommandName("mysql " + (add ? "add" : "set"));
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
        AtName forRemote_remoteName = null;
        AtName forRemote_localName = null;
        String forRemote_server = null;
        Boolean expectedRemote=null;
        boolean askPassword = false;
        while (commandLine.hasNext()) {
            if (commandLine.peek().isOption()) {
                switch (commandLine.peek().getStringKey()) {
                    case "--name": {
                        if (name == null) {
                            name = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    case "--shutdown-wait-time": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_shutdown_wait_time = commandLine.nextString().getArgumentValue().getInt();
                        break;
                    }
                    case "--startup-wait-time": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_startup_wait_time = commandLine.nextString().getArgumentValue().getInt();
                        break;
                    }
                    case "--backup-folder": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_archive_folder = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--running-folder": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_running_folder = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--log-file": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_log_file = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--mysql-command": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_mysql_command = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--mysqldump-command": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_mysqldump_command = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--kill": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        c_kill = commandLine.nextBoolean().getBooleanValue();
                        break;
                    }
                    case "--user": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        user = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--password": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        password = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--ask-password": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        askPassword = commandLine.nextBoolean().getBooleanValue();
                        break;
                    }
                    case "--db": {
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        dbname = commandLine.nextString().getStringValue();
                        break;
                    }
                    case "--local-name": {
                        if(expectedRemote==null){
                            expectedRemote=true;
                        }else if(!expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        if (forRemote_localName == null) {
                            forRemote_localName = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    case "--remote-name": {
                        if(expectedRemote==null){
                            expectedRemote=true;
                        }else if(!expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        if (forRemote_remoteName == null) {
                            forRemote_remoteName = AtName.nextAppOption(commandLine);
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    case "--server": {
                        if(expectedRemote==null){
                            expectedRemote=true;
                        }else if(!expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        if (forRemote_server == null) {
                            forRemote_server = commandLine.nextString().getStringValue();
                        } else {
                            commandLine.unexpectedArgument("already defined");
                        }
                        break;
                    }
                    case "--local": {
                        commandLine.nextBoolean();
                        if(expectedRemote==null){
                            expectedRemote=false;
                        }else if(expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        break;
                    }
                    case "--remote": {
                        commandLine.nextBoolean();
                        if(expectedRemote==null){
                            expectedRemote=true;
                        }else if(!expectedRemote){
                            commandLine.unexpectedArgument();
                        }
                        break;
                    }
                    default: {
                        if(commandLine.peek().isNonOption()){
                            if (name == null) {
                                name = AtName.nextAppOption(commandLine);
                            } else {
                                commandLine.unexpectedArgument("already defined");
                            }
                        }else{
                            service.getContext().configureLast(commandLine);
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
        if(expectedRemote==null){
            expectedRemote=false;
        }
        if(expectedRemote && forRemote_server==null){
            commandLine.required("required --server option");
        }
        NutsTextNodeFactory factory = service.getContext()
                .getWorkspace().formats().text().factory();
        if (commandLine.isExecMode()) {
            if(!expectedRemote) {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(name.getConfigName(), add?NutsOpenMode.OPEN_OR_CREATE:NutsOpenMode.OPEN_OR_ERROR);
                boolean overrideExisting = false;
                if (add) {
                    if (name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!service.getContext().getSession().getTerminal().ask()
                                    .forBoolean("already exists %s. override?", factory.styled(name.toString(),
                                                    NutsTextNodeStyle.primary(3))
                                    )
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(service.getContext().getWorkspace(), "already exists " + name, 2);
                            }
                        }
                    } else {
                        if (c.getDatabase(name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!service.getContext().getSession().getTerminal().ask()
                                    .forBoolean("already exists %s. override?",factory.styled(name.toString(),NutsTextNodeStyle.primary(3)))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(service.getContext().getWorkspace(), "already exists " + name, 2);
                            }
                        }
                    }
                } else {
                    if (name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(service.getContext().getWorkspace(), "not found " + name, 2);
                        }
                    } else {
                        if (c.getDatabase(name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(service.getContext().getWorkspace(), "not found  " + name, 2);
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
                    if (someUpdates && service.getContext().getSession().isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("adding local config (with override) %s%n",
                                        factory.styled(
                                        StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3))
                                );
                            } else {
                                service.getContext().getSession().out().printf("adding local config %s%n",
                                        factory.styled(
                                        StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3)));
                            }
                        } else {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("updating local config (with override) %s%n",
                                        factory.styled(StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3)));
                            } else {
                                service.getContext().getSession().out().printf("updating local config %s%n",
                                        factory.styled(
                                        StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3)));
                            }
                        }
                    }
                } else {
                    LocalMysqlDatabaseConfigService r = c.getDatabase(name.getDatabaseName(), NutsOpenMode.OPEN_OR_CREATE);
                    if (user != null) {
                        someUpdates = true;
                        r.getConfig().setUser(user);
                    }
                    if (password != null) {
                        someUpdates = true;
                        r.getConfig().setPassword(
                                new String(service.getContext().getWorkspace().security().createCredentials(password.toCharArray(), true,
                                        null, service.getContext().getSession()))
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
                                new String(service.getContext().getWorkspace().security()
                                        .createCredentials(service.getContext().getSession().getTerminal().readPassword("Password"), true,
                                                null,
                                                service.getContext().getSession())
                                )
                        );
                    }
                    if (r.getConfig().getUser() == null) {
                        throw new NutsExecutionException(service.getContext().getWorkspace(), "missing --user", 2);
                    }
                    if (r.getConfig().getPassword() == null) {
                        throw new NutsExecutionException(service.getContext().getWorkspace(), "missing --password", 2);
                    }
                    if (r.getConfig().getDatabaseName() == null) {
                        throw new NutsExecutionException(service.getContext().getWorkspace(), "missing --name", 2);
                    }
                    if (someUpdates && service.getContext().getSession().isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("adding local instance (with override) %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3)));
                            } else {
                                service.getContext().getSession().out().printf("adding local instance %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3)));
                            }
                        } else {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("updating local instance (with override) %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3))
                                );
                            } else {
                                service.getContext().getSession().out().printf("updating local instance %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3))
                                );
                            }
                        }
                    }
                }
                if (!someUpdates) {
                    throw new NutsExecutionException(service.getContext().getWorkspace(), "nothing to save", 2);
                }

                c.saveConfig();
            }else{
                if(forRemote_localName==null && forRemote_remoteName==null){
                    forRemote_localName=name;
                    forRemote_remoteName=name;
                }else if(forRemote_localName==null){
                    forRemote_localName=forRemote_remoteName;
                }else if(forRemote_remoteName==null){
                    forRemote_remoteName=forRemote_localName;
                }
                service.loadLocalMysqlConfig(forRemote_localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR)
                .getDatabase(forRemote_localName.getDatabaseName(),NutsOpenMode.OPEN_OR_ERROR)
                ;
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(name.getConfigName(), NutsOpenMode.OPEN_OR_CREATE);
                boolean overrideExisting = false;
                if (add) {
                    if (name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(name.getDatabaseName(),NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!service.getContext().getSession().getTerminal().ask()
                                    .forBoolean("already exists %s. override?", factory.styled(name.toString(),NutsTextNodeStyle.primary(3)))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(service.getContext().getWorkspace(), "already exists " + name, 2);
                            }
                        }
                    } else {
                        if (c.getDatabase(name.getDatabaseName(),NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!service.getContext().getSession().getTerminal().ask()
                                    .forBoolean("already exists %s. override?", factory.styled(name.toString(),NutsTextNodeStyle.primary(3)))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(service.getContext().getWorkspace(), "already exists " + name, 2);
                            }
                        }
                    }
                } else {
                    if (name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(name.getDatabaseName(),NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(service.getContext().getWorkspace(), "not found " + name, 2);
                        }
                    } else {
                        if (c.getDatabase(name.getDatabaseName(),NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(service.getContext().getWorkspace(), "not found  " + name, 2);
                        }
                    }
                }
                boolean someUpdates = false;
                if (name.getDatabaseName().isEmpty()) {
//                if (c_mysqldump_command != null) {
//                    someUpdates = true;
//                    c.getConfig().setMysqldumpCommand(c_mysqldump_command);
//                }
                    if (someUpdates && service.getContext().getSession().isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("adding remote config (with override) %s%n",
                                        factory.styled(StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3)));
                            } else {
                                service.getContext().getSession().out().printf("adding remote config %s%n",
                                        factory.styled(StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3)));
                            }
                        } else {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("updating remote config (with override) %s%n",
                                        factory.styled(StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3)));
                            } else {
                                service.getContext().getSession().out().printf("updating remote config %s%n",
                                        factory.styled(StringUtils.coalesce(name.getConfigName(), "default"),NutsTextNodeStyle.primary(3)));
                            }
                        }
                    }
                } else {
                    RemoteMysqlDatabaseConfigService r = c.getDatabaseOrCreate(name.getDatabaseName());
                    if (forRemote_localName != null) {
                        someUpdates = true;
                        r.getConfig().setLocalName(forRemote_localName.toString());
                    }
                    if (forRemote_remoteName != null) {
                        someUpdates = true;
                        r.getConfig().setRemoteName(forRemote_remoteName.toString());
                    }
                    if (forRemote_server != null) {
                        someUpdates = true;
                        r.getConfig().setServer(forRemote_server);
                    }
                    if (someUpdates && service.getContext().getSession().isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("adding remote instance (with override) %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3)));
                            } else {
                                service.getContext().getSession().out().printf("adding remote instance %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3)));
                            }
                        } else {
                            if (overrideExisting) {
                                service.getContext().getSession().out().printf("updating remote instance (with override) %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3))
                                );
                            } else {
                                service.getContext().getSession().out().printf("updating remote instance %s%n",
                                        factory.styled(r.getFullName(),NutsTextNodeStyle.primary(3)));
                            }
                        }
                    }
                }
                if (!someUpdates) {
                    throw new NutsExecutionException(service.getContext().getWorkspace(), "nothing to save", 2);
                }

                c.saveConfig();
            }
        }
    }

    public void runRemove(NutsCommandLine commandLine,NMySqlService service) {
        commandLine.setCommandName("mysql remove");
        List<AtName> localNames=new ArrayList<>();
        List<AtName> remoteNames=new ArrayList<>();
        boolean currentLocal=true;
        NutsArgument a;

        while (commandLine.hasNext()) {
            if (commandLine.peek().isOption()) {
                switch (commandLine.peek().getStringKey()) {
                    case "--remote": {
                        commandLine.nextBoolean();
                        currentLocal=false;
                        break;
                    }
                    case "--local": {
                        commandLine.nextBoolean();
                        currentLocal=true;
                        break;
                    }
                    case "--name": {
                        if(currentLocal) {
                            localNames.add(AtName.nextAppOption(commandLine));
                        }else {
                            remoteNames.add(AtName.nextAppOption(commandLine));
                        }
                        break;
                    }
                    default: {
                        service.getContext().configureLast(commandLine);
                    }
                }
            } else {
                if(currentLocal) {
                    localNames.add(AtName.nextAppNonOption(commandLine));
                }else {
                    remoteNames.add(AtName.nextAppNonOption(commandLine));
                }
            }
        }
        if(localNames.isEmpty() && remoteNames.isEmpty()){
            commandLine.required();
        }
        for (AtName localName : localNames) {
            if (localName.getDatabaseName().isEmpty()) {
                service.loadLocalMysqlConfig(localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR).removeConfig();
            } else {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(localName.toString(), NutsOpenMode.OPEN_OR_NULL);
                if(c!=null) {
                    LocalMysqlDatabaseConfigService v = c.getDatabase(localName.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL);
                    if (v != null) {
                        v.remove();
                        c.saveConfig();
                    }
                }
            }
        }
        for (AtName remoteName : remoteNames) {
            if (remoteName.getDatabaseName().isEmpty()) {
                RemoteMysqlConfigService v = service.loadRemoteMysqlConfig(remoteName.getConfigName(), NutsOpenMode.OPEN_OR_NULL);
                if(v!=null) {
                    v.removeConfig();
                }
            } else {
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(remoteName.getConfigName(), NutsOpenMode.OPEN_OR_NULL);
                if(c!=null) {
                    RemoteMysqlDatabaseConfigService v = c.getDatabase(remoteName.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL);
                    if(v!=null) {
                        v.remove();
                        c.saveConfig();
                    }
                }
            }
        }
    }

    public Object toObject(String dbName, String confName, LocalMysqlDatabaseConfig config,boolean describe,boolean plain,NutsApplicationContext context) {
        NutsTextFormatManager text = context.getSession().getWorkspace().formats().text();
        if(!describe){
            if(plain){
                return text.builder()
                        .append(" [local ] ",NutsTextNodeStyle.primary(4))
                        .append(dbName).append("@").append(confName,NutsTextNodeStyle.primary(4))
                        ;
            }else {
                return new Object[]{"local",dbName, confName};
            }
        }else{
            if(plain){
                return text.builder()
                        .append(" [local ] ",NutsTextNodeStyle.primary(4))
                        .append(dbName).append("@").append(confName,NutsTextNodeStyle.primary(4))
                        .append(" db=").append(config.getDatabaseName())
                        .append(" user=").append(config.getUser());
            }else {
                return new Object[]{"local",dbName, confName,config.getUser()};
            }
        }
    }

    public Object toObject(String dbName, String confName, RemoteMysqlDatabaseConfig config,boolean describe,boolean plain,NutsApplicationContext context) {
        NutsTextFormatManager text = context.getSession().getWorkspace().formats().text();
        if(!describe){
            if(plain){
                return text.builder()
                        .append(" [remote] ",NutsTextNodeStyle.primary(4))
                        .append(dbName).append("@").append(confName,NutsTextNodeStyle.primary(4))
                        ;
            }else {
                return new Object[]{"remote",dbName, confName};
            }
        }else{
            if(plain){
                return text.builder()
                        .append(" [remote] ",NutsTextNodeStyle.primary(4))
                        .append(dbName).append("@").append(confName,NutsTextNodeStyle.primary(4))
                        .append(" local=").append(config.getLocalName())
                        .append(" remote=").append(config.getRemoteName())
                        .append(" on=").append(config.getServer())
                        ;
            }else {
                return new Object[]{"remote",dbName, confName,config.getLocalName(),config.getRemoteName(),config.getServer()};
            }
        }
    }

    public void runList(NutsCommandLine commandLine,NMySqlService service,boolean describe) {
        commandLine.setCommandName("mysql list");
        List<AtName> localNames = new ArrayList<>();
        List<AtName> remoteNames = new ArrayList<>();
        Boolean expectedLocal=null;
        while (commandLine.hasNext()) {
            if(commandLine.peek().isOption()){
                switch (commandLine.peek().getStringKey()){
                    case "--local":{
                        commandLine.nextBoolean();
                        expectedLocal=true;
                        break;
                    }
                    case "--remote":{
                        commandLine.nextBoolean();
                        expectedLocal=false;
                        break;
                    }
                    default:{
                        service.getContext().configureLast(commandLine);
                    }
                }
            }else{
                if(expectedLocal==null){
                    expectedLocal=true;
                }
                if(expectedLocal){
                    localNames.add(AtName.nextConfigNonOption(commandLine));
                }else{
                    remoteNames.add(AtName.nextConfigNonOption(commandLine));
                }
            }
        }
        List<LocaleOrRemote> result=new ArrayList<>();
        if(localNames.isEmpty() && remoteNames.isEmpty()){
            if(expectedLocal==null || expectedLocal){
                for (LocalMysqlConfigService c : service.listLocalConfig()) {
                    result.add(new LocaleOrRemote(c.getName(), c.getConfig(),null));
                }
            }
            if(expectedLocal==null || !expectedLocal) {
                for (RemoteMysqlConfigService c : service.listRemoteConfig()) {
                    result.add(new LocaleOrRemote(c.getName(), null, c.getConfig()));
                }
            }
        }else{
            for (AtName localName : localNames) {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
                result.add(new LocaleOrRemote(c.getName(), c.getConfig(),null));
            }
            for (AtName localName : remoteNames) {
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
                result.add(new LocaleOrRemote(c.getName(), null,c.getConfig()));
            }
        }

        NutsSession session = service.getContext().getSession();
        if (session.isIterableOut()) {
            try {
                session.getIterableOutput().start();
                for (LocaleOrRemote cnf : result) {
                    if(cnf.local!=null) {
                        for (Map.Entry<String, LocalMysqlDatabaseConfig> db : cnf.local.getDatabases().entrySet()) {
                            session.getIterableOutput().next(toObject(db.getKey(),cnf.name,db.getValue(),describe,false, service.getContext()));
                        }
                    }else{
                        for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.remote.getDatabases().entrySet()) {
                            session.getIterableOutput().next(toObject(db.getKey(),cnf.name,db.getValue(),describe,false, service.getContext()));
                        }
                    }
                }
            } finally {
                session.getIterableOutput().complete();
            }
        } else {
            switch (session.getOutputFormat()) {
                case PLAIN: {
                    NutsTextFormatManager text = session.getWorkspace().formats().text();
                    for (LocaleOrRemote cnf : result) {
                        if(cnf.local!=null) {
                            for (Map.Entry<String, LocalMysqlDatabaseConfig> db : cnf.local.getDatabases().entrySet()) {
                                service.getContext().getSession().out().printf("%s%n",
                                        toObject(db.getKey(),cnf.name,db.getValue(),describe,true, service.getContext())
                                );
                            }
                        }else{
                            for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.remote.getDatabases().entrySet()) {
                                service.getContext().getSession().out().printf("%s%n",
                                        toObject(db.getKey(),cnf.name,db.getValue(),describe,true, service.getContext())
                                );
                            }
                        }
                    }
                    break;
                }
                default: {
                    service.getContext().getSession().formatObject(result).println();
                }
            }
        }
    }
    private static class LocaleOrRemote{
        String name;
        LocalMysqlConfig local;
        RemoteMysqlConfig remote;

        public LocaleOrRemote(String name, LocalMysqlConfig local, RemoteMysqlConfig remote) {
            this.name = name;
            this.local = local;
            this.remote = remote;
        }
    }

}
