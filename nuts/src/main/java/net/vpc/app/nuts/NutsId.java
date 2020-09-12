/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Map;

/**
 * Immutable Artifact id information.
 * @author vpc
 * @since 0.1.0
 * @category Descriptor
 */
public interface NutsId extends NutsTokenFilter, Serializable, Comparable<NutsId> {

    /**
     * true if other has exact shot name than {@code this}
     * @param other other id
     * @return true if other has exact shot name than {@code this}
     */
    boolean equalsShortName(NutsId other);

    /**
     * non null group id token
     * @return non null group id token
     */
    NutsTokenFilter groupIdToken();

    /**
     * non null properties query token
     * @return non null properties query token
     */
    NutsTokenFilter propertiesToken();

    /**
     * non null version token
     * @return non null version token
     */
    NutsTokenFilter versionToken();

    /**
     * non null artifact id token
     * @return non null artifact id token
     */
    NutsTokenFilter artifactIdToken();

    /**
     * non null namespace non null namespace token
     * @return non null namespace non null namespace token
     */
    NutsTokenFilter namespaceToken();

    /**
     * non null token filter that searches in all id fields
     * @return non null token filter that searches in all id fields
     */
    NutsTokenFilter anyToken();

    /**
     * id face define is a release file type selector of the id.
     * It helps discriminating content (jar) from descriptor, from other (hash,...)
     * files released for the very same  artifact.
     * @return id face selector
     */
    String getFace();

    /**
     * os supported by the artifact
     * @return os supported by the artifact
     */
    String getOs();

    /**
     * os distribution supported by the artifact
     * @return os distribution supported by the artifact
     */
    String getOsdist();

    /**
     * platform supported by the artifact
     * @return platform supported by the artifact
     */
    String getPlatform();

    /**
     * hardware architecture supported by the artifact
     * @return hardware architecture supported by the artifact
     */
    String getArch();

    /**
     * properties in the url query form
     * @return properties in the url query form.
     */
    String getPropertiesQuery();

    /**
     * properties as map.
     * @return properties as map.
     */
    Map<String, String> getProperties();

    /**
     * artifact namespace (usually repository name or id)
     * @return artifact namespace (usually repository name or id)
     */
    String getNamespace();

    /**
     * artifact group which identifies uniquely projects and group of projects.
     * @return artifact group which identifies uniquely projects and group of projects.
     */
    String getGroupId();

    /**
     * return a string representation of this id. All of group, name, version,
     * namespace, queryMap values are printed. This method is equivalent to
     * {@link Object#toString()}
     *
     * @return string representation of this id
     */
    String getFullName();

    /**
     * return a string concatenation of group, name and version,
     * ignoring namespace, and queryMap values. An example of long name is
     * <code>my-group:my-artifact#my-version?alt</code>
     *
     * @return group id, artifact id and version only Id instance
     */
    String getLongName();

    /**
     * returns a string concatenation of group and name (':' separated) ignoring
     * version,namespace, and queryMap values. In group is empty or null, name
     * is returned. Ann null values are trimmed to "" An example of simple name
     * is <code>my-group:my-artifact</code>
     *
     * @return group id and artifact id
     */
    String getShortName();

    /**
     * return a new instance of NutsId defining only group and name ignoring
     * version,namespace, and queryMap values.
     *
     * @return group and name only Id instance
     */
    NutsId getShortNameId();

    /**
     * return a new instance of NutsId defining only group, name and version,
     * ignoring namespace, and queryMap values.
     *
     * @return group, name and version only Id instance
     */
    NutsId getLongNameId();

    /**
     * return name part of this id
     *
     * @return return name part of this id
     */
    String getArtifactId();

    /**
     * tag used to distinguish between different artifacts that were built from the same source code
     * @return tag used to distinguish between different artifacts that were built from the same source code
     */
    String getClassifier();

    /**
     * artifact version (never null)
     * @return artifact version (never null)
     */
    NutsVersion getVersion();

    /**
     * create a filter based on this id
     * @return a filter based on this id
     */
    NutsIdFilter filter();

    /**
     * create a builder (mutable id) based on this id
     * @return a new instance of builder (mutable id) based on this id
     */
    NutsIdBuilder builder();

}
