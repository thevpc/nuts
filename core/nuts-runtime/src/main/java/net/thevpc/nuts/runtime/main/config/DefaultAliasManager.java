package net.thevpc.nuts.runtime.main.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.wscommands.CommandNutsWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.wscommands.ConfigNutsWorkspaceCommandFactory;
import net.thevpc.nuts.runtime.wscommands.DefaultNutsWorkspaceCommandAlias;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;

public class DefaultAliasManager implements NutsCommandAliasManager {
    private NutsWorkspace ws;
    private final ConfigNutsWorkspaceCommandFactory defaultCommandFactory;
    private final List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    public NutsLogger LOG;

    public DefaultAliasManager(NutsWorkspace ws) {
        this.ws = ws;
        LOG = ws.log().of(DefaultAliasManager.class);
        defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(ws);
    }

    @Override
    public void addFactory(NutsCommandAliasFactoryConfig commandFactoryConfig, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new NutsIllegalArgumentException(ws, "Invalid WorkspaceCommandFactory " + commandFactoryConfig);
        }
        for (NutsWorkspaceCommandFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new IllegalArgumentException();
            }
        }
        NutsWorkspaceCommandFactory f = null;
        if (CoreStringUtils.isBlank(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
            f = new CommandNutsWorkspaceCommandFactory(ws);
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
        NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
    }

    @Override
    public boolean removeFactory(String factoryId, NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (factoryId == null || factoryId.isEmpty()) {
            throw new NutsIllegalArgumentException(ws, "Invalid WorkspaceCommandFactory " + factoryId);
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsCommandAliasFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
                break;
            }
        }
        List<NutsCommandAliasFactoryConfig> _commandFactories = getStoreModelMain().getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NutsCommandAliasFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NutsCommandAliasFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
                    break;
                }
            }
        }
        if (removeMe == null && removeMeConfig == null) {
            throw new NutsIllegalArgumentException(ws, "Command Factory does not exists " + factoryId);
        }
        return true;
    }

    @Override
    public boolean add(NutsCommandAliasConfig command, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (command == null
                || CoreStringUtils.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().length == 0) {
            throw new NutsIllegalArgumentException(ws, "Invalid command alias " + (command == null ? "<NULL>" : command.getName()));
        }
        boolean forced = false;
        NutsSession session = options.getSession();
        if (defaultCommandFactory.findCommand(command.getName(), ws) != null) {
            if (session.isYes()) {
                forced = true;
                remove(command.getName(),
                        new NutsRemoveOptions().setSession(session.copy().setTrace(false))
                );
            } else {
                throw new NutsIllegalArgumentException(ws, "Command alias already exists " + command.getName());
            }
        }
        defaultCommandFactory.installCommand(command, options);
        if (session.isPlainTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(session);
            if (forced) {
                out.printf("[[re-install]] command alias ####%s####%n", command.getName());
            } else {
                out.printf("[[install]] command alias ####%s####%n", command.getName());
            }
        }
        return forced;
    }

    @Override
    public boolean remove(String name, NutsRemoveOptions options) {
        if (CoreStringUtils.isBlank(name)) {
            throw new NutsIllegalArgumentException(ws, "Invalid command alias " + (name == null ? "<NULL>" : name));
        }
        options = CoreNutsUtils.validate(options, ws);
        NutsSession session = options.getSession();
        NutsCommandAliasConfig command = defaultCommandFactory.findCommand(name, ws);
        if (command == null) {
            throw new NutsIllegalArgumentException(ws, "Command alias does not exists " + name);
        }
        defaultCommandFactory.uninstallCommand(name, options);
        if (session.isPlainTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(session);
            out.printf("[[uninstall]] command alias ####%s####%n", name);
        }
        return true;
    }

    NutsWorkspaceConfigMain getStoreModelMain(){
        return ((DefaultNutsWorkspaceConfigManager)ws.config()).getStoreModelMain();
    }

    @Override
    public NutsWorkspaceCommandAlias find(String name, NutsSession session) {
        NutsCommandAliasConfig c = defaultCommandFactory.findCommand(name, ws);
        if (c == null) {
            for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
                c = commandFactory.findCommand(name, ws);
                if (c != null) {
                    break;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return toDefaultNutsWorkspaceCommand(c);
    }

    @Override
    public List<NutsWorkspaceCommandAlias> findAll(NutsSession session) {
        HashMap<String, NutsWorkspaceCommandAlias> all = new HashMap<>();
        for (NutsCommandAliasConfig command : defaultCommandFactory.findCommands(ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
            for (NutsCommandAliasConfig command : commandFactory.findCommands(ws)) {
                if (!all.containsKey(command.getName())) {
                    all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    @Override
    public List<NutsWorkspaceCommandAlias> findByOwner(NutsId id, NutsSession session) {
        HashMap<String, NutsWorkspaceCommandAlias> all = new HashMap<>();
        for (NutsCommandAliasConfig command : defaultCommandFactory.findCommands(id, ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        return new ArrayList<>(all.values());
    }

    private NutsWorkspaceCommandAlias toDefaultNutsWorkspaceCommand(NutsCommandAliasConfig c) {
        if (c.getCommand() == null || c.getCommand().length == 0) {

            LOG.with().level(Level.WARNING).verb(NutsLogVerb.FAIL).log("invalid command definition ''{0}''. Missing command. Ignored", c.getName());
            return null;
        }
//        if (c.getOwner() == null) {
//            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
//            return null;
//        }
        return new DefaultNutsWorkspaceCommandAlias(ws)
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setOwner(c.getOwner())
                .setExecutorOptions(c.getExecutorOptions())
                .setName(c.getName())
                .setHelpCommand(c.getHelpCommand())
                .setHelpText(c.getHelpText());
    }

    @Override
    public NutsCommandAliasFactoryConfig[] getFactories(NutsSession session) {
        if (getStoreModelMain().getCommandFactories() != null) {
            return getStoreModelMain().getCommandFactories().toArray(new NutsCommandAliasFactoryConfig[0]);
        }
        return new NutsCommandAliasFactoryConfig[0];
    }

    @Override
    public NutsWorkspaceCommandAlias find(String name, NutsId forId, NutsId forOwner, NutsSession session) {
        NutsWorkspaceCommandAlias a = find(name, session);
        if (a != null && a.getCommand() != null && a.getCommand().length > 0) {
            NutsId i = ws.id().parser().parse(a.getCommand()[0]);
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

}
