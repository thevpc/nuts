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
package net.thevpc.nuts.runtime.core;

import java.util.List;
import java.util.Set;
import net.thevpc.nuts.NutsComponent;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public interface NutsWorkspaceFactory {

    List<Class> discoverTypes(ClassLoader bootClassLoader);

    List<Class> getImplementationTypes(Class type);

    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria);

    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters);

    <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, V supportCriteria);

    <T> List<T> createAll(Class<T> type);

    Set<Class> getExtensionPoints();

    Set<Class> getExtensionTypes(Class extensionPoint);

    List<Object> getExtensionObjects(Class extensionPoint);

    boolean isRegisteredType(Class extensionPointType, String name);

    boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl);

    <T> void registerInstance(Class<T> extensionPoint, T implementation);

    void registerType(Class extensionPointType, Class extensionType);

    boolean isRegisteredType(Class extensionPointType, Class extensionType);

}
