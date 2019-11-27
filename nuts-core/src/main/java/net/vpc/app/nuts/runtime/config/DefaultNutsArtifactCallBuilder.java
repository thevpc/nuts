/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.config;

import net.vpc.app.nuts.NutsArtifactCall;
import net.vpc.app.nuts.NutsArtifactCallBuilder;
import net.vpc.app.nuts.NutsId;

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

    public DefaultNutsArtifactCallBuilder() {
    }

    public DefaultNutsArtifactCallBuilder(NutsArtifactCall value) {
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
    public NutsArtifactCallBuilder id(NutsId value) {
        return setId(value);
    }

    @Override
    public NutsArtifactCallBuilder arguments(String... value) {
        return setArguments(value);
    }

    @Override
    public NutsArtifactCallBuilder properties(Map<String,String> value) {
        return setProperties(value);
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
}
