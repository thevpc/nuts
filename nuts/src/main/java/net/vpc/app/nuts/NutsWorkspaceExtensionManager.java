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

import java.util.List;
import java.util.Set;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsWorkspaceExtensionManager {

    boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl);

    List<Class> discoverTypes(ClassLoader classLoader);

    List<Class> getImplementationTypes(Class type);

    <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType);

    <T extends NutsComponent<B>, B> NutsServiceLoader<T, B> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader);

    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria);

    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters);

    <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, NutsSupportLevelContext<V> supportCriteria);

    <T> List<T> createAll(Class<T> type);

    Set<Class> getExtensionPoints();

    Set<Class> getExtensionTypes(Class extensionPoint);

    List<Object> getExtensionObjects(Class extensionPoint);

    boolean isRegisteredType(Class extensionPointType, String name);

    boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl);

    <T> boolean registerInstance(Class<T> extensionPoint, T implementation);

    boolean registerType(Class extensionPointType, Class extensionType);

    boolean isRegisteredType(Class extensionPointType, Class extensionType);

    /**
     * reurn loaded extensions
     *
     * @return
     */
    NutsId[] getExtensions();

}
