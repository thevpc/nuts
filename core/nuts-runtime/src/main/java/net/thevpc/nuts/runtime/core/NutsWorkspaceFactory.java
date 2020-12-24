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
package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsId;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public interface NutsWorkspaceFactory {

    Set<Class> discoverTypes(NutsId id, URL url, ClassLoader bootClassLoader, NutsSession session);
    Set<Class> discoverTypes(NutsId id, URL url, ClassLoader bootClassLoader, Class[] extensionPoints, NutsSession session);

//    Set<Class> discoverTypes(ClassLoader bootClassLoader);

    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, NutsSession session);

    <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters, NutsSession session);

    <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, V supportCriteria, NutsSession session);

    <T> List<T> createAll(Class<T> type, NutsSession session);

//    Set<Class> getExtensionPoints();

    Set<Class> getExtensionTypes(Class extensionPoint, NutsSession session);

    List<Object> getExtensionObjects(Class extensionPoint);

    boolean isRegisteredType(Class extensionPointType, String name, NutsSession session);

    boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl, NutsSession session);

    <T> void registerInstance(Class<T> extensionPoint, T implementation, NutsSession session);

    void registerType(Class extensionPointType, Class extensionType, NutsId source, NutsSession session);

    boolean isRegisteredType(Class extensionPointType, Class extensionType, NutsSession session);

}
