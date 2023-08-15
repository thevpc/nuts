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
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.NId;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public interface NWorkspaceFactory {

    Set<Class> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, NSession session);

    Set<Class> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, Class[] extensionPoints, NSession session);

    <T extends NComponent> NOptional<T> createComponent(Class<T> type, Object supportCriteria, NSession session);

    <T extends NComponent> List<T> createComponents(Class<T> type, Object supportCriteria, NSession session);

    <T> List<T> createAll(Class<T> type, NSession session);

    <T> T createFirst(Class<T> type, NSession session);

    Set<Class> getExtensionTypes(Class extensionPoint, NSession session);

    List<Object> getExtensionObjects(Class extensionPoint);

    boolean isRegisteredType(Class extensionPointType, String name, NSession session);

    boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl, NSession session);

    <T> void registerInstance(Class<T> extensionPoint, T implementation, NSession session);

    void registerType(Class extensionPointType, Class extensionType, NId source, NSession session);

    boolean isRegisteredType(Class extensionPointType, Class extensionType, NSession session);

}
