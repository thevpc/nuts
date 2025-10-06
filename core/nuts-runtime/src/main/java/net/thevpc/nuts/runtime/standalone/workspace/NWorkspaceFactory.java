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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.artifact.NId;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public interface NWorkspaceFactory {

    Set<Class<? extends NComponent>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader);

    Set<Class<? extends NComponent>> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, Class<? extends NComponent>[] extensionPoints);

    <T extends NComponent> NOptional<T> createComponent(Class<T> type, Object supportCriteria);

    <T extends NComponent> List<T> createComponents(Class<T> type, Object supportCriteria);

    <T extends NComponent> List<T> createAll(Class<T> type);

    <T extends NComponent> T createFirst(Class<T> type);

    <T extends NComponent> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint);

    <T extends NComponent> List<T> getExtensionObjects(Class<T> extensionPoint);

    <T extends NComponent> boolean isRegisteredType(Class<T> extensionPointType, String name);

    <T extends NComponent> boolean isRegisteredInstance(Class<T> extensionPointType, T extensionImpl);

    <T extends NComponent> void registerInstance(Class<T> extensionPoint, T implementation);

    <T extends NComponent> void registerType(Class<T> extensionPointType, Class<? extends T> implementationType, NId source);

    <T extends NComponent> boolean isRegisteredType(Class<T> extensionPointType, Class<? extends T> implementationType);

}
