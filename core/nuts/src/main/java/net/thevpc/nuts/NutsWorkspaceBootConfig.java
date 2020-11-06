package net.thevpc.nuts;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface NutsWorkspaceBootConfig {
    boolean isImmediateLocation();

    String getEffectiveWorkspaceName();

    String getBootPath();

    String getEffectivePath();

    String getName();

    String getWorkspace();

    List<Extension> getExtensions();

    String getBootRepositories();

    Map<String, String> getStoreLocations();

    Map<String, String> getHomeLocations();

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsOsFamily getStoreLocationLayout();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    String getUuid();

    boolean isGlobal();

    Path getStoreLocation(NutsId id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsStoreLocation storeLocation);

    Path getHomeLocation(NutsOsFamily layout, NutsStoreLocation storeLocation);

    Path getHomeLocation(NutsStoreLocation storeLocation);

    interface Extension {
        NutsId getId();
        boolean isEnabled();
    }
}
