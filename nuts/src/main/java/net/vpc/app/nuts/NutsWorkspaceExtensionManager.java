/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsWorkspaceExtensionManager {

    /**
     * find all available extensions for the current workspace
     *
     * @param session
     * @return
     */
    List<NutsExtensionInfo> findWorkspaceExtensions(NutsSession session);

    /**
     * finds all available extensions (from remote repositories) for the given
     * boot version (aka version of nuts api)
     *
     * @param version boot version
     * @param session current session
     * @return all available extensions
     */
    List<NutsExtensionInfo> findWorkspaceExtensions(String version, NutsSession session);

    List<NutsExtensionInfo> findExtensions(String id, String extensionType, NutsSession session);

    List<NutsExtensionInfo> findExtensions(NutsId id, String extensionType, NutsSession session);

    NutsWorkspaceExtension addWorkspaceExtension(NutsId id, NutsSession session);

    boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl);

    NutsWorkspaceExtension[] getWorkspaceExtensions();

    URL[] getExtensionURLLocations(NutsId id, String appId, String extensionType);

    String[] getExtensionRepositoryLocations(NutsId appId);

    List<Class> discoverTypes(Class type, ClassLoader bootClassLoader);

    <T> List<T> discoverInstances(Class<T> type, ClassLoader bootClassLoader);

    <T extends NutsComponent<B>,B> NutsServiceLoader<T,B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType);

    <T extends NutsComponent<B>,B> NutsServiceLoader<T,B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader);

    <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria);

    <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters);

    <T extends NutsComponent> List<T> createAllSupported(Class<T> type, Object supportCriteria);

    <T> List<T> createAll(Class<T> type);

    Set<Class> getExtensionPoints();

    Set<Class> getExtensionTypes(Class extensionPoint);

    List<Object> getExtensionObjects(Class extensionPoint);

    boolean isRegisteredType(Class extensionPointType, String name);

    boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl);

    <T> boolean registerInstance(Class<T> extensionPoint, T implementation);

    boolean registerType(Class extensionPointType, Class extensionType);

    boolean isRegisteredType(Class extensionPointType, Class extensionType);

    NutsId[] getExtensions();

    boolean addExtension(NutsId extensionId);

    boolean removeExtension(NutsId extensionId);

    boolean updateExtension(NutsId extensionId);

    boolean containsExtension(NutsId extensionId);


}
