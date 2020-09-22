/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.config;

import net.vpc.app.nuts.NutsArtifactCall;
import net.vpc.app.nuts.NutsId;

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
    private final String[] options;
    private final Map<String,String> properties;

    public DefaultNutsArtifactCall(NutsId id) {
        this(id, null, null);
    }

    public DefaultNutsArtifactCall(NutsId id, String[] options) {
        this(id, options, null);
    }

    public DefaultNutsArtifactCall(NutsId id, String[] options, Map<String,String> properties) {
        this.id = id;
        this.options = options == null ? new String[0] : options;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    public NutsId getId() {
        return id;
    }

    public String[] getArguments() {
        return options;
    }

    public Map<String,String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsArtifactCall that = (DefaultNutsArtifactCall) o;
        return Objects.equals(id, that.id) &&
                Arrays.equals(options, that.options) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, properties);
        result = 31 * result + Arrays.hashCode(options);
        return result;
    }
}
