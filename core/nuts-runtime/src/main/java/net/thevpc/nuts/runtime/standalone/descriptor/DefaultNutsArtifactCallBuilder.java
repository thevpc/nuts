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
 *
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
package net.thevpc.nuts.runtime.standalone.descriptor;

import net.thevpc.nuts.NutsArtifactCall;
import net.thevpc.nuts.NutsArtifactCallBuilder;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public class DefaultNutsArtifactCallBuilder implements NutsArtifactCallBuilder, Serializable {

    private static final long serialVersionUID = 1L;

    private NutsId id;
    private String[] arguments = new String[0];
    private final Map<String,String> properties = new LinkedHashMap<>();
    private NutsSession session;

    public DefaultNutsArtifactCallBuilder() {
    }

    public DefaultNutsArtifactCallBuilder(NutsSession session) {
        this.session=session;
    }

    public DefaultNutsArtifactCallBuilder(NutsArtifactCall value,NutsSession session) {
        this.session=session;
        setId(value.getId());
        setArguments(value.getArguments());
        setProperties(value.getProperties());
    }

    public NutsId getId() {
        return id;
    }

    public String[] getArguments() {
        return arguments;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    @Override
    public DefaultNutsArtifactCallBuilder setArguments(String... arguments) {
        this.arguments = arguments == null ? new String[0] : arguments;
        return this;
    }

    @Override
    public DefaultNutsArtifactCallBuilder setProperties(Map<String,String> properties) {
        this.properties.clear();
        if(properties!=null) {
            for (Map.Entry<String, String> stringStringEntry : properties.entrySet()) {
                if (stringStringEntry.getValue() != null) {
                    this.properties.put(stringStringEntry.getKey(), stringStringEntry.getValue());
                } else {
                    this.properties.remove(stringStringEntry.getKey());
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsArtifactCallBuilder setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsArtifactCallBuilder set(NutsArtifactCallBuilder value) {
        return null;
    }

    @Override
    public NutsArtifactCallBuilder set(NutsArtifactCall value) {
        if(value!=null){
            setId(value.getId());
            setArguments(value.getArguments());
            setProperties(value.getProperties());
        }else{
            clear();
        }
        return this;
    }

    @Override
    public NutsArtifactCallBuilder clear() {
        setId(null);
        setArguments();
        setProperties(null);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsArtifactCallBuilder that = (DefaultNutsArtifactCallBuilder) o;
        return Objects.equals(id, that.id) &&
                Arrays.equals(arguments, that.arguments) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, properties);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public NutsArtifactCall build() {
        return new DefaultNutsArtifactCall(id, arguments, properties);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
