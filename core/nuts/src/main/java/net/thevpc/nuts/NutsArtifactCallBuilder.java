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

import java.io.Serializable;
import java.util.Map;

/**
 * NutsArtifactCallBuilder is responsible of building instances of {@code NutsArtifactCall} to be used
 * as NutsDescriptor executor or installer.
 * To get an instance of NutsArtifactCallBuilder you can use {@code workspace.descriptor().callBuilder()}
 *
 * @since 0.5.4
 * @app.category Base
 */
public interface NutsArtifactCallBuilder extends Serializable {
    static NutsArtifactCallBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.descriptor().callBuilder();
    }

    /**
     * return artifact id
     *
     * @return artifact id
     */
    NutsId getId();

    /**
     * update artifact id
     *
     * @param value artifact id
     * @return {@code this} instance
     */
    NutsArtifactCallBuilder setId(NutsId value);

    /**
     * return call arguments
     *
     * @return call arguments
     */
    String[] getArguments();

    /**
     * update arguments
     *
     * @param value arguments
     * @return {@code this} instance
     */
    NutsArtifactCallBuilder setArguments(String... value);

    /**
     * return call properties map
     *
     * @return call properties map
     */
    Map<String, String> getProperties();

    /**
     * update call properties map (replace all existing properties)
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsArtifactCallBuilder setProperties(Map<String, String> value);

    /**
     * initialize this instance from the given value
     *
     * @param value copy from value
     * @return {@code this} instance
     */
    NutsArtifactCallBuilder set(NutsArtifactCallBuilder value);

    /**
     * initialize this instance from the given value
     *
     * @param value copy from value
     * @return {@code this} instance
     */
    NutsArtifactCallBuilder set(NutsArtifactCall value);

    /**
     * reset this instance to default (null) values
     *
     * @return {@code this} instance
     */
    NutsArtifactCallBuilder clear();

    /**
     * create an immutable instance of {@link NutsArtifactCall}
     * initialized with all of this attributes.
     *
     * @return immutable instance of {@link NutsArtifactCall}
     */
    NutsArtifactCall build();
}
