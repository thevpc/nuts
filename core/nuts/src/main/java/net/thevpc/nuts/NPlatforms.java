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

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NStream;

import java.util.function.Predicate;

/**
 * @app.category Toolkit
 */
public interface NPlatforms extends NComponent {
    static NPlatforms of(NSession session) {
        return NExtensions.of(session).createSupported(NPlatforms.class);
    }
    boolean addPlatform(NPlatformLocation location);

    boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation);

    boolean removePlatform(NPlatformLocation location);

    NPlatformLocation findPlatformByName(NPlatformFamily platformType, String locationName);

    NPlatformLocation findPlatformByPath(NPlatformFamily platformType, String path);

    NPlatformLocation findPlatformByVersion(NPlatformFamily platformType, String version);

    NPlatformLocation findPlatform(NPlatformLocation location);

    NPlatformLocation findPlatformByVersion(NPlatformFamily platformType, NVersionFilter requestedVersion);


    NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType);

    NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformType, String path);

    /**
     * verify if the path is a valid platform path and return null if not
     *
     * @param platformType  platform type
     * @param path          platform path
     * @param preferredName preferredName
     * @return null if not a valid jdk path
     */
    NPlatformLocation resolvePlatform(NPlatformFamily platformType, String path, String preferredName);

    NPlatformLocation findPlatform(NPlatformFamily type, Predicate<NPlatformLocation> filter);

    NStream<NPlatformLocation> findPlatforms(NPlatformFamily type, Predicate<NPlatformLocation> filter);

    NStream<NPlatformLocation> findPlatforms();

    NStream<NPlatformLocation> findPlatforms(NPlatformFamily type);

    NSession getSession();

    NPlatforms setSession(NSession session);
}
