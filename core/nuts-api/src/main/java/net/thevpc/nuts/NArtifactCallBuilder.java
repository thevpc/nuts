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
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.io.Serializable;
import java.util.List;

/**
 * NutsArtifactCallBuilder is responsible for building instances of {@code NutsArtifactCall} to be used
 * as NutsDescriptor executor or installer.
 * To get an instance of NutsArtifactCallBuilder you can use {@code workspace.descriptor().callBuilder()}
 *
 * @app.category Base
 * @since 0.5.4
 */
public interface NArtifactCallBuilder extends Serializable, NComponent {

    /**
     * Creates a new instance of {@code NArtifactCallBuilder}.
     *
     * @return a new instance of {@code NArtifactCallBuilder}.
     */
    static NArtifactCallBuilder of() {
       return NExtensions.of(NArtifactCallBuilder.class);
    }

    /**
     * return artifact id
     *
     * @return artifact id
     */
    NId getId();

    /**
     * update artifact id
     *
     * @param value artifact id
     * @return {@code this} instance
     */
    NArtifactCallBuilder setId(NId value);

    /**
     * return call arguments
     *
     * @return call arguments
     */
    List<String> getArguments();

    /**
     * update arguments
     *
     * @param value arguments
     * @return {@code this} instance
     */
    NArtifactCallBuilder setArguments(String... value);

    /**
     * Updates the call arguments with the provided list of values.
     *
     * @param value the list of arguments to set
     * @return {@code this} instance
     */
    NArtifactCallBuilder setArguments(List<String> value);

    /**
     * initialize this instance from the given value
     *
     * @param value copy from value
     * @return {@code this} instance
     */
    NArtifactCallBuilder set(NArtifactCallBuilder value);

    /**
     * initialize this instance from the given value
     *
     * @param value copy from value
     * @return {@code this} instance
     */
    NArtifactCallBuilder set(NArtifactCall value);

    /**
     * reset this instance to default (null) values
     *
     * @return {@code this} instance
     */
    NArtifactCallBuilder clear();

    /**
     * create an immutable instance of {@link NArtifactCall}
     * initialized with all of this attributes.
     *
     * @return immutable instance of {@link NArtifactCall}
     */
    NArtifactCall build();
}
