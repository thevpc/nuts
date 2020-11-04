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
package net.thevpc.nuts;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Nuts descriptors define a <strong>mutable</strong> image to all information needed to execute an artifact.
 * It help creating an instance of {@link NutsDescriptor} by calling {@link #build()}
 *
 * @since 0.5.4
 * @category Descriptor
 */
public interface NutsDescriptorBuilder extends Serializable {

    /**
     * artifact full id (groupId+artifactId+version)
     *
     * @return artifact id
     */
    NutsId getId();

    /**
     * descriptor parent list (may be empty)
     *
     * @return descriptor parent list (may be empty)
     */
    NutsId[] getParents();

    /**
     * true if the artifact is executable and is considered an application. if not it is a library.
     *
     * @return true if the artifact is executable
     */
    boolean isExecutable();

    /**
     * true if the artifact is a java executable that implements {@link NutsApplication} interface.
     *
     * @return true if the artifact is a java executable that implements {@link NutsApplication} interface.
     */
    boolean isNutsApplication();

    /**
     * return descriptor packaging (used to resolve file extension)
     *
     * @return return descriptor packaging (used to resolve file extension)
     */
    String getPackaging();

//    String getAlternative();

    /**
     * supported archs. if empty, all arch are supported (for example for java, all arch are supported).
     *
     * @return supported archs
     */
    String[] getArch();

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     *
     * @return supported oses
     */
    String[] getOs();

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     *
     * @return supported operating system distributions
     */
    String[] getOsdist();

    /**
     * supported platforms (java, dotnet, ...). if empty patform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     *
     * @return supported platforms
     */
    String[] getPlatform();

    /**
     * user friendly name, a short description for the artifact
     *
     * @return user friendly name
     */
    String getName();

    /**
     * long description for the artifact
     *
     * @return long description for the artifact
     */
    String getDescription();

    /**
     * ordered list of classifier mapping used to resolve valid classifier to use of ra given environment.
     *
     * @return ordered list of classifier mapping used to resolve valid classifier to use of ra given environment
     */
    NutsClassifierMapping[] getClassifierMappings();

    /**
     * list of available mirror locations from which nuts can download artifact content.
     * location can be mapped to a classifier.
     *
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
     *
     * @return list of immediate (non inherited and non transitive dependencies
     */
    NutsDependency[] getDependencies();

    /**
     * descriptor of artifact responsible of running this artifact
     *
     * @return descriptor of artifact responsible of running this artifact
     */
    NutsArtifactCall getExecutor();

    /**
     * descriptor of artifact responsible of installing this artifact
     *
     * @return descriptor of artifact responsible of installing this artifact
     */
    NutsArtifactCall getInstaller();

    /**
     * custom properties that can be used as place holders (int ${name} form) in other fields.
     *
     * @return custom properties that can be used as place holders (int ${name} form) in other fields.
     */
    Map<String, String> getProperties();

    /**
     * add location
     *
     * @param location location to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addLocation(NutsIdLocation location);

    /**
     * set locations
     *
     * @param locations new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setLocations(NutsIdLocation[] locations);

    NutsDescriptorBuilder locations(NutsIdLocation[] locations);

    /**
     * add classifier mapping
     *
     * @param mapping classifier mapping
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addClassifierMapping(NutsClassifierMapping mapping);

    /**
     * set classifier mappings
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setClassifierMappings(NutsClassifierMapping[] value);

    NutsDescriptorBuilder classifierMappings(NutsClassifierMapping[] value);

    /**
     * set installer
     *
     * @param installer new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setInstaller(NutsArtifactCall installer);

    NutsDescriptorBuilder installer(NutsArtifactCall installer);

    /**
     * set description
     *
     * @param description new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setDescription(String description);

    NutsDescriptorBuilder description(String description);

    /**
     * set executable flag
     *
     * @param executable new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setExecutable(boolean executable);

    NutsDescriptorBuilder executable(boolean executable);

    NutsDescriptorBuilder executable();

    /**
     * set nutsApp flag
     *
     * @param nutsApp new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setApplication(boolean nutsApp);

    NutsDescriptorBuilder application(boolean nutsApp);
    NutsDescriptorBuilder application();

    /**
     * set executor flag
     *
     * @param executor new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setExecutor(NutsArtifactCall executor);

    NutsDescriptorBuilder executor(NutsArtifactCall executor);

    /**
     * set or unset property.
     * if the value is null, the property is removed.
     *
     * @param name  property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setProperty(String name, String value);

    NutsDescriptorBuilder property(String name, String value);

    /**
     * add os
     *
     * @param os new value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addOs(String os);

    /**
     * add os dist
     *
     * @param osdist new value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addOsdist(String osdist);

    /**
     * add arch
     *
     * @param arch new value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addArch(String arch);

    /**
     * add platform
     *
     * @param platform new value to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addPlatform(String platform);

    /**
     * remove os
     *
     * @param os value to remove
     * @return {@code this} instance
     */
    NutsDescriptorBuilder removeOs(String os);

    /**
     * remove osdist
     *
     * @param osdist value to remove
     * @return {@code this} instance
     */
    NutsDescriptorBuilder removeOsdist(String osdist);

    /**
     * remove arch
     *
     * @param arch value to remove
     * @return {@code this} instance
     */
    NutsDescriptorBuilder removeArch(String arch);

    /**
     * remove platform
     *
     * @param platform value to remove
     * @return {@code this} instance
     */
    NutsDescriptorBuilder removePlatform(String platform);

    /**
     * set packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setPackaging(String packaging);

    /**
     * set packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder packaging(String packaging);

    /**
     * set id
     *
     * @param id new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setId(NutsId id);

    NutsDescriptorBuilder id(NutsId id);

    /**
     * set all fields from {@code other}
     *
     * @param other builder to copy from
     * @return {@code this} instance
     */
    NutsDescriptorBuilder set(NutsDescriptorBuilder other);

    NutsDescriptorBuilder descriptor(NutsDescriptor other);

    NutsDescriptorBuilder descriptor(NutsDescriptorBuilder other);

    /**
     * set all fields from {@code other}
     *
     * @param other descriptor to copy from
     * @return {@code this} instance
     */
    NutsDescriptorBuilder set(NutsDescriptor other);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NutsDescriptorBuilder clear();

    /**
     * set id
     *
     * @param id new value
     * @return {@code this instance}
     */
    NutsDescriptorBuilder setId(String id);

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
    NutsDescriptorBuilder addDependencies(NutsDependency[] dependencies);

    /**
     * set dependencies
     *
     * @param dependencies new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setDependencies(NutsDependency[] dependencies);

    /**
     * set dependencies
     *
     * @param dependencies new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder dependencies(NutsDependency[] dependencies);

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
    NutsDescriptorBuilder addStandardDependencies(NutsDependency[] dependencies);

    /**
     * set standard dependencies
     *
     * @param dependencies value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setStandardDependencies(NutsDependency[] dependencies);

    /**
     * set standard dependencies
     *
     * @param dependencies value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder standardDependencies(NutsDependency[] dependencies);

    /**
     * set properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder properties(Map<String, String> properties);

    /**
     * set properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setProperties(Map<String, String> properties);

    /**
     * merge properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addProperties(Map<String, String> properties);

    /**
     * replace placeholders with the corresponding property value in properties list
     *
     * @return {@code this} instance
     */
    NutsDescriptorBuilder applyProperties();

    /**
     * merge parent and child information (apply inheritance)
     *
     * @param parentDescriptors parent descriptors
     * @return {@code this} instance
     */
    NutsDescriptorBuilder applyParents(NutsDescriptor[] parentDescriptors);

    /**
     * replace placeholders with the corresponding property value in the given properties list and return a new instance.
     *
     * @param properties properties
     * @return {@code this} instance
     */
    NutsDescriptorBuilder applyProperties(Map<String, String> properties);

    /**
     * set name
     *
     * @param name value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setName(String name);

    /**
     * set name
     *
     * @param name value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder name(String name);

    /**
     * set parents
     *
     * @param parents value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setParents(NutsId[] parents);

    /**
     * set parents
     *
     * @param parents value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder parents(NutsId[] parents);

    /**
     * set archs
     *
     * @param archs value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setArch(String[] archs);

    /**
     * set archs
     *
     * @param archs value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder arch(String[] archs);

    /**
     * set os
     *
     * @param os value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setOs(String[] os);


    /**
     * set os
     *
     * @param os value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder os(String[] os);

    /**
     * set osdist
     *
     * @param osdist value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setOsdist(String[] osdist);

    /**
     * set osdist
     *
     * @param osdist value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder osdist(String[] osdist);

    /**
     * set platform
     *
     * @param platform value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setPlatform(String[] platform);

    /**
     * set platform
     *
     * @param platform value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder platform(String[] platform);

    /**
     * create a new instance of descriptor with added/merged properties
     *
     * @param filter    properties entry that match the update
     * @param converter function to provide new value to replace with
     * @return {@code this} instance
     */
    NutsDescriptorBuilder replaceProperty(Predicate<Map.Entry<String, String>> filter, Function<Map.Entry<String, String>, String> converter);

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

}
