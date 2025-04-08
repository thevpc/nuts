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
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

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
    NId getId();


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
    boolean isApplication();


    /**
     * return descriptor packaging (used to resolve file extension)
     *
     * @return return descriptor packaging (used to resolve file extension)
     */
    String getPackaging();

    /**
     *
     * This is typically the case for pom projects
     * @return true when the descriptor does not define a content.
     */
    boolean isNoContent();

    /**
     * dependency resolution solver. defaults to 'maven'
     *
     * @return dependency resolution solver
     */
    String getSolver();

    /**
     * Descriptor Condition
     *
     * @return Descriptor Condition
     */
    NEnvConditionBuilder getCondition();

    /**
     * user-friendly name, a short description for the artifact
     *
     * @return user friendly name
     */
    String getName();

    /**
     * url (external or classpath url) to the application Icon
     *
     * @return url (external or classpath url) to the application Icon
     */
    List<String> getIcons();

    /**
     * category path of the artifact (slash separated).
     * Standard Category Names should be used.
     *
     * @return category path of the artifact
     */
    List<String> getCategories();

    /**
     * long description for the artifact
     *
     * @return long description for the artifact
     */
    String getDescription();

    /**
     * list of available mirror locations from which nuts can download artifact content.
     * location can be mapped to a classifier.
     *
     * @return list of available mirror locations
     */
    List<NIdLocation> getLocations();


    /**
     * The dependencies specified here are not used until they are referenced in
     * a POM within the group. This allows the specification of a
     * &quot;standard&quot; version for a particular. This corresponds to
     * "dependencyManagement.dependencies" in maven
     *
     * @return "standard" dependencies
     */
    List<NDependency> getStandardDependencies();

    /**
     * list of immediate (non-inherited and non-transitive dependencies
     *
     * @return list of immediate (non-inherited and non-transitive dependencies
     */
    List<NDependency> getDependencies();

    /**
     * descriptor of artifact responsible for running this artifact
     *
     * @return descriptor of artifact responsible for running this artifact
     */
    NArtifactCall getExecutor();

    /**
     * descriptor of artifact responsible for installing this artifact
     *
     * @return descriptor of artifact responsible for installing this artifact
     */
    NArtifactCall getInstaller();

    /**
     * custom properties that can be used as placeholders (int ${name} form) in other fields.
     *
     * @return custom properties that can be used as placeholders (int ${name} form) in other fields.
     */
    List<NDescriptorProperty> getProperties();

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
    List<NDescriptorContributor> getContributors();

    /**
     * @since 0.8.4
     * @return developers
     */
    List<NDescriptorContributor> getDevelopers();

    /**
     * @since 0.8.4
     * @return licenses
     */
    List<NDescriptorLicense> getLicenses();

    /**
     * @since 0.8.4
     * @return mailing lists
     */
    List<NDescriptorMailingList> getMailingLists();

    /**
     * @since 0.8.4
     * @return organization
     */
    NDescriptorOrganization getOrganization();

    /**
     * set id
     *
     * @param id new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setId(NId id);

    /**
     * set id
     *
     * @param id new value
     * @return {@code this instance}
     */
    NDescriptorBuilder setId(String id);

    /**
     * descriptor parent list (may be empty)
     *
     * @return descriptor parent list (may be empty)
     */
    List<NId> getParents();

    /**
     * set parents
     *
     * @param parents value to set
     * @return {@code this} instance
     */
    NDescriptorBuilder setParents(List<NId> parents);

    /**
     * set packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setPackaging(String packaging);

    /**
     * set name
     *
     * @param name value to set
     * @return {@code this} instance
     */
    NDescriptorBuilder setName(String name);

    /**
     * update dependency resolution solver
     *
     * @param solver dependency resolution solver
     * @return {@code this} instance
     */
    NDescriptorBuilder setSolver(String solver);

    String getGenericName();

    NDescriptorBuilder setGenericName(String name);

    NDescriptorBuilder setIcons(List<String> icons);

    NDescriptorBuilder setIcons(String... icons);


    NDescriptorBuilder setCategories(List<String> categories);

    NDescriptorBuilder setCategories(String... categories);

    NDescriptorBuilder setCondition(NEnvCondition condition);
    NDescriptorBuilder setCondition(NEnvConditionBuilder condition);

    /**
     * set description
     *
     * @param description new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setDescription(String description);

    /**
     * set locations
     *
     * @param locations new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setLocations(List<NIdLocation> locations);

    NDescriptorBuilder setLocations(NIdLocation... locations);


    /**
     * set standard dependencies
     *
     * @param dependencies value to set
     * @return {@code this} instance
     */
    NDescriptorBuilder setStandardDependencies(List<NDependency> dependencies);

    /**
     * set dependencies
     *
     * @param dependencies new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setDependencies(List<NDependency> dependencies);


    /**
     * set executor flag
     *
     * @param executor new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setExecutor(NArtifactCall executor);


    /**
     * set installer
     *
     * @param installer new value
     * @return {@code this} instance
     */
    NDescriptorBuilder setInstaller(NArtifactCall installer);

    /**
     * set properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
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
    Set<NDescriptorFlag> getFlags();

    /**
     * set flags
     *
     * @param flags flags
     * @return {@code this} instance
     */
    NDescriptorBuilder setFlags(Set<NDescriptorFlag> flags);

    NDescriptorBuilder setFlags(NDescriptorFlag... flags);
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
    NIdType getIdType();

    /**
     * set idType
     *
     * @param idType idType
     * @return {@code this} instance
     */
    NDescriptorBuilder setIdType(NIdType idType);

    NDescriptorBuilder setContributors(List<NDescriptorContributor> contributors);

    NDescriptorBuilder setDevelopers(List<NDescriptorContributor> developers);


    NDescriptorBuilder setLicenses(List<NDescriptorLicense> licenses);


    NDescriptorBuilder setMailingLists(List<NDescriptorMailingList> mailingLists);

    NDescriptorBuilder setOrganization(NDescriptorOrganization organization);
}
