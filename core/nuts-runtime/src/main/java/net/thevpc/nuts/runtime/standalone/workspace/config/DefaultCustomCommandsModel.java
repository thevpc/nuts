package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.CommandNWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ConfigNWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.DefaultNCustomCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.*;
import java.util.logging.Level;

public class DefaultCustomCommandsModel {

    private final ConfigNWorkspaceCommandFactory defaultCommandFactory;
    private final List<NWorkspaceCmdFactory> commandFactories = new ArrayList<>();
    public NLog LOG;
    private NWorkspace workspace;

    public DefaultCustomCommandsModel(NWorkspace ws) {
        this.workspace = ws;
        defaultCommandFactory = new ConfigNWorkspaceCommandFactory(ws);
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultCustomCommandsModel.class, session);
        }
        return LOG;
    }

    public void addFactory(NCommandFactoryConfig commandFactoryConfig, NSession session) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new NIllegalArgumentException(session, NMsg.ofC("invalid WorkspaceCommandFactory %s", commandFactoryConfig));
        }
        for (NWorkspaceCmdFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new NIllegalArgumentException(session, NMsg.ofC("factory already registered : %s", factory.getFactoryId()));
            }
        }
        NWorkspaceCmdFactory f = null;
        if (NBlankable.isBlank(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
            f = new CommandNWorkspaceCommandFactory(session);
        }
        if (f != null) {
            f.configure(commandFactoryConfig);
            commandFactories.add(f);
        }
        Collections.sort(commandFactories, new Comparator<NWorkspaceCmdFactory>() {
            @Override
            public int compare(NWorkspaceCmdFactory o1, NWorkspaceCmdFactory o2) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            }
        });
        List<NCommandFactoryConfig> commandFactories = getStoreModelMain().getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            getStoreModelMain().setCommandFactories(commandFactories);
        }
        NCommandFactoryConfig oldCommandFactory = null;
        for (NCommandFactoryConfig commandFactory : commandFactories) {
            if (f == null || commandFactory.getFactoryId().equals(f.getFactoryId())) {
                oldCommandFactory = commandFactory;
            }
        }
        if (oldCommandFactory == null) {
            commandFactories.add(commandFactoryConfig);
        } else if (oldCommandFactory != commandFactoryConfig) {
            oldCommandFactory.setFactoryId(commandFactoryConfig.getFactoryId());
            oldCommandFactory.setFactoryType(commandFactoryConfig.getFactoryType());
            oldCommandFactory.setParameters(commandFactoryConfig.getParameters() == null ? null : new LinkedHashMap<>(commandFactoryConfig.getParameters()));
            oldCommandFactory.setPriority(commandFactoryConfig.getPriority());
        }
        NConfigsExt.of(NConfigs.of(session))
                .getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
    }

    public boolean removeFactoryIfExists(String factoryId, NSession session) {
        return removeFactory(factoryId, session, false);
    }

    public void removeFactory(String factoryId, NSession session) {
        removeFactory(factoryId, session, true);
    }

    public boolean commandFactoryExists(String factoryId, NSession session) {
        if (factoryId == null || factoryId.isEmpty()) {
            return false;
        }
        NWorkspaceCmdFactory removeMe = null;
        NCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NWorkspaceCmdFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NWorkspaceCmdFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                return true;
            }
        }
        List<NCommandFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NCommandFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeFactory(String factoryId, NSession session, boolean error) {
//        options = CoreNutsUtils.validate(options, workspace);
        if (factoryId == null || factoryId.isEmpty()) {
            if (!error) {
                return false;
            }
            throw new NIllegalArgumentException(session, NMsg.ofC("invalid WorkspaceCommandFactory %s", factoryId));
        }
        NWorkspaceCmdFactory removeMe = null;
        NCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NWorkspaceCmdFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NWorkspaceCmdFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                NConfigsExt.of(NConfigs.of(session))
                        .getModel()
                        .fireConfigurationChanged("command", session, ConfigEventType.MAIN);
                break;
            }
        }
        List<NCommandFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NCommandFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    NConfigsExt.of(NConfigs.of(session)).getModel()
                            .fireConfigurationChanged("command", session, ConfigEventType.MAIN);
                    break;
                }
            }
        }
        if (removeMe == null && removeMeConfig == null) {
            if (!error) {
                return false;
            }
            throw new NIllegalArgumentException(session, NMsg.ofC("command factory does not exists %s", factoryId));
        }
        return true;
    }

    public boolean add(NCommandConfig command, NSession session) {
        if (command == null
                || NBlankable.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().size() == 0) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofC("invalid command %s", (command == null ? "<NULL>" : command.getName()))
            );
        }
        NCommandConfig oldCommand = defaultCommandFactory.findCommand(command.getName(), session);
        find(command.getName(), session);
        if (oldCommand != null) {
            if (oldCommand.equals(command)) {
                return false;
            }
            if (!session.getTerminal().ask()
                    .resetLine()
                    .setDefaultValue(false)
                    .forBoolean(NMsg.ofC("override existing command %s ?",
                            NTexts.of(session).ofStyled(
                                    command.getName(), NTextStyle.primary1()
                            ))
                    ).getBooleanValue()) {
                update(command, session);
                return true;
            } else {
                return false;
            }
        } else {
            defaultCommandFactory.installCommand(command, session);
            if (session.isPlainTrace()) {
                NPrintStream out = session.getTerminal().out();
                NTexts text = NTexts.of(session);
                out.println(NMsg.ofC("%s command %s",
                        text.ofStyled("install", NTextStyle.success()),
                        text.ofStyled(command.getName(), NTextStyle.primary3())));
            }
            return true;
        }
    }

    public boolean update(NCommandConfig command, NSession session) {
        if (command == null
                || NBlankable.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().size() == 0) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofC("invalid command %s", (command == null ? "<NULL>" : command.getName()))
            );
        }
        NCommandConfig oldCommand = defaultCommandFactory.findCommand(command.getName(), session);
        if (oldCommand != null) {
            if (oldCommand.equals(command)) {
                return false;
            }
            defaultCommandFactory.uninstallCommand(command.getName(), session);
            defaultCommandFactory.installCommand(command, session);
            if (session.isPlainTrace()) {
                NPrintStream out = session.getTerminal().out();
                NTexts text = NTexts.of(session);
                out.println(NMsg.ofC("%s command %s",
                        text.ofStyled("update ", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined())),
                        text.ofStyled(command.getName(), NTextStyle.primary3())));
            }
            return true;
        } else {
            throw new NIllegalArgumentException(session,
                    NMsg.ofC("command not found %s", command.getName())
            );
        }
    }

    public void remove(String name, NSession session) {
        if (NBlankable.isBlank(name)) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofC("invalid command : %s", (name == null ? "<NULL>" : name))
            );
        }
//        options = CoreNutsUtils.validate(options, workspace);
        NCommandConfig command = defaultCommandFactory.findCommand(name, session);
        if (command == null) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofC("command does not exists %s", name)
            );
        }
        defaultCommandFactory.uninstallCommand(name, session);
        if (session.isPlainTrace()) {
            NPrintStream out = session.getTerminal().out();
            out.println(NMsg.ofC("%s command %s", "uninstall", NTexts.of(session).ofStyled(name, NTextStyle.primary3())));
        }
    }

    NWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNWorkspace) workspace).getConfigModel().getStoreModelMain();
    }

    public NCustomCmd find(String name, NSession session) {
        NCommandConfig c = defaultCommandFactory.findCommand(name, session);
        if (c == null) {
            for (NWorkspaceCmdFactory commandFactory : commandFactories) {
                c = commandFactory.findCommand(name, session);
                if (c != null) {
                    break;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return toDefaultNWorkspaceCommand(c, session);
    }

    public List<NCustomCmd> findAll(NSession session) {
        HashMap<String, NCustomCmd> all = new HashMap<>();
        for (NCommandConfig command : defaultCommandFactory.findCommands(session)) {
            all.put(command.getName(), toDefaultNWorkspaceCommand(command, session));
        }
        for (NWorkspaceCmdFactory commandFactory : commandFactories) {
            for (NCommandConfig command : commandFactory.findCommands(session)) {
                if (!all.containsKey(command.getName())) {
                    all.put(command.getName(), toDefaultNWorkspaceCommand(command, session));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    public List<NCustomCmd> findByOwner(NId id, NSession session) {
        HashMap<String, NCustomCmd> all = new HashMap<>();
        for (NCommandConfig command : defaultCommandFactory.findCommands(id, session)) {
            all.put(command.getName(), toDefaultNWorkspaceCommand(command, session));
        }
        return new ArrayList<>(all.values());
    }

    private NCustomCmd toDefaultNWorkspaceCommand(NCommandConfig c, NSession session) {
        if (c.getCommand() == null || c.getCommand().size() == 0) {

            _LOGOP(session).level(Level.WARNING).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("invalid command definition ''{0}''. Missing command . Ignored", c.getName()));
            return null;
        }
//        if (c.getOwner() == null) {
//            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
//            return null;
//        }
        return new DefaultNCustomCommand(workspace)
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setOwner(c.getOwner())
                .setExecutorOptions(c.getExecutorOptions())
                .setName(c.getName())
                .setHelpCommand(c.getHelpCommand())
                .setHelpText(c.getHelpText());
    }

    public NCommandFactoryConfig[] getFactories(NSession session) {
        if (getStoreModelMain().getCommandFactories() != null) {
            return getStoreModelMain().getCommandFactories().toArray(new NCommandFactoryConfig[0]);
        }
        return new NCommandFactoryConfig[0];
    }

    public NCustomCmd find(String name, NId forId, NId forOwner, NSession session) {
        NCustomCmd a = find(name, session);
        if (a != null && a.getCommand() != null && a.getCommand().size() > 0) {
            NId i = NId.of(a.getCommand().get(0)).orNull();
            if (i != null
                    && (forId == null
                    || i.getShortName().equals(forId.getArtifactId())
                    || i.getShortName().equals(forId.getShortName()))
                    && (forOwner == null || a.getOwner() != null && a.getOwner().getShortName().equals(forOwner.getShortName()))) {
                return a;
            }
        }
        return null;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

}
