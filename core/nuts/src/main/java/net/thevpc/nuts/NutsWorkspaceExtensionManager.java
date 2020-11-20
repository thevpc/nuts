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

import java.util.List;
import java.util.Set;

/**
 *
 * @author vpc
 * @since 0.5.4
 * %category Extensions
 */
public interface NutsWorkspaceExtensionManager {

    boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl, NutsSession session);

    List<Class> discoverTypes(ClassLoader classLoader, NutsSession session);

    List<Class> getImplementationTypes(Class type, NutsSession session);

    <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, NutsSession session);

    <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader, NutsSession session);

    /**
     * create supported extension implementation or return null.
     * @param <T> extension type class
     * @param <V> extension context type
     * @param type extension type
     * @param supportCriteria context
     * @param session
     * @return valid instance or null if no extension implementation was found
     */
    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, NutsSession session);

    /**
     * create supported extension implementation or return null.
     * @param <T> extension type class
     * @param <V> extension context type
     * @param type extension type
     * @param supportCriteria context
     * @param constructorParameterTypes constructor Parameter Types
     * @param constructorParameters constructor Parameters
     * @param session
     * @return valid instance or null if no extension implementation was found
     */
    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters, NutsSession session);

    <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, V supportCriteria, NutsSession session);

    <T> List<T> createAll(Class<T> type, NutsSession session);

    Set<Class> getExtensionPoints(NutsSession session);

    Set<Class> getExtensionTypes(Class extensionPoint, NutsSession session);

    List<Object> getExtensionObjects(Class extensionPoint, NutsSession session);

    boolean isRegisteredType(Class extensionPointType, String name, NutsSession session);

    boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl, NutsSession session);

    <T> boolean registerInstance(Class<T> extensionPoint, T implementation, NutsSession session);

    boolean registerType(Class extensionPointType, Class extensionType, NutsSession session);

    boolean isRegisteredType(Class extensionPointType, Class extensionType, NutsSession session);

    boolean isLoadedExtensions(NutsId id, NutsSession session);

    List<NutsId> getLoadedExtensions(NutsSession session);

    NutsWorkspaceExtensionManager loadExtension(NutsId extension, NutsSession session);

    NutsWorkspaceExtensionManager unloadExtension(NutsId extension, NutsSession session);

    /**
     * return loaded extensions
     *
     * @return extension ids
     * @param session
     */
    List<NutsId> getConfigExtensions(NutsSession session);

}
