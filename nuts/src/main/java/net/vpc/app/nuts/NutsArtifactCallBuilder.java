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
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Map;

/**
 * NutsArtifactCallBuilder is responsible of building instances of {@code NutsArtifactCall} to be used
 * as NutsDescriptor executor or installer.
 * To get an instance of NutsArtifactCallBuilder you can use {@code workspace.descriptor().callBuilder()}
 *
 * @since 0.5.4
 * @category Base
 */
public interface NutsArtifactCallBuilder extends Serializable {

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
