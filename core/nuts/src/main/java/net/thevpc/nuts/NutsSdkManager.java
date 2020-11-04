package net.thevpc.nuts;

import java.nio.file.Path;
import java.util.function.Predicate;

public interface NutsSdkManager {
    String[] findSdkTypes();

    boolean add(NutsSdkLocation location, NutsAddOptions options);

    boolean update(NutsSdkLocation oldLocation, NutsSdkLocation newLocation, NutsUpdateOptions options);

    boolean remove(NutsSdkLocation location, NutsRemoveOptions options);

    NutsSdkLocation findByName(String sdkType, String locationName, NutsSession session);

    NutsSdkLocation findByPath(String sdkType, Path path, NutsSession session);

    NutsSdkLocation findByVersion(String sdkType, String version, NutsSession session);

    NutsSdkLocation find(NutsSdkLocation location, NutsSession session);

    NutsSdkLocation findByVersion(String sdkType, NutsVersionFilter requestedVersion, NutsSession session);


    NutsSdkLocation[] searchSystem(String sdkType, NutsSession session);

    NutsSdkLocation[] searchSystem(String sdkType, Path path, NutsSession session);

    /**
     * verify if the path is a valid sdk path and return null if not
     *
     * @param sdkType       sdk type
     * @param path          sdk path
     * @param preferredName preferredName
     * @param session       session
     * @return null if not a valid jdk path
     */
    NutsSdkLocation resolve(String sdkType, Path path, String preferredName, NutsSession session);

    NutsSdkLocation findOne(String type, Predicate<NutsSdkLocation> filter, NutsSession session);

    NutsSdkLocation[] find(String type, Predicate<NutsSdkLocation> filter, NutsSession session);
}
