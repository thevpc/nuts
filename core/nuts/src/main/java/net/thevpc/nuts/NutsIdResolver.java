/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.util.List;

/**
 * Id Resolver from classpath
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.8.3
 */
public interface NutsIdResolver extends NutsComponent {

    static NutsIdResolver of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsIdResolver.class, true, null);
    }

    /**
     * detect nuts id from resources containing the given class
     * or null if not found. If multiple resolutions return the first.
     *
     * @param clazz to search for
     * @return nuts id detected from resources containing the given class
     */
    NutsId resolveId(Class clazz);

    /**
     * detect all nuts ids from resources containing the given class.
     *
     * @param clazz to search for
     * @return all nuts ids detected from resources containing the given class
     */
    List<NutsId> resolveIds(Class clazz);
}
