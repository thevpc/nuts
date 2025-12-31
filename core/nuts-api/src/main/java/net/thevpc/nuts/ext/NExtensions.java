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

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NMutableClassLoader;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NWorkspaceOptions;
import net.thevpc.nuts.io.NServiceLoader;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.*;

import java.util.List;
import java.util.Set;

/**
 * @author thevpc
 * @app.category Extensions
 * @since 0.5.4
 */
public interface NExtensions extends NComponent {
    static <T> T of(Class<T> type) {
        return of().createComponent(type).get();
    }

    static <T> NOptional<T> get(Class<T> type) {
        return get().flatMap(x -> x.createComponent(type));
    }

    static NExtensions of() {
        return NWorkspace.of().extensions();
    }

    static NOptional<NExtensions> get() {
        return NWorkspace.get().map(x -> x.extensions());
    }

    Set<NId> getCompanionIds();

    <T extends NComponent> boolean installWorkspaceExtensionComponent(Class<T> extensionPointType, T extensionImpl);

    Set<Class<?>> discoverTypes(NId id, ClassLoader classLoader);

    <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType);

    <T, B> NServiceLoader<T> createServiceLoader(Class<T> serviceType, Class<B> criteriaType, ClassLoader classLoader);


    NMutableClassLoader createMutableClassLoader(ClassLoader parentClassLoader);

    <T> NScoredValue<T> getTypeScoredValue(Class<? extends T> implType, Class<T> apiType, NScorableContext scorableContext);

    <T> NScoredValue<T> getInstanceScoredValue(T instance, Class<T> apiType, NScorableContext scorableContext);

    <T> NOptional<NScorable> getTypeScorable(Class<? extends T> implType, Class<T> apiType);

    <T> NOptional<NScorable> getInstanceScorable(T instance, Class<T> apiType);

    /**
     * create supported extension implementation or return null.
     *
     * @param <T>  extension type class
     * @param type extension type
     * @return valid instance or null if no extension implementation was found
     */
    <T> NOptional<T> createComponent(Class<T> type);

    /**
     * create supported extension implementation or return null.
     *
     * @param <T>             extension type class
     * @param <V>             extension context type
     * @param type            extension type
     * @param supportCriteria context
     * @return valid instance or null if no extension implementation was found
     */
    <T, V> NOptional<T> createSupported(Class<T> type, V supportCriteria);

    <T, V> List<T> createAllSupported(Class<T> type, V supportCriteria);

    <T> List<T> createAll(Class<T> type);

//    Set<Class> getExtensionPoints(NSession session);


    <T> Set<Class<? extends T>> getExtensionTypes(Class<T> extensionPoint);

    <T> List<T> getExtensionObjects(Class<T> extensionPoint);

    <T> boolean isRegisteredType(Class<T> extensionPointType, String name);

    <T> boolean isRegisteredInstance(Class<T> extensionPointType, T extensionImpl);

    <T> boolean registerInstance(Class<T> extensionPoint, T implementation);

    <T> boolean registerType(Class<T> extensionPointType, Class<? extends T> implementation, NId source);

    <T> boolean isRegisteredType(Class<T> extensionPointType, Class<? extends T> implementation);

    boolean isLoadedId(NId id);

    boolean isLoadedId(NId id, ClassLoader classLoader);

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
