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

import java.io.*;
import java.util.Map;

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
 * @app.category Descriptor
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
    boolean isApplication();

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
     * supported platforms (java, dotnet, ...). if empty platform is not relevant.
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
     * url (external or classpath url) to the application Icon
     * @return url (external or classpath url) to the application Icon
     */
    String getIcon();

    /**
     * Generic Artifact Name (like 'Text Editor', 'Image Processing Application', etc)
     * @return Generic Artifact Name
     */
    String getGenericName();

    /**
     * category path of the artifact (slash separated).
     * Standard Category Names should be used.
     * @return category path of the artifact
     */
    String getCategory();

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
    NutsArtifactCall getExecutor();

    /**
     * descriptor of artifact responsible of installing this artifact
     * @return descriptor of artifact responsible of installing this artifact
     */
    NutsArtifactCall getInstaller();

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
