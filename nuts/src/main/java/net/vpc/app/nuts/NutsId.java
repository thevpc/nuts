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
import java.util.function.Function;

/**
 *
 * @author vpc
 * @since 0.1.0
 */
public interface NutsId extends NutsTokenFilter, Serializable, Comparable<NutsId> {

    boolean equalsSimpleName(NutsId other);

    NutsTokenFilter groupIdToken();

    NutsTokenFilter propertiesToken();

    NutsTokenFilter versionToken();

    NutsTokenFilter artifactIdToken();

    NutsTokenFilter namespaceToken();

    NutsTokenFilter anyToken();

    NutsId setGroupId(String newGroupId);

    NutsId setNamespace(String newNamespace);

    NutsId setVersion(NutsVersion newVersion);

    NutsId setVersion(String newVersion);

    NutsId setArtifactId(String newName);

    String getFace();

    String getScope();

//    String getAlternative();

    NutsId setScope(String value);

    NutsId setOptional(String value);

//    NutsId setAlternative(String value);

    NutsId setArch(String value);

    NutsId setFace(String value);

    NutsId setPackaging(String value);

    NutsId setPlatform(String value);

    NutsId setOsdist(String value);

    NutsId setOs(String value);

    String getOs();

    String getOsdist();

    String getPlatform();

    String getArch();

    NutsId setFaceContent();

    NutsId setFaceDescriptor();

    NutsId setProperty(String property, String value);

//    NutsId setProperties(Map<String, String> queryMap, boolean merge);

    NutsId setProperties(Map<String, String> queryMap);

    NutsId addProperties(Map<String, String> queryMap);

    NutsId setProperties(String properties);

    NutsId addProperties(String properties);

    String getPropertiesQuery();

    Map<String, String> getProperties();

    String getNamespace();

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
     * return a new instance of NutsId defining only group, name and version,
     * ignoring namespace, and queryMap values. An example of long name is
     * <code>my-group:my-artifact#my-version?alt</code>
     *
     * @return group, name and version only Id instance
     */
    String getLongName();

    /**
     * returns a string concatenation of group and name (':' separated) ignoring
     * version,namespace, and queryMap values. In group is empty or null, name
     * is returned. Ann null values are trimmed to "" An example of simple name
     * is <code>my-group:my-artifact</code>
     *
     * @return group and name
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

    String getClassifier();

    NutsVersion getVersion();

    String getOptional();

    NutsId apply(Function<String, String> properties);

    NutsIdBuilder builder();

    boolean isOptional();

    NutsIdFilter filter();
}
