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
 * @app.category Toolkit
 */
public interface NutsPlatformManager {
    String[] findPlatformTypes();

    boolean add(NutsPlatformLocation location);

    boolean update(NutsPlatformLocation oldLocation, NutsPlatformLocation newLocation);

    boolean remove(NutsPlatformLocation location);

    NutsPlatformLocation findByName(String platformType, String locationName);

    NutsPlatformLocation findByPath(String platformType, String path);

    NutsPlatformLocation findByVersion(String platformType, String version);

    NutsPlatformLocation find(NutsPlatformLocation location);

    NutsPlatformLocation findByVersion(String platformType, NutsVersionFilter requestedVersion);


    NutsPlatformLocation[] searchSystem(String platformType);

    NutsPlatformLocation[] searchSystem(String platformType, String path);

    /**
     * verify if the path is a valid platform path and return null if not
     *
     * @param platformType       platform type
     * @param path          platform path
     * @param preferredName preferredName
     * @return null if not a valid jdk path
     */
    NutsPlatformLocation resolve(String platformType, String path, String preferredName);

    NutsPlatformLocation findOne(String type, Predicate<NutsPlatformLocation> filter);

    NutsPlatformLocation[] find(String type, Predicate<NutsPlatformLocation> filter);

    NutsPlatformLocation[] findAll(String type);

    NutsSession getSession();

    NutsPlatformManager setSession(NutsSession session);
}
