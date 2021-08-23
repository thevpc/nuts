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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Nuts descriptors define a <strong>mutable</strong> image to all information needed to execute an artifact.
 * It help creating an instance of {@link NutsDescriptor} by calling {@link #build()}
 *
 * @since 0.5.4
 * @app.category Descriptor
 */
public interface NutsDescriptorBuilder extends Serializable {

    static NutsDescriptorBuilder of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().descriptor().descriptorBuilder();
    }

    /**
     * artifact full id (groupId+artifactId+version)
     *
     * @return artifact id
     */
    NutsId getId();

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
    NutsId[] getParents();

    /**
     * set parents
     *
     * @param parents value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setParents(NutsId[] parents);

//    String getAlternative();

    /**
     * true if the artifact is executable and is considered an application. if not it is a library.
     *
     * @return true if the artifact is executable
     */
    boolean isExecutable();

    /**
     * set executable flag
     *
     * @param executable new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setExecutable(boolean executable);

    /**
     * true if the artifact is a java executable that implements {@link NutsApplication} interface.
     *
     * @return true if the artifact is a java executable that implements {@link NutsApplication} interface.
     */
    boolean isApplication();

    /**
     * return descriptor packaging (used to resolve file extension)
     *
     * @return return descriptor packaging (used to resolve file extension)
     */
    String getPackaging();

    /**
     * set packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setPackaging(String packaging);

    /**
     * supported archs. if empty, all arch are supported (for example for java, all arch are supported).
     *
     * @return supported archs
     */
    String[] getArch();

    /**
     * set archs
     *
     * @param archs value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setArch(String[] archs);

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     *
     * @return supported oses
     */
    String[] getOs();

    /**
     * set os
     *
     * @param os value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setOs(String[] os);

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     *
     * @return supported operating system distributions
     */
    String[] getOsdist();

    /**
     * set osdist
     *
     * @param osdist value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setOsdist(String[] osdist);

    /**
     * supported platforms (java, dotnet, ...). if empty patform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     *
     * @return supported platforms
     */
    String[] getPlatform();

    /**
     * set platform
     *
     * @param platform value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setPlatform(String[] platform);

    /**
     * supported desktop environment (kde, gnome, none, ...). if empty desktop environment is not relevant.
     *
     * @return supported environment list
     */
    String[] getDesktopEnvironment();

    /**
     * set desktopEnvironment
     *
     * @param desktopEnvironment value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setDesktopEnvironment(String[] desktopEnvironment);

    /**
     * user friendly name, a short description for the artifact
     *
     * @return user friendly name
     */
    String getName();

    /**
     * set name
     *
     * @param name value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setName(String name);

    String getGenericName();

    NutsDescriptorBuilder setGenericName(String name);

    List<String> getIcons();

    NutsDescriptorBuilder setIcons(List<String> icons);

    List<String> getCategories();

    NutsDescriptorBuilder setCategories(List<String> categories);

    /**
     * long description for the artifact
     *
     * @return long description for the artifact
     */
    String getDescription();

    /**
     * set description
     *
     * @param description new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setDescription(String description);

    /**
     * ordered list of classifier mapping used to resolve valid classifier to use of ra given environment.
     *
     * @return ordered list of classifier mapping used to resolve valid classifier to use of ra given environment
     */
    NutsClassifierMapping[] getClassifierMappings();

    /**
     * set classifier mappings
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setClassifierMappings(NutsClassifierMapping[] value);

    /**
     * list of available mirror locations from which nuts can download artifact content.
     * location can be mapped to a classifier.
     *
     * @return list of available mirror locations
     */
    NutsIdLocation[] getLocations();

    /**
     * set locations
     *
     * @param locations new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setLocations(NutsIdLocation[] locations);

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
     * set standard dependencies
     *
     * @param dependencies value to set
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setStandardDependencies(NutsDependency[] dependencies);

    /**
     * list of immediate (non inherited and non transitive dependencies
     *
     * @return list of immediate (non inherited and non transitive dependencies
     */
    NutsDependency[] getDependencies();

    /**
     * set dependencies
     *
     * @param dependencies new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setDependencies(NutsDependency[] dependencies);

    /**
     * descriptor of artifact responsible of running this artifact
     *
     * @return descriptor of artifact responsible of running this artifact
     */
    NutsArtifactCall getExecutor();

    /**
     * set executor flag
     *
     * @param executor new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setExecutor(NutsArtifactCall executor);

    /**
     * descriptor of artifact responsible of installing this artifact
     *
     * @return descriptor of artifact responsible of installing this artifact
     */
    NutsArtifactCall getInstaller();

    /**
     * set installer
     *
     * @param installer new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setInstaller(NutsArtifactCall installer);

    /**
     * custom properties that can be used as place holders (int ${name} form) in other fields.
     *
     * @return custom properties that can be used as place holders (int ${name} form) in other fields.
     */
    Map<String, String> getProperties();

    /**
     * set properties
     *
     * @param properties new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setProperties(Map<String, String> properties);

    /**
     * add location
     *
     * @param location location to add
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addLocation(NutsIdLocation location);


    /**
     * add classifier mapping
     *
     * @param mapping classifier mapping
     * @return {@code this} instance
     */
    NutsDescriptorBuilder addClassifierMapping(NutsClassifierMapping mapping);


    /**
     * set nutsApp flag
     *
     * @param nutsApp new value
     * @return {@code this} instance
     */
    NutsDescriptorBuilder setApplication(boolean nutsApp);

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
     * set all fields from {@code other}
     *
     * @param other builder to copy from
     * @return {@code this} instance
     */
    NutsDescriptorBuilder set(NutsDescriptorBuilder other);

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

    NutsDescriptorBuilder removeDesktopEnvironment(String desktopEnvironment);

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
