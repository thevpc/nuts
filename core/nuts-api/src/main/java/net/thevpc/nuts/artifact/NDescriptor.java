/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.app.NApplication;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Nuts descriptors define an <strong>immutable</strong> image to all information needed to execute an artifact.
 * It resembles to maven's pom file, but it focuses on execution information
 * rather than build information. Common features are inheritance
 * dependencies, standard dependencies, exclusions and properties.
 * However, nuts descriptor adds new features such as :
 * <ul>
 *     <li>multiple parent inheritance</li>
 *     <li>executable/nuts-executable flag</li>
 *     <li>environment (arch, os, dist,platform) filters</li>
 *     <li>classifiers may be mapped to environment (think of dlls for windows and so for linux)</li>
 * </ul>
 * A versatile way to change descriptor is to use builder ({@link #builder()}).
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.1.0
 */
public interface NDescriptor extends Serializable, NBlankable{

    /**
     * artifact full id (groupId+artifactId+version)
     *
     * @return artifact id
     */
    @NGetter
    NId id();

    /**
     * descriptor parent list (maybe empty)
     *
     * @return descriptor parent list (maybe empty)
     */
    @NGetter
    List<NId> parents();

    /**
     * true if the artifact is executable and is considered an application. if not It's a library.
     *
     * @return true if the artifact is executable
     */
    boolean isExecutable();

    /**
     * true if the artifact is a java executable that implements {@link NApplication} interface.
     *
     * @return true if the artifact is a java executable that implements {@link NApplication} interface.
     */
    boolean isNutsApplication();

    boolean isPlatformApplication();

    /**
     * return descriptor flags
     *
     * @return descriptor flags
     * @since 0.8.3
     */
    @NGetter
    Set<NDescriptorFlag> flags();


    /**
     * return id type
     *
     * @return id type
     */
    @NGetter
    NIdType idType();

    /**
     * return descriptor packaging (used to resolve file extension)
     *
     * @return return descriptor packaging (used to resolve file extension)
     */
    @NGetter
    String packaging();

    /**
     * This is typically the case for pom projects
     *
     * @return true when the descriptor does not define a content.
     */
    boolean isNoContent();

    /**
     * dependency resolution solver. defaults to 'maven'
     *
     * @return dependency resolution solver
     */
    String solver();

    /**
     * Descriptor Condition
     *
     * @return Descriptor Condition
     */
    @NGetter
    NEnvCondition condition();

    /**
     * user-friendly name, a short description for the artifact
     *
     * @return user friendly name
     */
    @NGetter
    String name();

    /**
     * url (external or classpath url) to the application Icon
     *
     * @return url (external or classpath url) to the application Icon
     */
    @NGetter
    List<String> icons();

    /**
     * Generic Artifact Name (like 'Text Editor', 'Image Processing Application', etc)
     *
     * @return Generic Artifact Name
     */
    @NGetter
    String genericName();

    /**
     * category path of the artifact (slash separated).
     * Standard Category Names should be used.
     *
     * @return category path of the artifact
     */
    @NGetter
    List<String> categories();

    /**
     * long description for the artifact
     *
     * @return long description for the artifact
     */
    String description();

    /**
     * list of available mirror locations from which nuts can download artifact content.
     * location can be mapped to a classifier.
     *
     * @return list of available mirror locations
     */
    @NGetter
    List<NIdLocation> locations();


    /**
     * The dependencies specified here are not used until they are referenced in
     * a POM within the group. This allows the specification of a
     * &quot;standard&quot; version for a particular. This corresponds to
     * "dependencyManagement.dependencies" in maven
     *
     * @return "standard" dependencies
     */
    @NGetter
    List<NDependency> standardDependencies();

    /**
     * list of immediate (non-inherited and non-transitive dependencies
     *
     * @return list of immediate (non-inherited and non-transitive dependencies
     */
    @NGetter
    List<NDependency> dependencies();

    /**
     * descriptor of artifact responsible for running this artifact
     *
     * @return descriptor of artifact responsible for running this artifact
     */
    @NGetter
    NArtifactCall executor();

    /**
     * descriptor of artifact responsible for installing this artifact
     *
     * @return descriptor of artifact responsible for installing this artifact
     */
    @NGetter
    NArtifactCall installer();

    /**
     * custom properties that can be used as placeholders (int ${name} form) in other fields.
     *
     * @return custom properties that can be used as placeholders (int ${name} form) in other fields.
     */
    @NGetter
    List<NDescriptorProperty> properties();

    /**
     * custom property
     *
     * @param name name
     * @return custom property value by name
     * @since 0.8.3
     */
    NOptional<NDescriptorProperty> getProperty(String name);

    /**
     * custom property
     *
     * @param name name
     * @return custom property value by name
     * @since 0.8.3
     */
    NOptional<NLiteral> getPropertyValue(String name);

    /**
     * create new builder filled with this descriptor fields.
     *
     * @return new builder filled with this descriptor fields.
     */
    NDescriptorBuilder builder();

    /**
     * @return contributors
     * @since 0.8.4
     */
    @NGetter
    List<NDescriptorContributor> contributors();

    /**
     * @return developers
     * @since 0.8.4
     */
    @NGetter
    List<NDescriptorContributor> developers();

    /**
     * @return licenses
     * @since 0.8.4
     */
    @NGetter
    List<NDescriptorLicense> licenses();

    /**
     * @return mailing lists
     * @since 0.8.4
     */
    @NGetter
    List<NDescriptorMailingList> mailingLists();

    /**
     * @return organization
     * @since 0.8.4
     */
    @NGetter
    NDescriptorOrganization organization();

}
