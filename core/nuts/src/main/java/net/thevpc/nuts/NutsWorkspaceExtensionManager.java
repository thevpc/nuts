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

import net.thevpc.nuts.spi.NutsComponent;

import java.util.List;
import java.util.Set;

/**
 * @author thevpc
 * @app.category Extensions
 * @since 0.5.4
 */
public interface NutsWorkspaceExtensionManager {

    Set<NutsId> getCompanionIds();

    boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl);

    Set<Class> discoverTypes(NutsId id, ClassLoader classLoader);

    <T extends NutsComponent, B> NutsServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType);

    <T extends NutsComponent, B> NutsServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader);

    /**
     * create supported extension implementation or return null.
     *
     * @param <T>             extension type class
     * @param <V>             extension context type
     * @param type            extension type
     * @param required required
     * @param supportCriteria context
     * @return valid instance or null if no extension implementation was found
     */
    <T extends NutsComponent, V> T createSupported(Class<T> type, boolean required, V supportCriteria);

    <T extends NutsComponent, V> List<T> createAllSupported(Class<T> type, V supportCriteria);

    <T> List<T> createAll(Class<T> type);

//    Set<Class> getExtensionPoints(NutsSession session);


    Set<Class> getExtensionTypes(Class extensionPoint);

    List<Object> getExtensionObjects(Class extensionPoint);

    boolean isRegisteredType(Class extensionPointType, String name);

    boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl);

    <T> boolean registerInstance(Class<T> extensionPoint, T implementation);

    boolean registerType(Class extensionPointType, Class extensionType, NutsId source);

    boolean isRegisteredType(Class extensionPointType, Class extensionType);

    boolean isLoadedExtensions(NutsId id);

    List<NutsId> getLoadedExtensions();

    NutsWorkspaceExtensionManager loadExtension(NutsId extension);

    NutsWorkspaceExtensionManager unloadExtension(NutsId extension);

    /**
     * return loaded extensions
     *
     * @return extension ids
     */
    List<NutsId> getConfigExtensions();

    NutsSession getSession();

    NutsWorkspaceExtensionManager setSession(NutsSession session);


}
