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
package net.thevpc.nuts.ext;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NServiceLoader;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Set;

/**
 * @author thevpc
 * @app.category Extensions
 * @since 0.5.4
 */
public interface NExtensions extends NComponent  {
    static <T extends NComponent> T of(Class<T> type) {
        return of().createComponent(type).get();
    }

    static NExtensions of() {
        return NWorkspace.get().get().extensions();
    }

    Set<NId> getCompanionIds();

    <T extends NComponent> boolean installWorkspaceExtensionComponent(Class<T> extensionPointType, T extensionImpl);

    Set<Class<? extends NComponent>> discoverTypes(NId id, ClassLoader classLoader);

    <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType);

    <T extends NComponent, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader);

    /**
     * create supported extension implementation or return null.
     *
     * @param <T>             extension type class
     * @param type            extension type
     * @return valid instance or null if no extension implementation was found
     */
    <T extends NComponent> NOptional<T> createComponent(Class<T> type);

    /**
     * create supported extension implementation or return null.
     *
     * @param <T>             extension type class
     * @param <V>             extension context type
     * @param type            extension type
     * @param supportCriteria context
     * @return valid instance or null if no extension implementation was found
     */
    <T extends NComponent, V> NOptional<T> createComponent(Class<T> type, V supportCriteria);

    <T extends NComponent, V> List<T> createComponents(Class<T> type, V supportCriteria);

    <T extends NComponent> List<T> createAll(Class<T> type);

//    Set<Class> getExtensionPoints(NutsSession session);


    <T extends NComponent> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint);

    <T extends NComponent> List<T> getExtensionObjects(Class<T> extensionPoint);

    <T extends NComponent> boolean isRegisteredType(Class<T> extensionPointType, String name);

    <T extends NComponent> boolean isRegisteredInstance(Class<T> extensionPointType, T extensionImpl);

    <T extends NComponent> boolean registerInstance(Class<T> extensionPoint, T implementation);

    <T extends NComponent> boolean registerType(Class<T> extensionPointType, Class<? extends T> implementation, NId source);

    <T extends NComponent> boolean isRegisteredType(Class<T> extensionPointType, Class<? extends T> implementation);

    boolean isLoadedId(NId id);

    boolean isLoadedId(NId id,ClassLoader classLoader);

    boolean isLoadedExtensions(NId id);

    List<NId> getLoadedExtensions();

    NExtensions loadExtension(NId extension);

    NExtensions unloadExtension(NId extension);

    /**
     * return loaded extensions
     *
     * @return extension ids
     */
    List<NId> getConfigExtensions();

    boolean isExcludedExtension(String extensionId, NWorkspaceOptions options);

}
