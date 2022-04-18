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
 * <p>
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

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Nuts descriptors define a <strong>mutable</strong> image to all information needed to execute an artifact.
 * It help creating an instance of {@link NutsDescriptor} by calling {@link #build()}
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NutsDescriptorBuilder extends NutsDescriptor{


    /**
     * set id
     *
     * @param id new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setId(NutsId id);

    /**
     * set id
     *
     * @param id new value
     * @return {@code this instance}
     */
    NutsDescriptorBuilder setId(String id);

    /**
     * descriptor parent list (may be empty)
     *
     * @return descriptor parent list (may be empty)
     */
    List<NutsId> getParents();

    /**
     * set parents
     *
     * @param parents value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setParents(List<NutsId> parents);

    /**
     * set packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setPackaging(String packaging);

    /**
     * set name
     *
     * @param name value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setName(String name);

    /**
     * update dependency resolution solver
     *
     * @param solver dependency resolution solver
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setSolver(String solver);

    String getGenericName();

    NutsDescriptorBuilder setGenericName(String name);

    NutsDescriptorBuilder setIcons(List<String> icons);


    NutsDescriptorBuilder setCategories(List<String> categories);


    NutsDescriptorBuilder setCondition(NutsEnvCondition condition);

    /**
     * set description
     *
     * @param description new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setDescription(String description);

    /**
     * set locations
     *
     * @param locations new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setLocations(List<NutsIdLocation> locations);


    /**
     * set standard dependencies
     *
     * @param dependencies value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setStandardDependencies(List<NutsDependency> dependencies);

    /**
     * set dependencies
     *
     * @param dependencies new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setDependencies(List<NutsDependency> dependencies);


    /**
     * set executor flag
     *
     * @param executor new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setExecutor(NutsArtifactCall executor);


    /**
     * set installer
     *
     * @param installer new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setInstaller(NutsArtifactCall installer);

    /**
     * set properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setProperties(List<NutsDescriptorProperty> properties);

    /**
     * add location
     *
     * @param location location to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addLocation(NutsIdLocation location);

    /**
     * set or unset property.
     * if the value is null, the property is removed.
     *
     * @param name  property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setProperty(String name, String value);

    /**
     * set all fields from {@code other}
     *
     * @param other builder to copy from
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setAll(NutsDescriptorBuilder other);

    /**
     * set all fields from {@code other}
     *
     * @param other descriptor to copy from
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setAll(NutsDescriptor other);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NutsDescriptorBuilder clear();

    /**
     * remove dependency
     *
     * @param dependency value to remove
     * @return {@code this} instance
     */
    NutsDescriptorBuilder removeDependency(NutsDependency dependency);

    /**
     * add dependency
     *
     * @param dependency new value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addDependency(NutsDependency dependency);

    /**
     * add dependencies
     *
     * @param dependencies new value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addDependencies(List<NutsDependency> dependencies);

    /**
     * remove standard dependency
     *
     * @param dependency value to remove
     * @return {@code this} instance
     */
    NutsDescriptorBuilder removeStandardDependency(NutsDependency dependency);

    /**
     * add standard dependency
     *
     * @param dependency value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addStandardDependency(NutsDependency dependency);

    /**
     * add standard dependencies
     *
     * @param dependencies value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addStandardDependencies(List<NutsDependency> dependencies);

    /**
     * add property
     *
     * @param property property
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsDescriptorBuilder addProperty(NutsDescriptorProperty property);

    /**
     * remove property
     *
     * @param property property
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsDescriptorBuilder removeProperties(NutsDescriptorProperty property);

    /**
     * merge properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addProperties(List<NutsDescriptorProperty> properties);
    /**
     * create a new instance of descriptor with added/merged properties
     *
     * @param filter    properties entry that match the update
     * @param converter function to provide new value to replace with
     * @return {@code this} instance
     */
    NutsDescriptorBuilder replaceProperty(Predicate<NutsDescriptorProperty> filter, Function<NutsDescriptorProperty, NutsDescriptorProperty> converter);

    /**
     * create a new instance of descriptor with added/merged dependencies
     *
     * @param filter    properties entry that match the update
     * @param converter function to provide new value to replace with
     * @return {@code this} instance
     */
    NutsDescriptorBuilder replaceDependency(Predicate<NutsDependency> filter, UnaryOperator<NutsDependency> converter);

    /**
     * create a new instance of descriptor with removed dependencies that match the predicate
     *
     * @param dependency predicate to test against
     * @return {@code this} instance
     */
    NutsDescriptorBuilder removeDependency(Predicate<NutsDependency> dependency);

    /**
     * create new Descriptor filled with this builder fields.
     *
     * @return {@code this} instance
     */
    NutsDescriptor build();

    /**
     * create a copy
     *
     * @return a copy
     * @since 0.8.2
     */
    NutsDescriptorBuilder copy();

    /**
     * return descriptor flags
     *
     * @return return descriptor flags
     */
    Set<NutsDescriptorFlag> getFlags();

    /**
     * set flags
     *
     * @param flags flags
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setFlags(Set<NutsDescriptorFlag> flags);

    /**
     * add flag
     *
     * @param flag flag
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsDescriptorBuilder addFlag(NutsDescriptorFlag flag);

    /**
     * add flags
     *
     * @param flags flags
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsDescriptorBuilder addFlags(NutsDescriptorFlag... flags);

    /**
     * remove flag
     *
     * @param flag flags
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsDescriptorBuilder removeFlag(NutsDescriptorFlag flag);

    /**
     * remove flags
     *
     * @param flags flags
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsDescriptorBuilder removeFlags(NutsDescriptorFlag... flags);


    /**
     * return first property
     *
     * @param name property name
     * @return first property
     * @since 0.8.3
     */
    NutsDescriptorProperty getProperty(String name);

    /**
     * return first property value
     *
     * @param name property name
     * @return first property value
     * @since 0.8.3
     */
    String getPropertyValue(String name);

    /**
     * return id type
     * @return id type
     */
    NutsIdType getIdType();

    /**
     * set idType
     * @param idType idType
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setIdType(NutsIdType idType);

    NutsDescriptorBuilder setContributors(List<NutsDescriptorContributor> contributors);


    NutsDescriptorBuilder setLicenses(List<NutsDescriptorLicense> licenses);


    NutsDescriptorBuilder setMailingLists(List<NutsDescriptorMailingList> mailingLists);

    NutsDescriptorBuilder setOrganization(NutsDescriptorOrganization organization);
}
