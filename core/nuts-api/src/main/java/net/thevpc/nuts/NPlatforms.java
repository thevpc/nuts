///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// *
// * <br>
// * <p>
// * Copyright [2020] [thevpc]
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
// * you may  not use this file except in compliance with the License. You may obtain
// * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// * either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br>
// * ====================================================================
// */
//package net.thevpc.nuts;
//
//import net.thevpc.nuts.ext.NExtensions;
//import net.thevpc.nuts.io.NPath;
//import net.thevpc.nuts.spi.NComponent;
//import net.thevpc.nuts.util.NOptional;
//import net.thevpc.nuts.NPlatformFamily;
//import net.thevpc.nuts.util.NStream;
//
//import java.util.function.Predicate;
//
///**
// * @app.category Toolkit
// */
//public interface NPlatforms extends NComponent {
//    static NPlatforms of() {
//        return NExtensions.of(NPlatforms.class);
//    }
//
//    boolean addPlatform(NPlatformLocation location);
//
//    boolean updatePlatform(NPlatformLocation oldLocation, NPlatformLocation newLocation);
//
//    boolean removePlatform(NPlatformLocation location);
//
//    NOptional<NPlatformLocation> findPlatformByName(NPlatformFamily platformType, String locationName);
//
//    NOptional<NPlatformLocation> findPlatformByPath(NPlatformFamily platformType, NPath path);
//
//    NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, String version);
//
//    NOptional<NPlatformLocation> findPlatform(NPlatformLocation location);
//
//    NOptional<NPlatformLocation> findPlatformByVersion(NPlatformFamily platformType, NVersionFilter requestedVersion);
//
//
//    NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily);
//
//    NStream<NPlatformLocation> searchSystemPlatforms(NPlatformFamily platformFamily, NPath path);
//
//    /**
//     * verify if the path is a valid platform path and return null if not
//     *
//     * @param platformType  platform type
//     * @param path          platform path
//     * @param preferredName preferredName
//     * @return null if not a valid jdk path
//     */
//    NOptional<NPlatformLocation> resolvePlatform(NPlatformFamily platformType, NPath path, String preferredName);
//
//    NOptional<NPlatformLocation> findPlatform(NPlatformFamily type, Predicate<NPlatformLocation> filter);
//
//    NStream<NPlatformLocation> findPlatforms(NPlatformFamily type, Predicate<NPlatformLocation> filter);
//
//    NStream<NPlatformLocation> findPlatforms();
//
//    NStream<NPlatformLocation> findPlatforms(NPlatformFamily type);
//
//    void addDefaultPlatforms(NPlatformFamily type);
//
//    void addDefaultPlatform(NPlatformFamily type);
//}
