package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
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

    private NWorkspace workspace;

    public ConfigNWorkspaceCommandFactory(NWorkspace workspace) {
        this.workspace = workspace;
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
            return NLog.of(ConfigNWorkspaceCommandFactory.class);
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
    public NCommandConfig findCommand(String name) {
        NPath file = getCommandsFolder().resolve(name + NConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (file.exists()) {
            NCommandConfig c = NElements.of().json().parse(file, NCommandConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NCommandConfig> findCommands() {
        return findCommands((Predicate<NCommandConfig>) null);
    }

    public NPath getStoreLocation() {
        return NLocations.of().getStoreLocation(workspace.getApiId(), NStoreType.BIN);
    }

    private NPath getCommandsFolder() {
//        options = CoreNutsUtils.validate(options, ws);
        return getStoreLocation().resolve("cmd");
    }

    public void uninstallCommand(String name) {
//        options = CoreNutsUtils.validate(options, ws);
        NPath file = getCommandsFolder().resolve(name + NConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (file.exists()) {
            file.delete();
            NConfigsExt.of(NConfigs.of()).getModel().fireConfigurationChanged("command", ConfigEventType.MAIN);
        }
    }

    public void installCommand(NCommandConfig command) {
        NPath path = getCommandsFolder().resolve(command.getName() + NConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        NElements.of().json().setValue(command)
                .setNtf(false).print(path);
        NConfigsExt.of(NConfigs.of()).getModel().fireConfigurationChanged("command", ConfigEventType.MAIN);
    }

    public List<NCommandConfig> findCommands(NId id) {
        return findCommands(value -> CoreFilterUtils.matchesSimpleNameStaticVersion(value.getOwner(), id));
    }

    public List<NCommandConfig> findCommands(Predicate<NCommandConfig> filter) {
        List<NCommandConfig> all = new ArrayList<>();
        NPath storeLocation = getCommandsFolder();
        if (!storeLocation.isDirectory()) {
            //_LOGOP().level(Level.SEVERE).log(NMsg.jstyle("unable to locate commands. Invalid store locate {0}", storeLocation));
            return all;
        }
        storeLocation.stream().forEach(file -> {
            String fileName = file.getName();
            if (file.getName().endsWith(NConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                NCommandConfig c = null;
                try {
                    c = NElements.of().json().parse(file, NCommandConfig.class);
                } catch (Exception ex) {
                    _LOGOP().level(Level.FINE).error(ex).log(NMsg.ofC("unable to parse %s", file));
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
