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
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Nuts descriptors define a <strong>mutable</strong> image to all information needed to execute an artifact.
 * It help creating an instance of {@link NDescriptor} by calling {@link #build()}
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NDescriptorBuilder extends Serializable, NBlankable, NComponent {

    static NDescriptorBuilder of() {
        return NExtensions.of(NDescriptorBuilder.class);
    }


    /**
     * artifact full id (groupId+artifactId+version)
     *
     * @return artifact id
     */
    @NGetter
    NId id();


    /**
     * true if the artifact is executable and is considered an application. if not It's a library.
     *
     * @return true if the artifact is executable
     */
    @NGetter
    boolean isExecutable();

    /**
     * true if the artifact is a java executable that implements {@link NApplication} interface.
     *
     * @return true if the artifact is a java executable that implements {@link NApplication} interface.
     */
    @NGetter
    boolean isNutsApplication();

    @NGetter
    boolean isPlatformApplication();


    /**
     * return descriptor packaging (used to resolve file extension)
     *
     * @return return descriptor packaging (used to resolve file extension)
     */
    @NGetter
    String packaging();

    /**
     *
     * This is typically the case for pom projects
     * @return true when the descriptor does not define a content.
     */
    @NGetter
    boolean isNoContent();

    /**
     * dependency resolution solver. defaults to 'maven'
     *
     * @return dependency resolution solver
     */
    @NGetter
    String solver();

    /**
     * Descriptor Condition
     *
     * @return Descriptor Condition
     */
    @NGetter
    NEnvConditionBuilder condition();

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
    @NGetter
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
     * create new builder filled with this descriptor fields.
     *
     * @return new builder filled with this descriptor fields.
     */
    NDescriptorBuilder builder();


    /**
     * @since 0.8.4
     * @return contributors
     */
    @NGetter
    List<NDescriptorContributor> contributors();

    /**
     * @since 0.8.4
     * @return developers
     */
    @NGetter
    List<NDescriptorContributor> developers();

    /**
     * @since 0.8.4
     * @return licenses
     */
    @NGetter
    List<NDescriptorLicense> licenses();

    /**
     * @since 0.8.4
     * @return mailing lists
     */
    @NGetter
    List<NDescriptorMailingList> mailingLists();

    /**
     * @since 0.8.4
     * @return organization
     */
    @NGetter
    NDescriptorOrganization organization();

    /**
     * set id
     *
     * @param id new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder id(NId id);

    /**
     * set id
     *
     * @param id new value
     * @return {@code this instance}
     */
    NDescriptorBuilder id(String id);

    /**
     * descriptor parent list (may be empty)
     *
     * @return descriptor parent list (may be empty)
     */
    @NGetter
    List<NId> parents();

    /**
     * set parents
     *
     * @param parents value to set
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder parents(List<NId> parents);

    /**
     * set packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder packaging(String packaging);

    /**
     * set name
     *
     * @param name value to set
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder name(String name);

    /**
     * update dependency resolution solver
     *
     * @param solver dependency resolution solver
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder solver(String solver);

    @NGetter
    String genericName();

    NDescriptorBuilder genericName(String name);

    @NSetter
    NDescriptorBuilder icons(List<String> icons);

    NDescriptorBuilder icons(String... icons);


    @NSetter
    NDescriptorBuilder categories(List<String> categories);

    @NSetter
    NDescriptorBuilder categories(String... categories);

    @NSetter
    NDescriptorBuilder condition(NEnvCondition condition);
    @NSetter
    NDescriptorBuilder condition(NEnvConditionBuilder condition);

    /**
     * set description
     *
     * @param description new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder description(String description);

    /**
     * set locations
     *
     * @param locations new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder locations(List<NIdLocation> locations);

    NDescriptorBuilder locations(NIdLocation... locations);


    /**
     * set standard dependencies
     *
     * @param dependencies value to set
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder standardDependencies(List<NDependency> dependencies);

    /**
     * set dependencies
     *
     * @param dependencies new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder dependencies(List<NDependency> dependencies);


    /**
     * set executor flag
     *
     * @param executor new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder executor(NArtifactCall executor);


    /**
     * set installer
     *
     * @param installer new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder installer(NArtifactCall installer);

    /**
     * set properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder setProperties(List<NDescriptorProperty> properties);

    /**
     * add location
     *
     * @param location location to add
     * @return {@code this} instance
     */
    NDescriptorBuilder addLocation(NIdLocation location);

    /**
     * set or unset property.
     * if the value is null, the property is removed.
     *
     * @param name  property name
     * @param value new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setProperty(String name, String value);

    /**
     * set all fields from {@code other}
     *
     * @param other builder to copy from
     * @return {@code this} instance
     */
    NDescriptorBuilder copyFrom(NDescriptorBuilder other);

    /**
     * set all fields from {@code other}
     *
     * @param other descriptor to copy from
     * @return {@code this} instance
     */
    NDescriptorBuilder copyFrom(NDescriptor other);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NDescriptorBuilder clear();

    /**
     * remove dependency
     *
     * @param dependency value to remove
     * @return {@code this} instance
     */
    NDescriptorBuilder removeDependency(NDependency dependency);

    /**
     * add dependency
     *
     * @param dependency new value to add
     * @return {@code this} instance
     */
    NDescriptorBuilder addDependency(NDependency dependency);

    /**
     * add dependencies
     *
     * @param dependencies new value to add
     * @return {@code this} instance
     */
    NDescriptorBuilder addDependencies(List<NDependency> dependencies);

    /**
     * remove standard dependency
     *
     * @param dependency value to remove
     * @return {@code this} instance
     */
    NDescriptorBuilder removeStandardDependency(NDependency dependency);

    /**
     * add standard dependency
     *
     * @param dependency value to add
     * @return {@code this} instance
     */
    NDescriptorBuilder addStandardDependency(NDependency dependency);

    /**
     * add standard dependencies
     *
     * @param dependencies value to add
     * @return {@code this} instance
     */
    NDescriptorBuilder addStandardDependencies(List<NDependency> dependencies);

    /**
     * add property
     *
     * @param property property
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDescriptorBuilder addProperty(NDescriptorProperty property);

    /**
     * remove property
     *
     * @param property property
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDescriptorBuilder removeProperties(NDescriptorProperty property);

    /**
     * merge properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    NDescriptorBuilder addProperties(List<NDescriptorProperty> properties);

    /**
     * create a new instance of descriptor with added/merged properties
     *
     * @param filter    properties entry that match the update
     * @param converter function to provide new value to replace with
     * @return {@code this} instance
     */
    NDescriptorBuilder replaceProperty(Predicate<NDescriptorProperty> filter, Function<NDescriptorProperty, NDescriptorProperty> converter);

    /**
     * create a new instance of descriptor with added/merged dependencies
     *
     * @param filter    properties entry that match the update
     * @param converter function to provide new value to replace with
     * @return {@code this} instance
     */
    NDescriptorBuilder replaceDependency(Predicate<NDependency> filter, UnaryOperator<NDependency> converter);

    /**
     * create a new instance of descriptor with removed dependencies that match the predicate
     *
     * @param dependency predicate to test against
     * @return {@code this} instance
     */
    NDescriptorBuilder removeDependency(Predicate<NDependency> dependency);

    /**
     * create new Descriptor filled with this builder fields.
     *
     * @return {@code this} instance
     */
    NDescriptor build();

    /**
     * create a copy
     *
     * @return a copy
     * @since 0.8.2
     */
    NDescriptorBuilder copy();

    /**
     * return descriptor flags
     *
     * @return return descriptor flags
     */
    @NGetter
    Set<NDescriptorFlag> flags();

    /**
     * set flags
     *
     * @param flags flags
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder flags(Set<NDescriptorFlag> flags);

    NDescriptorBuilder flags(NDescriptorFlag... flags);
    /**
     * add flag
     *
     * @param flag flag
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDescriptorBuilder addFlag(NDescriptorFlag flag);

    /**
     * add flags
     *
     * @param flags flags
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDescriptorBuilder addFlags(NDescriptorFlag... flags);

    /**
     * remove flag
     *
     * @param flag flags
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDescriptorBuilder removeFlag(NDescriptorFlag flag);

    /**
     * remove flags
     *
     * @param flags flags
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDescriptorBuilder removeFlags(NDescriptorFlag... flags);


    /**
     * return first property
     *
     * @param name property name
     * @return first property
     * @since 0.8.3
     */
    NOptional<NDescriptorProperty> getProperty(String name);

    /**
     * return first property value
     *
     * @param name property name
     * @return first property value
     * @since 0.8.3
     */
    NOptional<NLiteral> getPropertyValue(String name);

    /**
     * return id type
     *
     * @return id type
     */
    @NGetter
    NIdType idType();

    /**
     * set idType
     *
     * @param idType idType
     * @return {@code this} instance
     */
    @NSetter
    NDescriptorBuilder idType(NIdType idType);

    @NSetter
    NDescriptorBuilder contributors(List<NDescriptorContributor> contributors);

    @NSetter
    NDescriptorBuilder developers(List<NDescriptorContributor> developers);


    @NSetter
    NDescriptorBuilder licenses(List<NDescriptorLicense> licenses);


    @NSetter
    NDescriptorBuilder mailingLists(List<NDescriptorMailingList> mailingLists);

    @NSetter
    NDescriptorBuilder organization(NDescriptorOrganization organization);
}
