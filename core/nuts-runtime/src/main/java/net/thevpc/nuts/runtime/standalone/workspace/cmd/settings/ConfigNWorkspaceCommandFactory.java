package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElementWriter;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ConfigNWorkspaceCommandFactory implements NWorkspaceCmdFactory {

    public ConfigNWorkspaceCommandFactory() {
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
            NCommandConfig c = NElementParser.ofJson().parse(file, NCommandConfig.class);
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
        return NWorkspace.of().getStoreLocation(NWorkspace.of().getApiId(), NStoreType.BIN);
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
            NWorkspaceExt.of().getConfigModel().fireConfigurationChanged("command", ConfigEventType.MAIN);
        }
    }

    public void installCommand(NCommandConfig command) {
        NPath path = getCommandsFolder().resolve(command.getName() + NConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        NElementWriter.ofJson().setNtf(false).write(command,path);
        NWorkspaceExt.of().getConfigModel().fireConfigurationChanged("command", ConfigEventType.MAIN);
    }

    public List<NCommandConfig> findCommands(NId id) {
        return findCommands(value -> CoreFilterUtils.matchesSimpleNameStaticVersion(value.getOwner(), id));
    }

    public List<NCommandConfig> findCommands(Predicate<NCommandConfig> filter) {
        List<NCommandConfig> all = new ArrayList<>();
        NPath storeLocation = getCommandsFolder();
        if (!storeLocation.isDirectory()) {
            //_LOG().level(Level.SEVERE).log(NMsg.jstyle("unable to locate commands. Invalid store locate {0}", storeLocation));
            return all;
        }
        storeLocation.stream().forEach(file -> {
            String fileName = file.getName();
            if (file.getName().endsWith(NConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                NCommandConfig c = null;
                try {
                    c = NElementParser.ofJson().parse(file, NCommandConfig.class);
                } catch (Exception ex) {
                    _LOG().log(NMsg.ofC("unable to parse %s", file).asFine(ex));
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
