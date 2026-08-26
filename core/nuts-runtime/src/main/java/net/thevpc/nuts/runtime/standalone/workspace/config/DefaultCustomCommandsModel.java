package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NCommandConfig;
import net.thevpc.nuts.command.NCommandFactoryConfig;
import net.thevpc.nuts.command.NCustomCmd;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NWorkspaceCmdFactory;
import net.thevpc.nuts.io.NIn;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.NInternalCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.CommandNWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ConfigNWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.DefaultNCustomCommand;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DefaultCustomCommandsModel {

    private final ConfigNWorkspaceCommandFactory defaultCommandFactory;
    private final List<NWorkspaceCmdFactory> commandFactories = new ArrayList<>();
    private NWorkspace workspace;
    private Map<String, NInternalCommand> internalCommands;

    public DefaultCustomCommandsModel(NWorkspace ws) {
        this.workspace = ws;
        defaultCommandFactory = new ConfigNWorkspaceCommandFactory();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultCustomCommandsModel.class);
    }

    public void addFactory(NCommandFactoryConfig commandFactoryConfig) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (commandFactoryConfig == null || commandFactoryConfig.factoryId() == null || commandFactoryConfig.factoryId().isEmpty() || !commandFactoryConfig.factoryId().trim().equals(commandFactoryConfig.factoryId())) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid WorkspaceCommandFactory %s", commandFactoryConfig));
        }
        for (NWorkspaceCmdFactory factory : commandFactories) {
            if (commandFactoryConfig.factoryId().equals(factory.factoryId())) {
                throw new NIllegalArgumentException(NMsg.ofC("factory already registered : %s", factory.factoryId()));
            }
        }
        NWorkspaceCmdFactory f = null;
        if (NBlankable.isBlank(commandFactoryConfig.factoryType()) || "command".equals(commandFactoryConfig.factoryType().trim())) {
            f = new CommandNWorkspaceCommandFactory();
        }
        if (f != null) {
            f.configure(commandFactoryConfig);
            commandFactories.add(f);
        }
        Collections.sort(commandFactories, new Comparator<NWorkspaceCmdFactory>() {
            @Override
            public int compare(NWorkspaceCmdFactory o1, NWorkspaceCmdFactory o2) {
                return Integer.compare(o2.priority(), o1.priority());
            }
        });
        List<NCommandFactoryConfig> commandFactories = getStoreModelMain().getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            getStoreModelMain().setCommandFactories(commandFactories);
        }
        NCommandFactoryConfig oldCommandFactory = null;
        for (NCommandFactoryConfig commandFactory : commandFactories) {
            if (f == null || commandFactory.factoryId().equals(f.factoryId())) {
                oldCommandFactory = commandFactory;
            }
        }
        if (oldCommandFactory == null) {
            commandFactories.add(commandFactoryConfig);
        } else if (oldCommandFactory != commandFactoryConfig) {
            oldCommandFactory.factoryId(commandFactoryConfig.factoryId());
            oldCommandFactory.factoryType(commandFactoryConfig.factoryType());
            oldCommandFactory.parameters(commandFactoryConfig.parameters() == null ? null : new LinkedHashMap<>(commandFactoryConfig.parameters()));
            oldCommandFactory.priority(commandFactoryConfig.priority());
        }
        NWorkspaceExt.of(workspace)
                .getConfigModel().fireConfigurationChanged("command", ConfigEventType.MAIN);
    }

    public boolean removeFactoryIfExists(String factoryId) {
        return removeFactory(factoryId, false);
    }

    public void removeFactory(String factoryId) {
        removeFactory(factoryId, true);
    }

    public boolean commandFactoryExists(String factoryId) {
        if (factoryId == null || factoryId.isEmpty()) {
            return false;
        }
        NWorkspaceCmdFactory removeMe = null;
        NCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NWorkspaceCmdFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NWorkspaceCmdFactory factory = iterator.next();
            if (factoryId.equals(factory.factoryId())) {
                return true;
            }
        }
        List<NCommandFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NCommandFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.factoryId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeFactory(String factoryId, boolean error) {
//        options = CoreNutsUtils.validate(options, workspace);
        if (factoryId == null || factoryId.isEmpty()) {
            if (!error) {
                return false;
            }
            throw new NIllegalArgumentException(NMsg.ofC("invalid WorkspaceCommandFactory %s", factoryId));
        }
        NWorkspaceCmdFactory removeMe = null;
        NCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NWorkspaceCmdFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NWorkspaceCmdFactory factory = iterator.next();
            if (factoryId.equals(factory.factoryId())) {
                removeMe = factory;
                iterator.remove();
                NWorkspaceExt.of(workspace)
                        .getConfigModel()
                        .fireConfigurationChanged("command", ConfigEventType.MAIN);
                break;
            }
        }
        List<NCommandFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NCommandFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.factoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    NWorkspaceExt.of(workspace).getConfigModel()
                            .fireConfigurationChanged("command", ConfigEventType.MAIN);
                    break;
                }
            }
        }
        if (removeMe == null && removeMeConfig == null) {
            if (!error) {
                return false;
            }
            throw new NIllegalArgumentException(NMsg.ofC("command factory does not exists %s", factoryId));
        }
        return true;
    }

    public boolean add(NCommandConfig command) {
        if (command == null
                || NBlankable.isBlank(command.name())
                || command.name().contains(" ") || command.name().contains(".")
                || command.name().contains("/") || command.name().contains("\\")
                || command.command() == null
                || command.command().size() == 0) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("invalid command %s", (command == null ? "<NULL>" : command.name()))
            );
        }
        NCommandConfig oldCommand = defaultCommandFactory.findCommand(command.name());
        find(command.name());
        if (oldCommand != null) {
            if (oldCommand.equals(command)) {
                return false;
            }
            if (!NIn.ask()
                    .defaultValue(false)
                    .forBoolean(NMsg.ofC("override existing command %s ?",
                            NText.ofStyled(
                                    command.name(), NTextStyle.primary1()
                            ))
                    ).booleanValue()) {
                update(command);
                return true;
            } else {
                return false;
            }
        } else {
            defaultCommandFactory.installCommand(command);
//            NSession session = workspace.currentSession();
//            if (session.isPlainTrace()) {
//                NPrintStream out = session.terminal().out();
//                NTexts text = NTexts.of();
//                out.println(NMsg.ofC("%s command %s",
//                        text.ofStyled("install", NTextStyle.success()),
//                        text.ofStyled(command.name(), NTextStyle.primary3())));
//            }
            return true;
        }
    }

    public boolean update(NCommandConfig command) {
        if (command == null
                || NBlankable.isBlank(command.name())
                || command.name().contains(" ") || command.name().contains(".")
                || command.name().contains("/") || command.name().contains("\\")
                || command.command() == null
                || command.command().isEmpty()) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("invalid command %s", (command == null ? "<NULL>" : command.name()))
            );
        }
        NCommandConfig oldCommand = defaultCommandFactory.findCommand(command.name());
        if (oldCommand != null) {
            if (oldCommand.equals(command)) {
                return false;
            }
            defaultCommandFactory.uninstallCommand(command.name());
            defaultCommandFactory.installCommand(command);
//            NSession session = workspace.currentSession();
//            if (session.isPlainTrace()) {
//                NPrintStream out = session.terminal().out();
//                NTexts text = NTexts.of();
//                out.println(NMsg.ofC("%s command %s",
//                        text.ofStyled("update ", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined())),
//                        text.ofStyled(command.name(), NTextStyle.primary3())));
//            }
            return true;
        } else {
            throw new NIllegalArgumentException(
                    NMsg.ofC("command not found %s", command.name())
            );
        }
    }

    public void remove(String name) {
        if (NBlankable.isBlank(name)) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("invalid command : %s", (name == null ? "<NULL>" : name))
            );
        }
//        options = CoreNutsUtils.validate(options, workspace);
        NCommandConfig command = defaultCommandFactory.findCommand(name);
        if (command == null) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("command does not exists %s", name)
            );
        }
        defaultCommandFactory.uninstallCommand(name);
        NSession session = workspace.currentSession();
        if (session.isPlainTrace()) {
            NPrintStream out = session.terminal().out();
            out.println(NMsg.ofC("%s command %s", "uninstall", NText.ofStyled(name, NTextStyle.primary3())));
        }
    }

    NWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNWorkspace) workspace).getConfigModel().getStoreModelMain();
    }

    public NCustomCmd find(String name) {
        NCommandConfig c = defaultCommandFactory.findCommand(name);
        if (c == null) {
            for (NWorkspaceCmdFactory commandFactory : commandFactories) {
                c = commandFactory.findCommand(name);
                if (c != null) {
                    break;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return toDefaultNWorkspaceCommand(c);
    }

    public List<NCustomCmd> findAll() {
        HashMap<String, NCustomCmd> all = new HashMap<>();
        for (NCommandConfig command : defaultCommandFactory.findCommands()) {
            all.put(command.name(), toDefaultNWorkspaceCommand(command));
        }
        for (NWorkspaceCmdFactory commandFactory : commandFactories) {
            for (NCommandConfig command : commandFactory.findCommands()) {
                if (!all.containsKey(command.name())) {
                    all.put(command.name(), toDefaultNWorkspaceCommand(command));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    public List<NCustomCmd> findByOwner(NId id) {
        HashMap<String, NCustomCmd> all = new HashMap<>();
        for (NCommandConfig command : defaultCommandFactory.findCommands(id)) {
            all.put(command.name(), toDefaultNWorkspaceCommand(command));
        }
        return new ArrayList<>(all.values());
    }

    private NCustomCmd toDefaultNWorkspaceCommand(NCommandConfig c) {
        if (c.command() == null || c.command().isEmpty()) {

            _LOG()
                    .log(NMsg.ofC("invalid command definition '%s'. Missing command . Ignored", c.name())
                            .withLevel(Level.WARNING).withIntent(NMsgIntent.FAIL)
                    );
            return null;
        }
//        if (c.getOwner() == null) {
//            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
//            return null;
//        }
        return new DefaultNCustomCommand()
                .setCommand(c.command())
                .setFactoryId(c.factoryId())
                .setOwner(c.owner())
                .setExecutorOptions(c.executorOptions())
                .setName(c.name())
                .setHelpCommand(c.helpCommand())
                .setHelpText(c.helpText());
    }

    public NCommandFactoryConfig[] getFactories() {
        if (getStoreModelMain().getCommandFactories() != null) {
            return getStoreModelMain().getCommandFactories().toArray(new NCommandFactoryConfig[0]);
        }
        return new NCommandFactoryConfig[0];
    }

    public NCustomCmd find(String name, NId forId, NId forOwner) {
        NCustomCmd a = find(name);
        if (a != null && a.command() != null && a.command().size() > 0) {
            NId i = NId.get(a.command().get(0)).orNull();
            if (i != null
                    && (forId == null
                    || i.shortName().equals(forId.artifactId())
                    || i.shortName().equals(forId.shortName()))
                    && (forOwner == null || a.owner() != null && a.owner().shortName().equals(forOwner.shortName()))) {
                return a;
            }
        }
        return null;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public Map<String, NInternalCommand> getInternalCommands() {
        if (internalCommands == null) {
            List<NInternalCommand> all = workspace.extensions().createAllSupported(NInternalCommand.class, null);
            internalCommands = all.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
        }
        return internalCommands;
    }

}
