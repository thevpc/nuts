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