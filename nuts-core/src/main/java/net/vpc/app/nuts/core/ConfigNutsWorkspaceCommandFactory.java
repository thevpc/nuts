package net.vpc.app.nuts.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

public class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {

    private NutsWorkspaceConfigManager configManager;
    private NutsWorkspaceConfigManagerExt configManagerExt;

    public ConfigNutsWorkspaceCommandFactory(NutsWorkspaceConfigManager cnf) {
        this.configManager = cnf;
        this.configManagerExt = NutsWorkspaceConfigManagerExt.of(cnf);
    }

    @Override
    public void configure(NutsCommandAliasFactoryConfig config) {

    }

    @Override
    public String getFactoryId() {
        return "default";
    }

    public Path getStoreLocation() {
        return configManager.getStoreLocation(configManager.getApiId(), NutsStoreLocation.APPS);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    public void uninstallCommand(String name) {
        Path file = getStoreLocation().resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public void installCommand(NutsCommandAliasConfig command) {
        Path path = getStoreLocation().resolve(command.getName() + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        configManagerExt.getWorkspace().json().value(command).print(path);
    }

    @Override
    public NutsCommandAliasConfig findCommand(String name, NutsWorkspace workspace) {
        Path file = getStoreLocation().resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            NutsCommandAliasConfig c = configManagerExt.getWorkspace().json().parse(file, NutsCommandAliasConfig.class);
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
            Files.list(getStoreLocation()).forEach(file -> {
                String fileName = file.getFileName().toString();
                if (file.getFileName().toString().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                    NutsCommandAliasConfig c = null;
                    try {
                        c = configManagerExt.getWorkspace().json().parse(file, NutsCommandAliasConfig.class);
                    } catch (Exception ex) {
                        //
                    }
                    if (c != null) {
                        c.setName(fileName.substring(0, fileName.length() - 4));
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
