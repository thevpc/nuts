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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.spi.NComponent;

import java.util.Map;

/**
 * @author thevpc
 * @app.category Format
 * @since 0.5.4
 */
public interface NVersionFormat extends NFormat, NComponent {

    static NVersionFormat of(NSession session) {
       return NExtensions.of(session).createComponent(NVersionFormat.class).get();
    }

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NVersionFormat setSession(NSession session);

    @Override
    NVersionFormat setNtf(boolean ntf);

    NVersionFormat addProperty(String key, String value);

    NVersionFormat addProperties(Map<String, String> p);

    /**
     * return version set by {@link #setVersion(NVersion) }
     *
     * @return version set by {@link #setVersion(NVersion) }
     */
    NVersion getVersion();

    /**
     * set version to print. if null, workspace version will be considered.
     *
     * @param version version to print
     * @return {@code this} instance
     */
    NVersionFormat setVersion(NVersion version);

    /**
     * return true if version is null (default). In such case, workspace version
     * is considered.
     *
     * @return true if version is null (default)
     */
    boolean isWorkspaceVersion();
}
