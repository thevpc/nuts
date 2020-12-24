package net.thevpc.nuts.runtime.standalone.wscommands;

import java.io.IOException;
import java.nio.file.Files;

import net.thevpc.nuts.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.main.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;

public class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {

    private final NutsLogger LOG;
    private NutsWorkspace ws;

    public ConfigNutsWorkspaceCommandFactory(NutsWorkspace ws) {
        this.ws = ws;
        LOG = ws.log().of(ConfigNutsWorkspaceCommandFactory.class);
    }

    @Override
    public void configure(NutsCommandAliasFactoryConfig config) {

    }

    @Override
    public String getFactoryId() {
        return "default";
    }

    public Path getStoreLocation() {
        return ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.APPS);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    public void uninstallCommand(String name, NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        Path file = getStoreLocation().resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
            } catch (IOException ex) {
                throw new NutsIOException(ws,ex);
            }
        }
    }

    public void installCommand(NutsCommandAliasConfig command, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        Path path = getStoreLocation().resolve(command.getName() + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        ws.formats().element().setContentType(NutsContentType.JSON).setValue(command).setSession(options.getSession()).print(path);
        NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
    }

    @Override
    public NutsCommandAliasConfig findCommand(String name, NutsSession session) {
        Path file = getStoreLocation().resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            NutsCommandAliasConfig c = ws.formats().element().setContentType(NutsContentType.JSON).parse(file, NutsCommandAliasConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NutsCommandAliasConfig> findCommands(NutsSession session) {
        return findCommands((Predicate<NutsCommandAliasConfig>) null, session);
    }

    public List<NutsCommandAliasConfig> findCommands(NutsId id, NutsSession session) {
        return findCommands(new Predicate<NutsCommandAliasConfig>() {
            @Override
            public boolean test(NutsCommandAliasConfig value) {
                return CoreNutsUtils.matchesSimpleNameStaticVersion(value.getOwner(), id);
            }
        }, session);
    }

    public List<NutsCommandAliasConfig> findCommands(Predicate<NutsCommandAliasConfig> filter, NutsSession session) {
        List<NutsCommandAliasConfig> all = new ArrayList<>();
        try {
            if (!Files.isDirectory(getStoreLocation())) {
                LOG.with().session(session).level(Level.SEVERE).log("Unable to locate commands. Invalid store locate {0}", getStoreLocation());
                return all;
            }
            Files.list(getStoreLocation()).forEach(file -> {
                String fileName = file.getFileName().toString();
                if (file.getFileName().toString().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                    NutsCommandAliasConfig c = null;
                    try {
                        c = ws.formats().element().setContentType(NutsContentType.JSON).parse(file, NutsCommandAliasConfig.class);
                    } catch (Exception ex) {
                        LOG.with().session(session).level(Level.FINE).error(ex).log("unable to parse {0}", file);
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
        } catch (IOException ex) {
            throw new NutsIOException(ws,ex);
        }
        return all;
    }
}
