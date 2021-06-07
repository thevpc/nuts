package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.CommandNutsWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.standalone.wscommands.ConfigNutsWorkspaceCommandFactory;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.wscommands.DefaultNutsWorkspaceCommandAlias;

public class DefaultAliasModel {

    private NutsWorkspace workspace;
    private final ConfigNutsWorkspaceCommandFactory defaultCommandFactory;
    private final List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    public NutsLogger LOG;

    public DefaultAliasModel(NutsWorkspace ws) {
        this.workspace = ws;
        defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(ws);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.workspace.log().setSession(session).of(DefaultAliasModel.class);
        }
        return LOG;
    }

    public void addFactory(NutsCommandAliasFactoryConfig commandFactoryConfig, NutsSession session) {
//        session = CoreNutsUtils.validate(session, workspace);
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new NutsIllegalArgumentException(session, "invalid WorkspaceCommandFactory " + commandFactoryConfig);
        }
        for (NutsWorkspaceCommandFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new IllegalArgumentException();
            }
        }
        NutsWorkspaceCommandFactory f = null;
        if (CoreStringUtils.isBlank(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
            f = new CommandNutsWorkspaceCommandFactory(workspace);
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
        List<NutsCommandAliasFactoryConfig> commandFactories = getStoreModelMain().getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            getStoreModelMain().setCommandFactories(commandFactories);
        }
        NutsCommandAliasFactoryConfig oldCommandFactory = null;
        for (NutsCommandAliasFactoryConfig commandFactory : commandFactories) {
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
        NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config())
                .getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
    }

    public boolean removeFactory(String factoryId, NutsSession session) {
//        options = CoreNutsUtils.validate(options, workspace);
        if (factoryId == null || factoryId.isEmpty()) {
            throw new NutsIllegalArgumentException(session, "invalid WorkspaceCommandFactory " + factoryId);
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsCommandAliasFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext();) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config())
                        .getModel()
                        .fireConfigurationChanged("command", session, ConfigEventType.MAIN);
                break;
            }
        }
        List<NutsCommandAliasFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NutsCommandAliasFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext();) {
                NutsCommandAliasFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config()).getModel()
                            .fireConfigurationChanged("command", session, ConfigEventType.MAIN);
                    break;
                }
            }
        }
        if (removeMe == null && removeMeConfig == null) {
            throw new NutsIllegalArgumentException(session, "command factory does not exists " + factoryId);
        }
        return true;
    }

    public boolean add(NutsCommandAliasConfig command, NutsSession session) {
        if (command == null
                || CoreStringUtils.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().length == 0) {
            throw new NutsIllegalArgumentException(session, "invalid command alias " + (command == null ? "<NULL>" : command.getName()));
        }
        boolean forced = false;
        if (defaultCommandFactory.findCommand(command.getName(), session) != null) {
            if (session.isYes()) {
                forced = true;
                remove(command.getName(),
                        session.copy().setTrace(false)
                );
            } else {
                throw new NutsIllegalArgumentException(session, "command alias already exists " + command.getName());
            }
        }
        defaultCommandFactory.installCommand(command, session);
        if (session.isPlainTrace()) {
            NutsPrintStream out = session.getTerminal().out();
            NutsTextManager text = session.getWorkspace().text();
            if (forced) {
                out.printf("[%s] command alias %s%n", "re-install", text.forStyled(command.getName(), NutsTextStyle.primary(3)));
            } else {
                out.printf("[%s] command alias %s%n", "install", text.forStyled(command.getName(), NutsTextStyle.primary(3)));
            }
        }
        return forced;
    }

    public boolean remove(String name, NutsSession session) {
        if (CoreStringUtils.isBlank(name)) {
            throw new NutsIllegalArgumentException(session, "invalid command alias " + (name == null ? "<NULL>" : name));
        }
//        options = CoreNutsUtils.validate(options, workspace);
//        NutsSession session = session.getSession();
        NutsCommandAliasConfig command = defaultCommandFactory.findCommand(name, session);
        if (command == null) {
            throw new NutsIllegalArgumentException(session, "command alias does not exists " + name);
        }
        defaultCommandFactory.uninstallCommand(name, session);
        if (session.isPlainTrace()) {
            NutsPrintStream out = session.getTerminal().out();
            out.printf("[%s] command alias %s%n", "uninstall", session.getWorkspace().text().forStyled(name, NutsTextStyle.primary(3)));
        }
        return true;
    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNutsWorkspace) workspace).getConfigModel().getStoreModelMain();
    }

    public NutsWorkspaceCommandAlias find(String name, NutsSession session) {
        NutsCommandAliasConfig c = defaultCommandFactory.findCommand(name, session);
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

    public List<NutsWorkspaceCommandAlias> findAll(NutsSession session) {
        HashMap<String, NutsWorkspaceCommandAlias> all = new HashMap<>();
        for (NutsCommandAliasConfig command : defaultCommandFactory.findCommands(session)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command, session));
        }
        for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
            for (NutsCommandAliasConfig command : commandFactory.findCommands(session)) {
                if (!all.containsKey(command.getName())) {
                    all.put(command.getName(), toDefaultNutsWorkspaceCommand(command, session));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    public List<NutsWorkspaceCommandAlias> findByOwner(NutsId id, NutsSession session) {
        HashMap<String, NutsWorkspaceCommandAlias> all = new HashMap<>();
        for (NutsCommandAliasConfig command : defaultCommandFactory.findCommands(id, session)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command, session));
        }
        return new ArrayList<>(all.values());
    }

    private NutsWorkspaceCommandAlias toDefaultNutsWorkspaceCommand(NutsCommandAliasConfig c, NutsSession session) {
        if (c.getCommand() == null || c.getCommand().length == 0) {

            _LOGOP(session).level(Level.WARNING).verb(NutsLogVerb.FAIL).log("invalid alias definition ''{0}''. Missing alias . Ignored", c.getName());
            return null;
        }
//        if (c.getOwner() == null) {
//            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
//            return null;
//        }
        return new DefaultNutsWorkspaceCommandAlias(workspace)
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setOwner(c.getOwner())
                .setExecutorOptions(c.getExecutorOptions())
                .setName(c.getName())
                .setHelpCommand(c.getHelpCommand())
                .setHelpText(c.getHelpText());
    }

    public NutsCommandAliasFactoryConfig[] getFactories(NutsSession session) {
        if (getStoreModelMain().getCommandFactories() != null) {
            return getStoreModelMain().getCommandFactories().toArray(new NutsCommandAliasFactoryConfig[0]);
        }
        return new NutsCommandAliasFactoryConfig[0];
    }

    public NutsWorkspaceCommandAlias find(String name, NutsId forId, NutsId forOwner, NutsSession session) {
        NutsWorkspaceCommandAlias a = find(name, session);
        if (a != null && a.getCommand() != null && a.getCommand().length > 0) {
            NutsId i = session.getWorkspace().id().parser().parse(a.getCommand()[0]);
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
