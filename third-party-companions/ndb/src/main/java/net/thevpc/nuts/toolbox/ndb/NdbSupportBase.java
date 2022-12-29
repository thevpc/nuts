package net.thevpc.nuts.toolbox.ndb;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

public abstract class NdbSupportBase<C extends NdbConfig> implements NdbSupport {
    protected Class<C> configClass;
    protected NApplicationContext appContext;
    protected String dbType;
    protected NPath sharedConfigFolder;

    public NdbSupportBase(String dbType, Class<C> configClass, NApplicationContext appContext) {
        this.configClass = configClass;
        this.appContext = appContext;
        sharedConfigFolder = appContext.getVersionFolder(NStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT)
                .resolve(dbType);
    }

    @Override
    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NArgument a;
        commandLine.setCommandName(dbType);
        while (commandLine.hasNext()) {
            if (commandLine.withNextBoolean((value, arg, session) -> {
                addConfig(commandLine);
            }, "add")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                updateConfig(commandLine);
            }, "update")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                removeConfig(commandLine);
            }, "remove")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                showTables(commandLine, session);
            }, "show-tables")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                dump(commandLine, session);
            }, "dump")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                restore(commandLine, session);
            }, "restore")) {
            } else if (runExtraCommand(appContext, commandLine)) {

            } else {
                commandLine.getSession().configureLast(commandLine);
            }
        }
    }

    protected abstract void showTables(NCommandLine commandLine, NSession session);

    protected abstract void dump(NCommandLine commandLine, NSession session);

    protected abstract void restore(NCommandLine commandLine, NSession session);


    protected boolean runExtraCommand(NApplicationContext appContext, NCommandLine commandLine) {
        return false;
    }

    protected C createConfigInstance() {
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

    protected NOptional<C> loadConfig(AtName name) {
        NPath file = sharedConfigFolder.resolve(asFullName(name) + NdbUtils.SERVER_CONFIG_EXT);
        if (!file.exists()) {
            return NOptional.ofNamedEmpty("config " + name);
        }
        NElements json = NElements.of(appContext.getSession()).setNtf(false).json();
        return NOptional.ofNamed(json.parse(file, configClass), "config " + name);
    }

    protected void removeConfig(NCommandLine commandLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NSession session = commandLine.getSession();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                name.set(new AtName(a.getStringValue().get(session)));
                            } else {
                                commandLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    default: {
                        session.configureLast(commandLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(commandLine.next().get(session).asString().get(session)));
                } else {
                    commandLine.throwUnexpectedArgument();
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        removeConfig(name.get());
    }

    private void removeConfig(AtName name) {
        NPath file = sharedConfigFolder.resolve(asFullName(name) + NdbUtils.SERVER_CONFIG_EXT);
        if (file.exists()) {
            file.delete();
        }
    }

    protected boolean fillExtraOption(NCommandLine cmdLine, C options) {
        return false;
    }

    protected boolean fillOption(NCommandLine cmdLine, C options) {
        NSession session = appContext.getSession();
        NArgument a;
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


    protected NPath getRemoteTempFolder(C options, NSession session) {
        String remoteTempFolder = options.getRemoteTempFolder();
        if (NBlankable.isBlank(remoteTempFolder)) {
            return NPath.of("/home/" + options.getRemoteUser(), session);
        } else {
            remoteTempFolder = remoteTempFolder.trim();
            return NPath.of("/home/" + options.getRemoteUser(), session).resolve(remoteTempFolder);
        }
    }

    protected boolean isRemoteCommand(C options) {
        return
                !NBlankable.isBlank(options.getRemoteServer())
                        || !NBlankable.isBlank(options.getRemoteUser());
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

    protected void updateConfig(NCommandLine commandLine) {
        C options = createConfigInstance();
        while (commandLine.hasNext()) {
            if (fillOption(commandLine, options)) {
                //
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
        if (!file.exists()) {
            throw new RuntimeException("not found");
        }
        NElements json = NElements.of(commandLine.getSession()).setNtf(false).json();
        C old = json.parse(file, configClass);
        String oldName = old.getName();
        old.setNonNull(options);
        old.setName(oldName);
        json.setValue(options).print(file);
    }


    protected NExecCommand sysSsh(C options, NSession session) {
        return sysCmd(session).addCommand("ssh", options.getRemoteUser() + "@" + options.getRemoteServer());
    }

    protected NExecCommand run(NExecCommand cmd) {
        cmd.getSession().out().printlnf(cmd);
        cmd.run();
        return cmd;
    }

    protected static NExecCommand sysCmd(NSession session) {
        return NExecCommand.of(session)
                .setFailFast(true)
                .setExecutionType(NExecutionType.SYSTEM)
                ;
    }

}
