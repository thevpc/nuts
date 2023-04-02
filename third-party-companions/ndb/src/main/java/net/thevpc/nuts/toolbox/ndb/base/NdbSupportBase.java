package net.thevpc.nuts.toolbox.ndb.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.*;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.DbUrlString;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class NdbSupportBase<C extends NdbConfig> implements NdbSupport {
    protected Class<C> configClass;
    protected NSession session;
    protected String dbType;
    protected NPath sharedConfigFolder;
    protected Map<String, NdbCmd<C>> commands = new HashMap<>();

    public NdbSupportBase(String dbType, Class<C> configClass, NSession session) {
        this.dbType = dbType;
        this.configClass = configClass;
        this.session = session;
        this.sharedConfigFolder = session.getAppVersionFolder(NStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT)
                .resolve(dbType);
        declareNdbCmd(new AddConfigCmd<>(this));
        declareNdbCmd(new CountCmd<>(this));
        declareNdbCmd(new CreateIndexCmd<>(this));
        declareNdbCmd(new DeleteCmd<>(this));
        declareNdbCmd(new DumpCmd<>(this));
        declareNdbCmd(new FindCmd<>(this));
        declareNdbCmd(new InsertCmd<>(this));
        declareNdbCmd(new QueryCmd<>(this));
        declareNdbCmd(new RemoveConfigCmd<>(this));
        declareNdbCmd(new RenameTableCmd<>(this));
        declareNdbCmd(new ReplaceCmd<>(this));
        declareNdbCmd(new RestoreCmd<>(this));
        declareNdbCmd(new ShowDatabasesCmd<>(this));
        declareNdbCmd(new ShowTablesCmd<>(this));
        declareNdbCmd(new UpdateCmd<>(this));
        declareNdbCmd(new UpdateConfigCmd<>(this));
    }

    protected void declareNdbCmd(NdbCmd<C> cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public void run(NSession session, NCmdLine commandLine) {
        NArg a;
        commandLine.setCommandName("ndb " + dbType);
        while (commandLine.hasNext()) {
            boolean ok = false;
            for (NdbCmd<C> cc : commands.values()) {
                if (commandLine.withNextFlag((value, arg, s) -> {
                    commandLine.setCommandName(getDbType() + " " + arg.key());
                    cc.run(s, commandLine);
                }, cc.getNames())) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                if (runExtraCommand(session, commandLine)) {

                } else {
                    commandLine.getSession().configureLast(commandLine);
                }
            }
        }
    }

    public NOptional<NdbCmd<C>> findCommand(String name) {
        for (NdbCmd<C> value : commands.values()) {
            for (String valueName : value.getNames()) {
                if (Objects.equals(valueName, name)) {
                    return NOptional.of(value);
                }
            }
        }
        return NOptional.ofNamedEmpty("command " + name);
    }

    protected boolean runExtraCommand(NSession session, NCmdLine commandLine) {
        return false;
    }

    public C createConfigInstance() {
        try {
            return configClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected void validateConfig(C options) {
        if (NBlankable.isBlank(options.getName())) {
            throw new RuntimeException("missing name");
        }
    }

    protected String asFullName(String name) {
        return asFullName(new AtName(name));
    }

    protected String asFullName(AtName name) {
        String cn = NdbUtils.coalesce(name.getConfigName(), "default");
        String dn = NdbUtils.coalesce(name.getDatabaseName(), "default");
        return cn + "-" + dn;
    }

    public NOptional<C> loadConfig(AtName name) {
        NPath file = sharedConfigFolder.resolve(asFullName(name) + NdbUtils.SERVER_CONFIG_EXT);
        if (!file.exists()) {
            return NOptional.ofNamedEmpty("config " + name);
        }
        NElements json = NElements.of(session).setNtf(false).json();
        return NOptional.ofNamed(json.parse(file, configClass), "config " + name);
    }


    protected boolean fillExtraOption(NCmdLine cmdLine, C options) {
        return false;
    }


    protected boolean fillOption(NCmdLine cmdLine, C options) {
        NSession session = getSession();
        NArg a;
        if ((a = cmdLine.nextEntry("--name").orNull()) != null) {
            options.setName(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-h", "--host").orNull()) != null) {
            options.setHost(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-p", "--port").orNull()) != null) {
            options.setPort(a.getValue().asInt().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-n", "--dbname").orNull()) != null) {
            options.setDatabaseName(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-u", "--user").orNull()) != null) {
            options.setUser(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-P", "--password").orNull()) != null) {
            options.setPassword(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("--db").orNull()) != null) {
            String db = a.getStringValue().get(session);
            DbUrlString dbUrlString = DbUrlString.parse(db).get();

            options.setRemoteUser(dbUrlString.getSshUser());
            options.setRemotePassword(dbUrlString.getSshPassword());
            options.setRemoteServer(dbUrlString.getSshServer());
            options.setRemotePort(dbUrlString.getSshPort());

            options.setUser(dbUrlString.getDbUser());
            options.setPassword(dbUrlString.getDbPassword());
            options.setHost(dbUrlString.getDbServer());
            options.setPort(dbUrlString.getDbPort());
            options.setDatabaseName(dbUrlString.getDbPath());

            return true;
        } else if ((a = cmdLine.nextEntry("--remote-server").orNull()) != null) {
            options.setRemoteServer(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("--remote-user").orNull()) != null) {
            options.setRemoteUser(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("--remote-temp-folder").orNull()) != null) {
            options.setRemoteTempFolder(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("--ssh").orNull()) != null) {
            String ssh = a.getStringValue().get(session);
            DbUrlString dbUrlString = DbUrlString.parse(ssh).get();
            options.setRemoteUser(dbUrlString.getSshUser());
            options.setRemoteServer(dbUrlString.getSshServer());
            options.setRemoteTempFolder(dbUrlString.getDbPath());
            return true;
        } else if (fillExtraOption(cmdLine, options)) {
            return true;
        }
        return false;
    }

    protected boolean fillAddConfigOption(NCmdLine commandLine) {
        return false;
    }

    protected boolean fillRemoveConfigOption(NCmdLine commandLine) {
        return false;
    }

    protected boolean fillUpdateConfigOption(NCmdLine commandLine) {
        return false;
    }


    public NPath getRemoteTempFolder(C options, NSession session) {
        String remoteTempFolder = options.getRemoteTempFolder();
        if (NBlankable.isBlank(remoteTempFolder)) {
            return NPath.of(NdbUtils.getDefaultUserHome(options.getRemoteUser()), session);
        } else {
            remoteTempFolder = remoteTempFolder.trim();
            return NPath.of(NdbUtils.getDefaultUserHome(options.getRemoteUser()), session).resolve(remoteTempFolder);
        }
    }


    protected void addConfig(NCmdLine commandLine) {
        C options = createConfigInstance();
        NRef<Boolean> update = NRef.of(false);
        while (commandLine.hasNext()) {
            if (fillOption(commandLine, options)) {
                //
            } else if (
                    commandLine.withNextFlag((v, a, s) -> {
                        update.set(v);
                    }, "--update")
            ) {

            } else if (fillAddConfigOption(commandLine)) {

            } else if (session.configureFirst(commandLine)) {

            } else {
                commandLine.throwUnexpectedArgument();
            }
        }
        options.setName(NStringUtils.trimToNull(options.getName()));
        if (NBlankable.isBlank(options.getName())) {
            options.setName("default");
        }

        NPath file = sharedConfigFolder.resolve(asFullName(options.getName()) + NdbUtils.SERVER_CONFIG_EXT);
        NElements json = NElements.of(commandLine.getSession()).setNtf(false).json();
        if (file.exists()) {
            if (update.get()) {
                C old = json.parse(file, configClass);
                String oldName = old.getName();
                old.setNonNull(options);
                old.setName(oldName);
                json.setValue(options).print(file);
            } else {
                throw new RuntimeException("already found");
            }
        } else {
            json.setValue(options).print(file);
        }
    }


    public NExecCommand sysSsh(C options, NSession session) {
        return sysCmd(session).addCommand("ssh", options.getRemoteUser() + "@" + options.getRemoteServer());
    }

    public NExecCommand run(NExecCommand cmd) {
        cmd.getSession().out().println(cmd);
        cmd.run();
        return cmd;
    }

    public NExecCommand sysCmd(NSession session) {
        return NExecCommand.of(session)
                .setFailFast(true)
                .setExecutionType(NExecutionType.SYSTEM)
                ;
    }


    public abstract void revalidateOptions(C options);

    public Class<C> getConfigClass() {
        return configClass;
    }

    public String getDbType() {
        return dbType;
    }

    public NPath getSharedConfigFolder() {
        return sharedConfigFolder;
    }

    public boolean isRemoteHost(String host) {
        if (NBlankable.isBlank(host)) {
            return false;
        }
        if (host.trim().equals("localhost")) {
            return false;
        }
        if (host.trim().startsWith("127.0.0.")) {
            return false;
        }
        return true;
    }

    public boolean isRemoteCommand(C options) {
        return isRemoteHost(options.getRemoteServer());
    }

    public DumpRestoreMode getDumpRestoreMode(C options, NSession session) {
        return DumpRestoreMode.FILE;
    }

    public <C extends NdbConfig> String getDumpExt(C options, NSession session) {
        return ".sql";
    }

    public abstract CmdRedirect createDumpCommand(NPath remoteSql, C options, NSession session);

    public abstract CmdRedirect createRestoreCommand(NPath remoteSql, C options, NSession session);

    public void prepareDump(C options, NSession session) {

    }

    public boolean isFolderArchive(C options) {
        return false;
    }

    public String getZipSubFolder(C options) {
        return null;
    }

    public enum DumpRestoreMode {
        FILE,
        FOLDER
    }
}
