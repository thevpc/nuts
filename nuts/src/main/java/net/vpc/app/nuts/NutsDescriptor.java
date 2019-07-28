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

import java.io.*;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Nuts descriptors define an <strong>immutable</strong> image to all information needed to execute an artifact.
 * It resembles to maven's pom file but it focuses on execution information
 * rather then build information. Common features are inheritance
 * dependencies, standard dependencies, exclusions and properties.
 * However nuts descriptor adds new features such as :
 * <ul>
 *     <li>multiple parent inheritance</li>
 *     <li>executable/nuts-executable flag</li>
 *     <li>environment (arch, os, dist,platform) filters</li>
 *     <li>classifiers may be mapped to environment (think of dlls for windows and so for linux)</li>
 * </ul>
 * A versatile way to change descriptor is to use builder ({@link #builder()}).
 *
 * @since 0.1.0
 */
public interface NutsDescriptor extends Serializable {

    /**
     * artifact full id (groupId+artifactId+version)
     * @return artifact id
     */
    NutsId getId();

    /**
     * descriptor parent list (may be empty)
     * @return descriptor parent list (may be empty)
     */
    NutsId[] getParents();

    /**
     * true if the artifact is executable and is considered an application. if not it is a library.
     * @return true if the artifact is executable
     */
    boolean isExecutable();

    /**
     * true if the artifact is a java executable that implements {@link NutsApplication} interface.
     * @return true if the artifact is a java executable that implements {@link NutsApplication} interface.
     */
    boolean isNutsApplication();

    //    String getAlternative();

    /**
     * return descriptor packaging (used to resolve file extension)
     * @return return descriptor packaging (used to resolve file extension)
     */
    String getPackaging();

    /**
     * supported archs. if empty, all arch are supported (for example for java, all arch are supported).
     * @return supported archs
     */
    String[] getArch();

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     * @return supported oses
     */
    String[] getOs();

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     * @return supported operating system distributions
     */
    String[] getOsdist();

    /**
     * supported platforms (java, dotnet, ...). if empty patform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     * @return supported platforms
     */
    String[] getPlatform();

    /**
     * user friendly name, a short description for the artifact
     * @return user friendly name
     */
    String getName();

    /**
     * long description for the artifact
     * @return long description for the artifact
     */
    String getDescription();

    /**
     * ordered list of classifier mapping used to resolve valid classifier to use of ra given environment.
     * @return ordered list of classifier mapping used to resolve valid classifier to use of ra given environment
     */
    NutsClassifierMapping[] getClassifierMappings();

    /**
     * list of available mirror locations from which nuts can download artifact content.
     * location can be mapped to a classifier.
     * @return list of available mirror locations
     */
    NutsIdLocation[] getLocations();


    /**
     * The dependencies specified here are not used until they are referenced in
     * a POM within the group. This allows the specification of a
     * &quot;standard&quot; version for a particular. This corresponds to
     * "dependencyManagement.dependencies" in maven
     *
     * @return "standard" dependencies
     */
    NutsDependency[] getStandardDependencies();

    /**
     * list of immediate (non inherited and non transitive dependencies
     * @return list of immediate (non inherited and non transitive dependencies
     */
    NutsDependency[] getDependencies();

    /**
     * descriptor of artifact responsible of running this artifact
     * @return descriptor of artifact responsible of running this artifact
     */
    NutsExecutorDescriptor getExecutor();

    /**
     * descriptor of artifact responsible of installing this artifact
     * @return descriptor of artifact responsible of installing this artifact
     */
    NutsExecutorDescriptor getInstaller();

    /**
     * custom properties that can be used as place holders (int ${name} form) in other fields.
     * @return custom properties that can be used as place holders (int ${name} form) in other fields.
     */
    Map<String, String> getProperties();

    /**
     * create new builder filled with this descriptor fields.
     * @return new builder filled with this descriptor fields.
     */
    NutsDescriptorBuilder builder();

}
