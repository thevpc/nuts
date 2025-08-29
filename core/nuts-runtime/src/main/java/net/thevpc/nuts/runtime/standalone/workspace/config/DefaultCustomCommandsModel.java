package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.NInternalCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.CommandNWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ConfigNWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.DefaultNCustomCommand;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NOptional;

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
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid WorkspaceCommandFactory %s", commandFactoryConfig));
        }
        for (NWorkspaceCmdFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new NIllegalArgumentException(NMsg.ofC("factory already registered : %s", factory.getFactoryId()));
            }
        }
        NWorkspaceCmdFactory f = null;
        if (NBlankable.isBlank(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
            f = new CommandNWorkspaceCommandFactory();
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
            if (factoryId.equals(factory.getFactoryId())) {
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
                if (factoryId.equals(commandFactory.getFactoryId())) {
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
                || NBlankable.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().size() == 0) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("invalid command %s", (command == null ? "<NULL>" : command.getName()))
            );
        }
        NCommandConfig oldCommand = defaultCommandFactory.findCommand(command.getName());
        find(command.getName());
        if (oldCommand != null) {
            if (oldCommand.equals(command)) {
                return false;
            }
            if (!NAsk.of()
                    .setDefaultValue(false)
                    .forBoolean(NMsg.ofC("override existing command %s ?",
                            NText.ofStyled(
                                    command.getName(), NTextStyle.primary1()
                            ))
                    ).getBooleanValue()) {
                update(command);
                return true;
            } else {
                return false;
            }
        } else {
            defaultCommandFactory.installCommand(command);
            NSession session = workspace.currentSession();
            if (session.isPlainTrace()) {
                NPrintStream out = session.getTerminal().out();
                NTexts text = NTexts.of();
                out.resetLine().println(NMsg.ofC("%s command %s",
                        text.ofStyled("install", NTextStyle.success()),
                        text.ofStyled(command.getName(), NTextStyle.primary3())));
            }
            return true;
        }
    }

    public boolean update(NCommandConfig command) {
        if (command == null
                || NBlankable.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().size() == 0) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("invalid command %s", (command == null ? "<NULL>" : command.getName()))
            );
        }
        NCommandConfig oldCommand = defaultCommandFactory.findCommand(command.getName());
        if (oldCommand != null) {
            if (oldCommand.equals(command)) {
                return false;
            }
            defaultCommandFactory.uninstallCommand(command.getName());
            defaultCommandFactory.installCommand(command);
            NSession session = workspace.currentSession();
            if (session.isPlainTrace()) {
                NPrintStream out = session.getTerminal().out();
                NTexts text = NTexts.of();
                out.resetLine().println(NMsg.ofC("%s command %s",
                        text.ofStyled("update ", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined())),
                        text.ofStyled(command.getName(), NTextStyle.primary3())));
            }
            return true;
        } else {
            throw new NIllegalArgumentException(
                    NMsg.ofC("command not found %s", command.getName())
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
            NPrintStream out = session.getTerminal().out();
            out.resetLine().println(NMsg.ofC("%s command %s", "uninstall", NText.ofStyled(name, NTextStyle.primary3())));
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
            all.put(command.getName(), toDefaultNWorkspaceCommand(command));
        }
        for (NWorkspaceCmdFactory commandFactory : commandFactories) {
            for (NCommandConfig command : commandFactory.findCommands()) {
                if (!all.containsKey(command.getName())) {
                    all.put(command.getName(), toDefaultNWorkspaceCommand(command));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    public List<NCustomCmd> findByOwner(NId id) {
        HashMap<String, NCustomCmd> all = new HashMap<>();
        for (NCommandConfig command : defaultCommandFactory.findCommands(id)) {
            all.put(command.getName(), toDefaultNWorkspaceCommand(command));
        }
        return new ArrayList<>(all.values());
    }

    private NCustomCmd toDefaultNWorkspaceCommand(NCommandConfig c) {
        if (c.getCommand() == null || c.getCommand().isEmpty()) {

            _LOG()
                    .log(NMsg.ofC("invalid command definition '%s'. Missing command . Ignored", c.getName())
                            .withLevel(Level.WARNING).withIntent(NMsgIntent.FAIL)
                    );
            return null;
        }
//        if (c.getOwner() == null) {
//            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
//            return null;
//        }
        return new DefaultNCustomCommand()
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setOwner(c.getOwner())
                .setExecutorOptions(c.getExecutorOptions())
                .setName(c.getName())
                .setHelpCommand(c.getHelpCommand())
                .setHelpText(c.getHelpText());
    }

    public NCommandFactoryConfig[] getFactories() {
        if (getStoreModelMain().getCommandFactories() != null) {
            return getStoreModelMain().getCommandFactories().toArray(new NCommandFactoryConfig[0]);
        }
        return new NCommandFactoryConfig[0];
    }

    public NCustomCmd find(String name, NId forId, NId forOwner) {
        NCustomCmd a = find(name);
        if (a != null && a.getCommand() != null && a.getCommand().size() > 0) {
            NId i = NId.get(a.getCommand().get(0)).orNull();
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

    public Map<String, NInternalCommand> getInternalCommands() {
        if (internalCommands == null) {
            List<NInternalCommand> all = workspace.extensions().createComponents(NInternalCommand.class, null);
            internalCommands = all.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
        }
        return internalCommands;
    }

    public NOptional<NExecCmdExtension> getExecCmdExtension(String target) {
        return workspace.extensions().createComponent(NExecCmdExtension.class, target);
    }
}
