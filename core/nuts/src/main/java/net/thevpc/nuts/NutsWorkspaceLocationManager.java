package net.thevpc.nuts;

import java.nio.file.Path;
import java.util.Map;

public interface NutsWorkspaceLocationManager {
    Path getHomeLocation(NutsStoreLocation folderType);

    Path getStoreLocation(NutsStoreLocation folderType);

    void setStoreLocation(NutsStoreLocation folderType, String location, NutsUpdateOptions options);

    void setStoreLocationStrategy(NutsStoreLocationStrategy strategy, NutsUpdateOptions options);

    void setStoreLocationLayout(NutsOsFamily layout, NutsUpdateOptions options);

    Path getStoreLocation(String id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, NutsStoreLocation folderType);
    void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType, String location, NutsUpdateOptions options);
    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    NutsOsFamily getStoreLocationLayout();

    /**
     * all home locations key/value map where keys are in the form "location"
     * and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<String, String> getStoreLocations();

    /**
     * all home locations key/value map where keys are in the form
     * "osfamily:location" and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<String, String> getHomeLocations();

    Path getHomeLocation(NutsOsFamily layout, NutsStoreLocation location);

    Path getWorkspaceLocation();

    String getDefaultIdFilename(NutsId id);

    String getDefaultIdBasedir(NutsId id);

    String getDefaultIdContentExtension(String packaging);

    String getDefaultIdExtension(NutsId id);

}
