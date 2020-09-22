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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * Definition is an <strong>immutable</strong> object that contains all information about a artifact identified by it's Id.
 *
 * @since 0.5.4
 * @category Base
 */
public interface NutsDefinition extends Serializable, Comparable<NutsDefinition> {

    /**
     * artifact id
     *
     * @return artifact id
     */
    NutsId getId();

    /**
     * return artifact type
     * @return return artifact type
     */
    NutsIdType getType();

    /**
     * return artifact descriptor
     * @return artifact descriptor
     */
    NutsDescriptor getDescriptor();

    /**
     * return artifact content file info (including path).
     * this is an <strong>optional</strong> property. It must be requested (see {@link NutsSearchCommand#setContent(boolean)}) to be available.
     *
     * @return artifact content file info
     * @throws NutsElementNotFoundException if the property is not requested
     */
    NutsContent getContent();

    /**
     * return artifact content file path.
     * this is an <strong>optional</strong> property. It must be requested (see {@link NutsSearchCommand#setContent(boolean)}) to be available.
     *
     * @return artifact content file path
     * @throws NutsElementNotFoundException if the property is not requested
     */
    Path getPath();

    /**
     * return artifact install information.
     *
     * @return artifact install information
     * @throws NutsElementNotFoundException if the property is not requested
     */
    NutsInstallInformation getInstallInformation();

    /**
     * return artifact effective descriptor.
     * this is an <strong>optional</strong> property.
     * It must be requested (see {@link NutsSearchCommand#setEffective(boolean)} to be available).
     *
     * @return artifact effective descriptor
     * @throws NutsElementNotFoundException if the property is not requested
     */
    NutsDescriptor getEffectiveDescriptor();

    /**
     * return all or some of the transitive dependencies of the current Nuts as List
     * result of the search command
     * this is an <strong>optional</strong> property.
     * It must be requested (see {@link NutsSearchCommand#setDependencies(boolean)} to be available.
     *
     * @return all or some of the transitive dependencies of the current Nuts as List
     * result of the search command.
     * @throws NutsElementNotFoundException if the property is not requested
     */
    NutsDependency[] getDependencies();

    /**
     * return all of some of the transitive dependencies of the current Nuts as Tree result of the search command
     * this is an <strong>optional</strong> property.
     * It must be requested (see {@link NutsSearchCommand#setDependenciesTree(boolean)} to be available.
     *
     * @return all of some of the transitive dependencies of the current Nuts as Tree result of the search command.
     * @throws NutsElementNotFoundException if the property is not requested
     */
    NutsDependencyTreeNode[] getDependencyNodes();

    /**
     * return target api id (included in dependency) for the current id.
     * This is relevant for runtime, extension and companion ids.
     * For other regular ids, this returns null.
     *
     * @return target (included in dependency) api id for the current id
     */
    NutsId getApiId();

    /**
     * Compares this object with the specified definition for order.
     * This is equivalent to comparing subsequent ids.
     *
     * @param other other definition to compare with
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    int compareTo(NutsDefinition other);

    /**
     * id of the repository providing this id.
     * @return id of the repository providing this id.
     */
    String getRepositoryUuid();

    /**
     * name of the repository providing this id.
     * @return name of the repository providing this id.
     */
    String getRepositoryName();


    /**
     * true if requested content
     *
     * @return true if requested content
     */
    boolean isSetDependencyNodes();

    /**
     * true if requested content
     *
     * @return true if requested content
     */
    boolean isSetDependencies();

    /**
     * true if requested effective descriptor
     *
     * @return true if requested content
     */
    boolean isSetEffectiveDescriptor();

}
