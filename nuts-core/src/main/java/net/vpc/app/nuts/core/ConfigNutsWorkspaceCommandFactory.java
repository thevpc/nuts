package net.vpc.app.nuts.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {

    private DefaultNutsWorkspaceConfigManager configManager;

    public ConfigNutsWorkspaceCommandFactory(DefaultNutsWorkspaceConfigManager defaultNutsWorkspaceConfigManager) {
        this.configManager = defaultNutsWorkspaceConfigManager;
    }

    @Override
    public void configure(NutsCommandAliasFactoryConfig config) {

    }

    @Override
    public String getFactoryId() {
        return "default";
    }

    public Path getStoreLocation() {
        return configManager.getStoreLocation(configManager.getApiId(), NutsStoreLocation.PROGRAMS);
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
        Path file = getStoreLocation().resolve(command.getName() + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        configManager.getWorkspace().io().writeJson(command, file, true);
    }

    @Override
    public NutsCommandAliasConfig findCommand(String name, NutsWorkspace workspace) {
        Path file = getStoreLocation().resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            NutsCommandAliasConfig c = configManager.getWorkspace().io().readJson(file, NutsCommandAliasConfig.class);
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
                if (id.getVersion().isBlank()) {
                    return value.getOwner().getSimpleName().equals(id.getSimpleName());
                } else {
                    return value.getOwner().getLongName().equals(id.getLongName());
                }
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
                        c = configManager.getWorkspace().io().readJson(file, NutsCommandAliasConfig.class);
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
