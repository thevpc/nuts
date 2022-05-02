package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

public class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {

    private NutsLogger LOG;
    private NutsWorkspace ws;

    public ConfigNutsWorkspaceCommandFactory(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(ConfigNutsWorkspaceCommandFactory.class, session);
        }
        return LOG;
    }

    @Override
    public void configure(NutsCommandFactoryConfig config) {

    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getFactoryId() {
        return "default";
    }

    @Override
    public NutsCommandConfig findCommand(String name, NutsSession session) {
        checkSession(session);
        NutsPath file = getCommandsFolder(session).resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (file.exists()) {
            NutsCommandConfig c = NutsElements.of(session).json().parse(file, NutsCommandConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NutsCommandConfig> findCommands(NutsSession session) {
        return findCommands((Predicate<NutsCommandConfig>) null, session);
    }

    public NutsPath getStoreLocation(NutsSession session) {
        checkSession(session);
        return session.locations().getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.APPS);
    }

    private NutsPath getCommandsFolder(NutsSession session) {
        checkSession(session);
//        options = CoreNutsUtils.validate(options, ws);
        return getStoreLocation(session).resolve("cmd");
    }

    public void uninstallCommand(String name, NutsSession session) {
        checkSession(session);
//        options = CoreNutsUtils.validate(options, ws);
        NutsPath file = getCommandsFolder(session).resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
            NutsWorkspaceConfigManagerExt.of(session.config()).getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
        }
    }

    protected void checkSession(NutsSession session) {
        NutsSessionUtils.checkSession(ws, session);
    }

    public void installCommand(NutsCommandConfig command, NutsSession session) {
        checkSession(session);
        NutsPath path = getCommandsFolder(session).resolve(command.getName() + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        NutsElements.of(session).json().setValue(command)
                .setNtf(false).print(path);
        NutsWorkspaceConfigManagerExt.of(session.config()).getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
    }

    public List<NutsCommandConfig> findCommands(NutsId id, NutsSession session) {
        return findCommands(value -> CoreFilterUtils.matchesSimpleNameStaticVersion(value.getOwner(), id), session);
    }

    public List<NutsCommandConfig> findCommands(Predicate<NutsCommandConfig> filter, NutsSession session) {
        checkSession(session);
        List<NutsCommandConfig> all = new ArrayList<>();
        NutsPath storeLocation = getCommandsFolder(session);
        if (!storeLocation.isDirectory()) {
            //_LOGOP(session).level(Level.SEVERE).log(NutsMessage.jstyle("unable to locate commands. Invalid store locate {0}", storeLocation));
            return all;
        }
        storeLocation.list().forEach(file -> {
            String fileName = file.getName();
            if (file.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                NutsCommandConfig c = null;
                try {
                    c = NutsElements.of(session).json().parse(file, NutsCommandConfig.class);
                } catch (Exception ex) {
                    _LOGOP(session).level(Level.FINE).error(ex).log(NutsMessage.jstyle("unable to parse {0}", file));
                    //
                }
                if (c != null) {
                    c.setName(fileName.substring(0, fileName.length() - NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION.length()));
                    if (filter == null || filter.test(c)) {
                        all.add(c);
                    }
                }
            }
        });
        return all;
    }
}
