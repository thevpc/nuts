package net.thevpc.nuts.toolbox.ndb.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.*;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.DbUrlString;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.*;

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
        this.sharedConfigFolder = NApp.of().getVersionFolder(NStoreType.CONF, NMySqlConfigVersions.CURRENT)
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
    NWorkspace getWorkspace(){
        return session.getWorkspace();
    }
    @Override
    public void run(NCmdLine cmdLine) {
        NArg a;
        cmdLine.setCommandName("ndb " + dbType);
        while (cmdLine.hasNext()) {
            boolean ok = false;
            for (NdbCmd<C> cc : commands.values()) {
                if (cmdLine.withNextFlag((value, arg) -> {
                    cmdLine.setCommandName(getDbType() + " " + arg.key());
                    cc.run(cmdLine);
                }, cc.getNames())) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                if (runExtraCommand(session, cmdLine)) {

                } else {
                    NSession session = NSession.of().get();
                    session.configureLast(cmdLine);
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
        NElements json = NElements.of().setNtf(false).json();
        return NOptional.ofNamed(json.parse(file, configClass), "config " + name);
    }


    protected boolean fillExtraOption(NCmdLine cmdLine, C options) {
        return false;
    }


    protected boolean fillOption(NCmdLine cmdLine, C options) {
        NSession session = getSession();
        NArg a;
        if ((a = cmdLine.nextEntry("--name").orNull()) != null) {
            options.setName(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("-h", "--host").orNull()) != null) {
            options.setHost(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("-p", "--port").orNull()) != null) {
            options.setPort(a.getValue().asInt().get());
            return true;
        } else if ((a = cmdLine.nextEntry("-n", "--dbname").orNull()) != null) {
            options.setDatabaseName(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("-u", "--user").orNull()) != null) {
            options.setUser(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("-P", "--password").orNull()) != null) {
            options.setPassword(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("--db").orNull()) != null) {
            String db = a.getStringValue().get();
            DbUrlString dbUrlString = DbUrlString.parse(db).get();
            if(dbUrlString.getSsh()!=null) {
                options.setRemoteUser(dbUrlString.getSsh().getUser());
                options.setRemotePassword(dbUrlString.getSsh().getPassword());
                options.setRemoteServer(dbUrlString.getSsh().getHost());
                options.setRemotePort(NLiteral.of(dbUrlString.getSsh().getPort()).asInt().orNull());
            }else{
                options.setRemoteUser(null);
                options.setRemotePassword(null);
                options.setRemoteServer(null);
                options.setRemotePort(null);
            }
            if(dbUrlString.getDb()!=null) {
                options.setUser(dbUrlString.getDb().getUser());
                options.setPassword(dbUrlString.getDb().getPassword());
                options.setHost(dbUrlString.getDb().getHost());
                options.setPort(NLiteral.of(dbUrlString.getDb().getPort()).asInt().orNull());
                options.setDatabaseName(dbUrlString.getDb().getPath());
            }else{
                options.setUser(null);
                options.setPassword(null);
                options.setHost(null);
                options.setPort(null);
                options.setDatabaseName(null);
            }
            return true;
        } else if ((a = cmdLine.nextEntry("--remote-server").orNull()) != null) {
            options.setRemoteServer(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("--remote-user").orNull()) != null) {
            options.setRemoteUser(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("--remote-temp-folder").orNull()) != null) {
            options.setRemoteTempFolder(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("--ssh").orNull()) != null) {
            String ssh = a.getStringValue().get();
            NConnexionString dbUrlString = NConnexionString.of("ssh://"+ssh).get();
            options.setRemoteUser(dbUrlString.getUser());
            options.setRemotePassword(dbUrlString.getPassword());
            options.setRemoteServer(dbUrlString.getHost());
            options.setRemotePort(NLiteral.of(dbUrlString.getPort()).asInt().orNull());
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


    public NPath getRemoteTempFolder(C options) {
        String remoteTempFolder = options.getRemoteTempFolder();
        if (NBlankable.isBlank(remoteTempFolder)) {
            return NPath.of(NdbUtils.getDefaultUserHome(options.getRemoteUser()));
        } else {
            remoteTempFolder = remoteTempFolder.trim();
            return NPath.of(NdbUtils.getDefaultUserHome(options.getRemoteUser())).resolve(remoteTempFolder);
        }
    }


    protected void addConfig(NCmdLine commandLine) {
        C options = createConfigInstance();
        NRef<Boolean> update = NRef.of(false);
        while (commandLine.hasNext()) {
            if (fillOption(commandLine, options)) {
                //
            } else if (
                    commandLine.withNextFlag((v, a) -> {
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
        NElements json = NElements.of().setNtf(false).json();
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


    public NExecCmd sysSsh(C options) {
        return sysCmd().addCommand("ssh", options.getRemoteUser() + "@" + options.getRemoteServer());
    }

    public NExecCmd run(NExecCmd cmd) {
        NSession session = getWorkspace().currentSession();
        session.out().println(cmd);
        cmd.run();
        return cmd;
    }

    public NExecCmd sysCmd() {
        return NExecCmd.of()
                .failFast()
                .system()
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

    public DumpRestoreMode getDumpRestoreMode(C options) {
        return DumpRestoreMode.FILE;
    }

    public <C extends NdbConfig> String getDumpExt(C options) {
        return ".sql";
    }

    public abstract CmdRedirect createDumpCommand(NPath remoteSql, C options);

    public abstract CmdRedirect createRestoreCommand(NPath remoteSql, C options);

    public void prepareDump(C options) {

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
