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
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;

import java.util.List;

/**
 * Descriptor Filter Factory
 *
 * @author thevpc
 * @app.category Base
 */
@NComponentScope(NComponentScopeType.SESSION)
public interface NDescriptorFilters extends NTypedFilters<NDescriptorFilter> {
    static NDescriptorFilters of(NSession session) {
       return NExtensions.of(session).createSupported(NDescriptorFilters.class);
    }

    NDescriptorFilter byPackaging(String... values);

    NDescriptorFilter byArch(String... values);

    NDescriptorFilter byOs(String... values);

    NDescriptorFilter byOsDist(String... values);

    NDescriptorFilter byPlatform(String... values);

    NDescriptorFilter byDesktopEnvironment(String... values);

    NDescriptorFilter byFlag(NDescriptorFlag... flags);


    NDescriptorFilter byPackaging(List<String> values);

    NDescriptorFilter byArch(List<String> values);

    NDescriptorFilter byOs(List<String> values);

    NDescriptorFilter byOsDist(List<String> values);

    NDescriptorFilter byPlatform(List<String> values);

    NDescriptorFilter byDesktopEnvironment(List<String> values);

    NDescriptorFilter byFlag(List<NDescriptorFlag> flags);

    NDescriptorFilter byExtension(NVersion apiVersion);

    NDescriptorFilter byRuntime(NVersion apiVersion);

    NDescriptorFilter byCompanion(NVersion apiVersion);

    NDescriptorFilter byApiVersion(NVersion apiVersion);

    NDescriptorFilter byLockedIds(String... ids);
}
