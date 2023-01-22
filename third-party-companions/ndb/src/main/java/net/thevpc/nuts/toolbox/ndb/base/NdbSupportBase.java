package net.thevpc.nuts.toolbox.ndb.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.*;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class NdbSupportBase<C extends NdbConfig> implements NdbSupport {
    protected Class<C> configClass;
    protected NApplicationContext appContext;
    protected String dbType;
    protected NPath sharedConfigFolder;
    protected Map<String, NdbCmd<C>> commands = new HashMap<>();

    public NdbSupportBase(String dbType, Class<C> configClass, NApplicationContext appContext) {
        this.dbType = dbType;
        this.configClass = configClass;
        this.appContext = appContext;
        this.sharedConfigFolder = appContext.getVersionFolder(NStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT)
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

    public NApplicationContext getAppContext() {
        return appContext;
    }

    @Override
    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NArg a;
        commandLine.setCommandName("ndb "+dbType);
        while (commandLine.hasNext()) {
            boolean ok = false;
            for (NdbCmd<C> cc : commands.values()) {
                if (commandLine.withNextBoolean((value, arg, session) -> {
                    commandLine.setCommandName(getDbType() + " " + arg.key());
                    cc.run(appContext, commandLine);
                }, cc.getNames())) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                if (runExtraCommand(appContext, commandLine)) {

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

    protected boolean runExtraCommand(NApplicationContext appContext, NCommandLine commandLine) {
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
        NElements json = NElements.of(appContext.getSession()).setNtf(false).json();
        return NOptional.ofNamed(json.parse(file, configClass), "config " + name);
    }


    protected boolean fillExtraOption(NCommandLine cmdLine, C options) {
        return false;
    }


    protected boolean fillOption(NCommandLine cmdLine, C options) {
        NSession session = appContext.getSession();
        NArg a;
        if ((a = cmdLine.nextString("--name").orNull()) != null) {
            options.setName(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("-h", "--host").orNull()) != null) {
            options.setHost(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("-p", "--port").orNull()) != null) {
            options.setPort(a.getValue().asInt().get(session));
            return true;
        } else if ((a = cmdLine.nextString("-n", "--dbname").orNull()) != null) {
            options.setDatabaseName(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("-u", "--user").orNull()) != null) {
            options.setUser(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("-P", "--password").orNull()) != null) {
            options.setPassword(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("--remote-server").orNull()) != null) {
            options.setRemoteServer(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("--remote-user").orNull()) != null) {
            options.setRemoteUser(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("--remote-temp-folder").orNull()) != null) {
            options.setRemoteTempFolder(a.getStringValue().get(session));
            return true;
        } else if (fillExtraOption(cmdLine, options)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean fillAddConfigOption(NCommandLine commandLine) {
        return false;
    }

    protected boolean fillRemoveConfigOption(NCommandLine commandLine) {
        return false;
    }

    protected boolean fillUpdateConfigOption(NCommandLine commandLine) {
        return false;
    }


    public NPath getRemoteTempFolder(C options, NSession session) {
        String remoteTempFolder = options.getRemoteTempFolder();
        if (NBlankable.isBlank(remoteTempFolder)) {
            return NPath.of("/home/" + options.getRemoteUser(), session);
        } else {
            remoteTempFolder = remoteTempFolder.trim();
            return NPath.of("/home/" + options.getRemoteUser(), session).resolve(remoteTempFolder);
        }
    }


    protected void addConfig(NCommandLine commandLine) {
        C options = createConfigInstance();
        NRef<Boolean> update = NRef.of(false);
        while (commandLine.hasNext()) {
            if (fillOption(commandLine, options)) {
                //
            } else if (
                    commandLine.withNextBoolean((v, a, s) -> {
                        update.set(v);
                    }, "--update")
            ) {

            } else if (fillAddConfigOption(commandLine)) {

            } else if (appContext.configureFirst(commandLine)) {

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

    public enum DumpRestoreMode {
        FILE,
        FOLDER
    }
}
