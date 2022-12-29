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
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedCollectionUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public class DefaultNArtifactCallBuilder implements NArtifactCallBuilder, Serializable {

    private static final long serialVersionUID = 1L;

    private NId id;
    private List<String> arguments = new ArrayList<>();
    private final Map<String,String> properties = new LinkedHashMap<>();
    private NSession session;

    public DefaultNArtifactCallBuilder() {
    }

    public DefaultNArtifactCallBuilder(NSession session) {
        this.session=session;
    }

    public DefaultNArtifactCallBuilder(NArtifactCall value, NSession session) {
        this.session=session;
        setId(value.getId());
        setArguments(value.getArguments());
        setProperties(value.getProperties());
    }

    public NId getId() {
        return id;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    @Override
    public DefaultNArtifactCallBuilder setArguments(String... arguments) {
        this.arguments = NReservedCollectionUtils.unmodifiableList(Arrays.asList(arguments));
        return this;
    }

    @Override
    public NArtifactCallBuilder setArguments(List<String> value) {
        this.arguments = NReservedCollectionUtils.unmodifiableList(value);
        return this;
    }

    @Override
    public DefaultNArtifactCallBuilder setProperties(Map<String,String> properties) {
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
    public DefaultNArtifactCallBuilder setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NArtifactCallBuilder set(NArtifactCallBuilder value) {
        return null;
    }

    @Override
    public NArtifactCallBuilder set(NArtifactCall value) {
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
    public NArtifactCallBuilder clear() {
        setId(null);
        setArguments();
        setProperties(null);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNArtifactCallBuilder that = (DefaultNArtifactCallBuilder) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(arguments, that.arguments) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, properties);
        result = 31 * result + Objects.hashCode(arguments);
        return result;
    }

    @Override
    public NArtifactCall build() {
        return new DefaultNArtifactCall(id, arguments, properties);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
