package net.vpc.app.nuts.runtime.wscommands;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import net.vpc.app.nuts.main.config.DefaultNutsWorkspaceConfigManager;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.NutsLogger;

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
        return ws.config().getStoreLocation(ws.getApiId(), NutsStoreLocation.APPS);
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
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("command", options.getSession(), DefaultNutsWorkspaceConfigManager.ConfigEventType.MAIN);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public void installCommand(NutsCommandAliasConfig command, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        Path path = getStoreLocation().resolve(command.getName() + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        ws.formats().json().value(command).print(path);
        NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("command", options.getSession(), DefaultNutsWorkspaceConfigManager.ConfigEventType.MAIN);
    }

    @Override
    public NutsCommandAliasConfig findCommand(String name, NutsWorkspace workspace) {
        Path file = getStoreLocation().resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            NutsCommandAliasConfig c = ws.formats().json().parse(file, NutsCommandAliasConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NutsCommandAliasConfig> findCommands(NutsWorkspace workspace) {
        return findCommands((Predicate<NutsCommandAliasConfig>) null);
    }

    public List<NutsCommandAliasConfig> findCommands(NutsId id, NutsWorkspace workspace) {
        return findCommands(new Predicate<NutsCommandAliasConfig>() {
            @Override
            public boolean test(NutsCommandAliasConfig value) {
                return CoreNutsUtils.matchesSimpleNameStaticVersion(value.getOwner(), id);
            }
        });
    }

    public List<NutsCommandAliasConfig> findCommands(Predicate<NutsCommandAliasConfig> filter) {
        List<NutsCommandAliasConfig> all = new ArrayList<>();
        try {
            if (!Files.isDirectory(getStoreLocation())) {
                LOG.with().level(Level.SEVERE).log("Unable to locate commands. Invalid store locate {0}", getStoreLocation());
                return all;
            }
            Files.list(getStoreLocation()).forEach(file -> {
                String fileName = file.getFileName().toString();
                if (file.getFileName().toString().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                    NutsCommandAliasConfig c = null;
                    try {
                        c = ws.formats().json().parse(file, NutsCommandAliasConfig.class);
                    } catch (Exception ex) {
                        LOG.with().level(Level.FINE).error(ex).log("unable to parse {0}", file);
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
            throw new UncheckedIOException(ex);
        }
        return all;
    }
}
