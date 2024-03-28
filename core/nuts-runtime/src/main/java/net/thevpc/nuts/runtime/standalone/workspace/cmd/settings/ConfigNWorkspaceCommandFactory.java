package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NConfigsExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

public class ConfigNWorkspaceCommandFactory implements NWorkspaceCmdFactory {

    private NLog LOG;
    private NWorkspace ws;

    public ConfigNWorkspaceCommandFactory(NWorkspace ws) {
        this.ws = ws;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(ConfigNWorkspaceCommandFactory.class, session);
        }
        return LOG;
    }

    @Override
    public void configure(NCommandFactoryConfig config) {

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
    public NCommandConfig findCommand(String name, NSession session) {
        checkSession(session);
        NPath file = getCommandsFolder(session).resolve(name + NConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (file.exists()) {
            NCommandConfig c = NElements.of(session).json().parse(file, NCommandConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NCommandConfig> findCommands(NSession session) {
        return findCommands((Predicate<NCommandConfig>) null, session);
    }

    public NPath getStoreLocation(NSession session) {
        checkSession(session);
        return NLocations.of(session).getStoreLocation(session.getWorkspace().getApiId(), NStoreType.BIN);
    }

    private NPath getCommandsFolder(NSession session) {
        checkSession(session);
//        options = CoreNutsUtils.validate(options, ws);
        return getStoreLocation(session).resolve("cmd");
    }

    public void uninstallCommand(String name, NSession session) {
        checkSession(session);
//        options = CoreNutsUtils.validate(options, ws);
        NPath file = getCommandsFolder(session).resolve(name + NConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
            NConfigsExt.of(NConfigs.of(session)).getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
        }
    }

    protected void checkSession(NSession session) {
        NSessionUtils.checkSession(ws, session);
    }

    public void installCommand(NCommandConfig command, NSession session) {
        checkSession(session);
        NPath path = getCommandsFolder(session).resolve(command.getName() + NConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        NElements.of(session).json().setValue(command)
                .setNtf(false).print(path);
        NConfigsExt.of(NConfigs.of(session)).getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
    }

    public List<NCommandConfig> findCommands(NId id, NSession session) {
        return findCommands(value -> CoreFilterUtils.matchesSimpleNameStaticVersion(value.getOwner(), id), session);
    }

    public List<NCommandConfig> findCommands(Predicate<NCommandConfig> filter, NSession session) {
        checkSession(session);
        List<NCommandConfig> all = new ArrayList<>();
        NPath storeLocation = getCommandsFolder(session);
        if (!storeLocation.isDirectory()) {
            //_LOGOP(session).level(Level.SEVERE).log(NMsg.jstyle("unable to locate commands. Invalid store locate {0}", storeLocation));
            return all;
        }
        storeLocation.stream().forEach(file -> {
            String fileName = file.getName();
            if (file.getName().endsWith(NConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                NCommandConfig c = null;
                try {
                    c = NElements.of(session).json().parse(file, NCommandConfig.class);
                } catch (Exception ex) {
                    _LOGOP(session).level(Level.FINE).error(ex).log(NMsg.ofJ("unable to parse {0}", file));
                    //
                }
                if (c != null) {
                    c.setName(fileName.substring(0, fileName.length() - NConstants.Files.NUTS_COMMAND_FILE_EXTENSION.length()));
                    if (filter == null || filter.test(c)) {
                        all.add(c);
                    }
                }
            }
        });
        return all;
    }
}
