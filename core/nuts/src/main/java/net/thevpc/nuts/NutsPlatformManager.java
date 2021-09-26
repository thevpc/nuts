/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
 * @app.category Toolkit
 */
public interface NutsPlatformManager {
    boolean addPlatform(NutsPlatformLocation location);

    boolean updatePlatform(NutsPlatformLocation oldLocation, NutsPlatformLocation newLocation);

    boolean removePlatform(NutsPlatformLocation location);

    NutsPlatformLocation findPlatformByName(NutsPlatformType platformType, String locationName);

    NutsPlatformLocation findPlatformByPath(NutsPlatformType platformType, String path);

    NutsPlatformLocation findPlatformByVersion(NutsPlatformType platformType, String version);

    NutsPlatformLocation findPlatform(NutsPlatformLocation location);

    NutsPlatformLocation findPlatformByVersion(NutsPlatformType platformType, NutsVersionFilter requestedVersion);


    NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformType platformType);

    NutsPlatformLocation[] searchSystemPlatforms(NutsPlatformType platformType, String path);

    /**
     * verify if the path is a valid platform path and return null if not
     *
     * @param platformType       platform type
     * @param path          platform path
     * @param preferredName preferredName
     * @return null if not a valid jdk path
     */
    NutsPlatformLocation resolvePlatform(NutsPlatformType platformType, String path, String preferredName);

    NutsPlatformLocation findPlatform(NutsPlatformType type, Predicate<NutsPlatformLocation> filter);

    NutsPlatformLocation[] findPlatforms(NutsPlatformType type, Predicate<NutsPlatformLocation> filter);

    NutsPlatformLocation[] findPlatforms(NutsPlatformType type);

    NutsSession getSession();

    NutsPlatformManager setSession(NutsSession session);
}
