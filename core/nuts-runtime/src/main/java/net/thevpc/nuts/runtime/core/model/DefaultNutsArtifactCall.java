/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.NutsArtifactCall;
import net.thevpc.nuts.NutsId;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public class DefaultNutsArtifactCall implements NutsArtifactCall, Serializable {

    private static final long serialVersionUID = 1L;

    private final NutsId id;
    private final String[] arguments;
    private final Map<String, String> properties;

    public DefaultNutsArtifactCall(NutsArtifactCall other) {
        this.id = other.getId();
        this.arguments = other.getArguments();
        this.properties = other.getProperties();
    }

    public DefaultNutsArtifactCall(NutsId id) {
        this(id, null, null);
    }

    public DefaultNutsArtifactCall(NutsId id, String[] options) {
        this(id, options, null);
    }

    public DefaultNutsArtifactCall(NutsId id, String[] options, Map<String, String> properties) {
        this.id = id;
        this.arguments = options == null ? new String[0] : options;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    public NutsId getId() {
        return id;
    }

    public String[] getArguments() {
        return arguments;
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultNutsArtifactCall that = (DefaultNutsArtifactCall) o;
        return Objects.equals(id, that.id)
                && Arrays.equals(arguments, that.arguments)
                && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, properties);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}
