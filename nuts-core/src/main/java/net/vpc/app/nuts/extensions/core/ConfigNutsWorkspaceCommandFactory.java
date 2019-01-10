package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {
    private DefaultNutsWorkspaceConfigManager defaultNutsWorkspaceConfigManager;

    public ConfigNutsWorkspaceCommandFactory(DefaultNutsWorkspaceConfigManager defaultNutsWorkspaceConfigManager) {
        this.defaultNutsWorkspaceConfigManager = defaultNutsWorkspaceConfigManager;
    }

    @Override
    public void configure(NutsWorkspaceCommandFactoryConfig config) {

    }

    @Override
    public String getFactoryId() {
        return "default";
    }

    public File getStoreLocation() {
        String storeLocation = defaultNutsWorkspaceConfigManager.getStoreLocation(StoreFolder.PROGRAMS);
        if (storeLocation == null) {
            return null;
        }
        return new File(storeLocation);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    public void uninstallCommand(String name) {
        File file = new File(getStoreLocation(), name + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
        if (file.isFile()) {
            if (!file.delete()) {
                throw new IllegalArgumentException("Unable to delete file " + file.getPath());
            }
        }
    }

    public void installCommand(NutsWorkspaceCommandConfig command) {
        File file = new File(getStoreLocation(), command.getName() + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
        defaultNutsWorkspaceConfigManager.getWorkspace().getIOManager().writeJson(command, file, true);
    }

    @Override
    public NutsWorkspaceCommandConfig findCommand(String name, NutsWorkspace workspace) {
        File file = new File(getStoreLocation(), name + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
        if (file.exists()) {
            NutsWorkspaceCommandConfig c = defaultNutsWorkspaceConfigManager.getWorkspace().getIOManager().readJson(file, NutsWorkspaceCommandConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NutsWorkspaceCommandConfig> findCommands(NutsWorkspace workspace) {
        return findCommands((ObjectFilter<NutsWorkspaceCommandConfig>) null);
    }

    public List<NutsWorkspaceCommandConfig> findCommands(NutsId id, NutsWorkspace workspace) {
        return findCommands(new ObjectFilter<NutsWorkspaceCommandConfig>() {
            @Override
            public boolean accept(NutsWorkspaceCommandConfig value) {
                if (id.getVersion().isEmpty()) {
                    return value.getId().getSimpleName().equals(id.getSimpleName());
                } else {
                    return value.getId().getLongName().equals(id.getLongName());
                }
            }
        });
    }

    public List<NutsWorkspaceCommandConfig> findCommands(ObjectFilter<NutsWorkspaceCommandConfig> filter) {
        List<NutsWorkspaceCommandConfig> all = new ArrayList<>();
        File[] files = getStoreLocation().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(NutsConstants.NUTS_COMMAND_FILE_EXTENSION)) {
                    NutsWorkspaceCommandConfig c = null;
                    try {
                        c = defaultNutsWorkspaceConfigManager.getWorkspace().getIOManager().readJson(file, NutsWorkspaceCommandConfig.class);
                    } catch (Exception ex) {
                        //
                    }
                    if (c != null) {
                        c.setName(file.getName().substring(0, file.getName().length() - 4));
                        if (filter == null || filter.accept(c)) {
                            all.add(c);
                        }
                    }
                }
            }
        }
        return all;
    }
}
