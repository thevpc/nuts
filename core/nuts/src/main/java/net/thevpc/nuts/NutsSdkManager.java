/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import java.util.function.Predicate;

/**
 * @category Toolkit
 */
public interface NutsSdkManager {
    String[] findSdkTypes();

    boolean add(NutsSdkLocation location, NutsAddOptions options);

    boolean update(NutsSdkLocation oldLocation, NutsSdkLocation newLocation, NutsUpdateOptions options);

    boolean remove(NutsSdkLocation location, NutsRemoveOptions options);

    NutsSdkLocation findByName(String sdkType, String locationName, NutsSession session);

    NutsSdkLocation findByPath(String sdkType, String path, NutsSession session);

    NutsSdkLocation findByVersion(String sdkType, String version, NutsSession session);

    NutsSdkLocation find(NutsSdkLocation location, NutsSession session);

    NutsSdkLocation findByVersion(String sdkType, NutsVersionFilter requestedVersion, NutsSession session);


    NutsSdkLocation[] searchSystem(String sdkType, NutsSession session);

    NutsSdkLocation[] searchSystem(String sdkType, String path, NutsSession session);

    /**
     * verify if the path is a valid sdk path and return null if not
     *
     * @param sdkType       sdk type
     * @param path          sdk path
     * @param preferredName preferredName
     * @param session       session
     * @return null if not a valid jdk path
     */
    NutsSdkLocation resolve(String sdkType, String path, String preferredName, NutsSession session);

    NutsSdkLocation findOne(String type, Predicate<NutsSdkLocation> filter, NutsSession session);

    NutsSdkLocation[] find(String type, Predicate<NutsSdkLocation> filter, NutsSession session);
}
