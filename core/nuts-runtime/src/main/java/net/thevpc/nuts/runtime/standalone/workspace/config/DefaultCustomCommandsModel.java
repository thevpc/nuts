package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.CommandNutsWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ConfigNutsWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.DefaultNutsWorkspaceCustomCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyles;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.util.*;
import java.util.logging.Level;

public class DefaultCustomCommandsModel {

    private final ConfigNutsWorkspaceCommandFactory defaultCommandFactory;
    private final List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    public NutsLogger LOG;
    private NutsWorkspace workspace;

    public DefaultCustomCommandsModel(NutsWorkspace ws) {
        this.workspace = ws;
        defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(ws);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultCustomCommandsModel.class,session);
        }
        return LOG;
    }

    public void addFactory(NutsCommandFactoryConfig commandFactoryConfig, NutsSession session) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid WorkspaceCommandFactory %s", commandFactoryConfig));
        }
        for (NutsWorkspaceCommandFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("factory already registered : %s",factory.getFactoryId()));
            }
        }
        NutsWorkspaceCommandFactory f = null;
        if (NutsBlankable.isBlank(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
            f = new CommandNutsWorkspaceCommandFactory(session);
        }
        if (f != null) {
            f.configure(commandFactoryConfig);
            commandFactories.add(f);
        }
        Collections.sort(commandFactories, new Comparator<NutsWorkspaceCommandFactory>() {
            @Override
            public int compare(NutsWorkspaceCommandFactory o1, NutsWorkspaceCommandFactory o2) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            }
        });
        List<NutsCommandFactoryConfig> commandFactories = getStoreModelMain().getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            getStoreModelMain().setCommandFactories(commandFactories);
        }
        NutsCommandFactoryConfig oldCommandFactory = null;
        for (NutsCommandFactoryConfig commandFactory : commandFactories) {
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
        NutsWorkspaceConfigManagerExt.of(session.config())
                .getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
    }

    public boolean removeFactoryIfExists(String factoryId, NutsSession session) {
        return removeFactory(factoryId, session,false);
    }

    public void removeFactory(String factoryId, NutsSession session) {
        removeFactory(factoryId, session,true);
    }

    public boolean commandFactoryExists(String factoryId, NutsSession session) {
        if (factoryId == null || factoryId.isEmpty()) {
            return false;
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                return true;
            }
        }
        List<NutsCommandFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NutsCommandFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NutsCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeFactory(String factoryId, NutsSession session,boolean error) {
//        options = CoreNutsUtils.validate(options, workspace);
        if (factoryId == null || factoryId.isEmpty()) {
            if(!error){
                return false;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid WorkspaceCommandFactory %s", factoryId));
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                NutsWorkspaceConfigManagerExt.of(session.config())
                        .getModel()
                        .fireConfigurationChanged("command", session, ConfigEventType.MAIN);
                break;
            }
        }
        List<NutsCommandFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NutsCommandFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NutsCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    NutsWorkspaceConfigManagerExt.of(session.config()).getModel()
                            .fireConfigurationChanged("command", session, ConfigEventType.MAIN);
                    break;
                }
            }
        }
        if (removeMe == null && removeMeConfig == null) {
            if(!error){
                return false;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("command factory does not exists %s", factoryId));
        }
        return true;
    }

    public boolean add(NutsCommandConfig command, NutsSession session) {
        if (command == null
                || NutsBlankable.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().size() == 0) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid command %s", (command == null ? "<NULL>" : command.getName()))
            );
        }
        NutsCommandConfig oldCommand =defaultCommandFactory.findCommand(command.getName(), session);
                find(command.getName(), session);
        if (oldCommand != null) {
            if(oldCommand.equals(command)){
                return false;
            }
            if (!session.getTerminal().ask()
                    .resetLine()
                    .setDefaultValue(false)
                    .forBoolean("override existing command %s ?",
                            NutsTexts.of(session).ofStyled(
                                    command.getName(), NutsTextStyle.primary1()
                            )
                    ).getBooleanValue()) {
                update(command,session);
                return true;
            }else{
                return false;
            }
        }else{
            defaultCommandFactory.installCommand(command, session);
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.getTerminal().out();
                NutsTexts text = NutsTexts.of(session);
                out.printf("%s command %s%n",
                        text.ofStyled("install", NutsTextStyle.success()),
                        text.ofStyled(command.getName(), NutsTextStyle.primary3()));
            }
            return true;
        }
    }

    public boolean update(NutsCommandConfig command, NutsSession session) {
        if (command == null
                || NutsBlankable.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().size() == 0) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid command %s", (command == null ? "<NULL>" : command.getName()))
            );
        }
        NutsCommandConfig oldCommand = defaultCommandFactory.findCommand(command.getName(), session);
        if (oldCommand != null) {
            if(oldCommand.equals(command)){
                return false;
            }
            defaultCommandFactory.uninstallCommand(command.getName(), session);
            defaultCommandFactory.installCommand(command, session);
            if (session.isPlainTrace()) {
                NutsPrintStream out = session.getTerminal().out();
                NutsTexts text = NutsTexts.of(session);
                out.printf("%s command %s%n",
                        text.ofStyled("update ", NutsTextStyles.of(NutsTextStyle.success(), NutsTextStyle.underlined())),
                        text.ofStyled(command.getName(), NutsTextStyle.primary3()));
            }
            return true;
        }else{
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("command not found %s", command.getName())
            );
        }
    }

    public void remove(String name, NutsSession session) {
        if (NutsBlankable.isBlank(name)) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid command : %s" + (name == null ? "<NULL>" : name))
            );
        }
//        options = CoreNutsUtils.validate(options, workspace);
//        NutsSession session = session.getSession();
        NutsCommandConfig command = defaultCommandFactory.findCommand(name, session);
        if (command == null) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("command does not exists %s", name)
            );
        }
        defaultCommandFactory.uninstallCommand(name, session);
        if (session.isPlainTrace()) {
            NutsPrintStream out = session.getTerminal().out();
            out.printf("%s command %s%n", "uninstall", NutsTexts.of(session).ofStyled(name, NutsTextStyle.primary3()));
        }
    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNutsWorkspace) workspace).getConfigModel().getStoreModelMain();
    }

    public NutsWorkspaceCustomCommand find(String name, NutsSession session) {
        NutsCommandConfig c = defaultCommandFactory.findCommand(name, session);
        if (c == null) {
            for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
                c = commandFactory.findCommand(name, session);
                if (c != null) {
                    break;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return toDefaultNutsWorkspaceCommand(c, session);
    }

    public List<NutsWorkspaceCustomCommand> findAll(NutsSession session) {
        HashMap<String, NutsWorkspaceCustomCommand> all = new HashMap<>();
        for (NutsCommandConfig command : defaultCommandFactory.findCommands(session)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command, session));
        }
        for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
            for (NutsCommandConfig command : commandFactory.findCommands(session)) {
                if (!all.containsKey(command.getName())) {
                    all.put(command.getName(), toDefaultNutsWorkspaceCommand(command, session));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    public List<NutsWorkspaceCustomCommand> findByOwner(NutsId id, NutsSession session) {
        HashMap<String, NutsWorkspaceCustomCommand> all = new HashMap<>();
        for (NutsCommandConfig command : defaultCommandFactory.findCommands(id, session)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command, session));
        }
        return new ArrayList<>(all.values());
    }

    private NutsWorkspaceCustomCommand toDefaultNutsWorkspaceCommand(NutsCommandConfig c, NutsSession session) {
        if (c.getCommand() == null || c.getCommand().size() == 0) {

            _LOGOP(session).level(Level.WARNING).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.jstyle("invalid command definition ''{0}''. Missing command . Ignored", c.getName()));
            return null;
        }
//        if (c.getOwner() == null) {
//            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
//            return null;
//        }
        return new DefaultNutsWorkspaceCustomCommand(workspace)
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setOwner(c.getOwner())
                .setExecutorOptions(c.getExecutorOptions())
                .setName(c.getName())
                .setHelpCommand(c.getHelpCommand())
                .setHelpText(c.getHelpText());
    }

    public NutsCommandFactoryConfig[] getFactories(NutsSession session) {
        if (getStoreModelMain().getCommandFactories() != null) {
            return getStoreModelMain().getCommandFactories().toArray(new NutsCommandFactoryConfig[0]);
        }
        return new NutsCommandFactoryConfig[0];
    }

    public NutsWorkspaceCustomCommand find(String name, NutsId forId, NutsId forOwner, NutsSession session) {
        NutsWorkspaceCustomCommand a = find(name, session);
        if (a != null && a.getCommand() != null && a.getCommand().size() > 0) {
            NutsId i = NutsId.of(a.getCommand().get(0)).orNull();
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

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

}
