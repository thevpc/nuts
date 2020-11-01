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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.List;
import java.util.Set;

/**
 *
 * @author vpc
 * @since 0.5.4
 * @category Extensions
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
