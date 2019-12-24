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
 * NutsDependency is an <strong>immutable</strong> object that contains all information about a component's dependency.
 * @author vpc
 * @since 0.5.3
 */
public interface NutsDependency extends Serializable{

    /**
     * return mutable id builder instance initialized with {@code this} instance.
     * @return mutable id builder instance initialized with {@code this} instance
     */
    NutsDependencyBuilder builder();

    /**
     * true if this dependency is optional.
     * equivalent to {@code Boolean.parseBoolean(getOptional())}
     * @return true if this dependency is optional.
     */
    boolean isOptional();

    /**
     * Indicates the dependency is optional for use of this library.
     * @return string representation (or $ var) that can be evaluated as 'true'
     */
    String getOptional();

    /**
     * get scope string value (may be $ var).
     * @return scope string value (may be $ var)
     */
    String getScope();

    /**
     * get classifier string value (may be $ var)
     * @return classifier string
     */
    String getClassifier();

    /**
     * convert to NutsId
     * @return converted to NutsId
     */
    NutsId getId();

    /**
     * return namespace
     * @return namespace
     */
    String getNamespace();


    /**
     * return artifact group id (aka groupId in maven)
     * @return artifact group id (aka groupId in maven)
     */
    String getGroupId();

    /**
     * return artifact id (aka artifactId)
     * @return artifact id (aka artifactId in maven)
     */
    String getArtifactId();

    /**
     * return dependency full name in the form
     * group:name
     *
     * @return return dependency short name
     */
    String getSimpleName();

    /**
     * return dependency full name in the form
     * group:name#version
     *
     * @return return dependency long name
     */
    String getLongName();

    /**
     * return dependency full name in the form
     * namespace://group:name#version?scope=&lt;scope&gt;{@code &}optional=&lt;optional&gt;
     *
     * @return return dependency full name
     */
    String getFullName();

    /**
     * return dependency version
     * @return return dependency version
     */
    NutsVersion getVersion();


    /**
     * dependency exclusions
     * @return dependency exclusions
     */
    NutsId[] getExclusions();

    /**
     * properties in the url query form
     * @return properties in the url query form.
     * @since 0.5.7
     */
    String getPropertiesQuery();

    /**
     * properties in the url query form
     * @return properties in the url query form.
     * @since 0.5.7
     */
    Map<String, String> getProperties();
}
