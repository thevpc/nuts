/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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

import net.thevpc.nuts.boot.PrivateNutsDefaultNutsProperties;
import net.thevpc.nuts.boot.PrivateNutsUtilCollections;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class DefaultNutsDescriptorBuilder implements NutsDescriptorBuilder {

    private static final long serialVersionUID = 1L;

    private NutsId id;
    private List<NutsId> parents = new ArrayList<>();
    private NutsIdType idType = NutsIdType.REGULAR;
    private String packaging;
    private NutsArtifactCall executor;
    private NutsArtifactCall installer;
    /**
     * short description
     */
    private String name;
    private List<String> icons = new ArrayList<>();
    private List<String> categories;
    private String genericName;
    /**
     * some longer (but not too long) description
     */
    private String description;
    private String solver;
    private NutsEnvConditionBuilder condition;
    private List<NutsIdLocation> locations = new ArrayList<>(); //defaults to empty;
    private List<NutsDependency> dependencies = new ArrayList<>(); //defaults to empty;
    private List<NutsDependency> standardDependencies = new ArrayList<>(); //defaults to empty;
    private Set<NutsDescriptorFlag> flags = new LinkedHashSet<>();
    private List<NutsDescriptorProperty> properties = new ArrayList<>(); //defaults to empty;
    private transient PrivateNutsDefaultNutsProperties _propertiesBuilder = new PrivateNutsDefaultNutsProperties(); //defaults to empty;
    private List<NutsDescriptorContributor> contributors = new ArrayList<>(); //defaults to empty;
    private List<NutsDescriptorLicense> licenses = new ArrayList<>(); //defaults to empty;
    private List<NutsDescriptorMailingList> mailingLists = new ArrayList<>(); //defaults to empty;
    private NutsDescriptorOrganization organization;


    public DefaultNutsDescriptorBuilder() {
        condition = new DefaultNutsEnvConditionBuilder();
    }

    public DefaultNutsDescriptorBuilder(NutsDescriptor other) {
        condition = new DefaultNutsEnvConditionBuilder();
        setAll(other);
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDescriptorBuilder setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setId(String id) {
        this.id = NutsId.of(id).get();
        return this;
    }

    @Override
    public List<NutsId> getParents() {
        return parents;
    }

    public NutsDescriptorBuilder setParents(List<NutsId> parents) {
        this.parents = PrivateNutsUtilCollections.uniqueNonBlankList(parents);
        return this;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public NutsDescriptorBuilder setPackaging(String packaging) {
        this.packaging = NutsUtilStrings.trim(packaging);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsDescriptorBuilder setName(String name) {
        this.name = NutsUtilStrings.trim(name);
        return this;
    }

    @Override
    public String getSolver() {
        return solver;
    }

    @Override
    public NutsDescriptorBuilder setSolver(String solver) {
        this.solver = solver;
        return this;
    }

    @Override
    public String getGenericName() {
        return genericName;
    }

    @Override
    public NutsDescriptorBuilder setGenericName(String name) {
        this.genericName = name;
        return this;
    }

    @Override
    public List<String> getIcons() {
        return icons;
    }

    @Override
    public NutsDescriptorBuilder setIcons(List<String> icons) {
        this.icons = icons == null ? new ArrayList<>() : new ArrayList<>(icons);
        return this;
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public NutsDescriptorBuilder setCategories(List<String> categories) {
        this.categories = categories == null ? new ArrayList<>() : new ArrayList<>(categories);
        return this;
    }

    @Override
    public NutsEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NutsDescriptorBuilder setCondition(NutsEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public NutsDescriptorBuilder setDescription(String description) {
        this.description = NutsUtilStrings.trim(description);
        return this;
    }

    @Override
    public List<NutsIdLocation> getLocations() {
        return locations;
    }

    @Override
    public NutsDescriptorBuilder setLocations(List<NutsIdLocation> locations) {
        this.locations = PrivateNutsUtilCollections.uniqueList(locations);
        return this;
    }

    @Override
    public List<NutsDependency> getStandardDependencies() {
        return standardDependencies;
    }

    @Override
    public NutsDescriptorBuilder setStandardDependencies(List<NutsDependency> dependencies) {
        this.standardDependencies = PrivateNutsUtilCollections.uniqueNonBlankList(dependencies);
        return this;
    }

    @Override
    public List<NutsDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public NutsDescriptorBuilder setDependencies(List<NutsDependency> dependencies) {
        this.dependencies = PrivateNutsUtilCollections.uniqueNonBlankList(dependencies);
        return this;
    }

    @Override
    public NutsArtifactCall getExecutor() {
        return executor;
    }

    @Override
    public NutsDescriptorBuilder setExecutor(NutsArtifactCall executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public NutsArtifactCall getInstaller() {
        return installer;
    }

    @Override
    public NutsDescriptorBuilder setInstaller(NutsArtifactCall installer) {
        this.installer = installer;
        return this;
    }

    @Override
    public List<NutsDescriptorProperty> getProperties() {
        return properties;
    }

    @Override
    public NutsDescriptorBuilder setProperties(List<NutsDescriptorProperty> properties) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.clear();
        if (properties == null || properties.size() == 0) {

        } else {
            _propertiesBuilder.addAll(properties);
        }
        _updateProperties();
        return this;
    }

    @Override
    public NutsDescriptorBuilder addLocation(NutsIdLocation location) {
        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }
        this.locations.add(location);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setProperty(String name, String value) {
        NutsDescriptorProperty pp = new DefaultNutsDescriptorPropertyBuilder()
                .setName(name)
                .setValue(value)
                .readOnly();
        _rebuildPropertiesBuilder();
        if (value == null) {
            _propertiesBuilder.remove(pp);
        } else {
            properties.add(pp);
        }
        _updateProperties();
        return this;
    }

    @Override
    public NutsDescriptorBuilder setAll(NutsDescriptorBuilder other) {
        if (other != null) {
            setId(other.getId());
            setIdType(other.getIdType());
            setPackaging(other.getPackaging());
            setParents(other.getParents());
            setDescription(other.getDescription());
            setName(other.getName());
            setExecutor(other.getExecutor());
            setInstaller(other.getInstaller());
//            setExt(other.getExt());
            setCondition(other.getCondition());
            setLocations(other.getLocations());
            setDependencies(other.getDependencies());
            setStandardDependencies(other.getStandardDependencies());
            setProperties(other.getProperties());
            setIcons(new ArrayList<>(other.getIcons()));
            setCategories(other.getCategories());
            setGenericName(other.getGenericName());
            setSolver(other.getSolver());
            setFlags(other.getFlags());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder setAll(NutsDescriptor other) {
        if (other != null) {
            setId(other.getId());
            setIdType(other.getIdType());
            setPackaging(other.getPackaging());
            setParents(other.getParents());
            setDescription(other.getDescription());
            setName(other.getName());
            setExecutor(other.getExecutor());
            setInstaller(other.getInstaller());
            setCondition(other.getCondition());
            setLocations(other.getLocations());
            setDependencies(other.getDependencies());
            setStandardDependencies(other.getStandardDependencies());
            setProperties(other.getProperties());
            setIcons(other.getIcons());
            setGenericName(other.getGenericName());
            setCategories(other.getCategories());
            setSolver(other.getSolver());
            setFlags(other.getFlags());
            setOrganization(other.getOrganization());
            setContributors(other.getContributors());
            setLicenses(other.getLicenses());
            setMailingLists(other.getMailingLists());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder clear() {
        setId((NutsId) null);
        setIdType(null);
        setPackaging(null);
        setParents(null);
        setDescription(null);
        setFlags(new LinkedHashSet<>());
        setName(null);
        setExecutor(null);
        setInstaller(null);
        setCondition((NutsEnvCondition) null);
        setLocations(null);
        setDependencies(null);
        setStandardDependencies(null);
        setProperties(null);
        setIcons(null);
        setCategories(null);
        setGenericName(null);
        setSolver(null);
        setOrganization(null);
        setContributors(null);
        setLicenses(null);
        setMailingLists(null);
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeDependency(NutsDependency dependency) {
        if (this.dependencies != null) {
            this.dependencies.remove(dependency);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder addDependency(NutsDependency dependency) {
        if (dependency == null) {
            throw new NullPointerException();
        }
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addDependencies(List<NutsDependency> dependencies) {
        PrivateNutsUtilCollections.addUniqueNonBlankList(this.dependencies, dependencies);
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeStandardDependency(NutsDependency dependency) {
        if (this.standardDependencies != null) {
            this.standardDependencies.remove(dependency);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder addStandardDependency(NutsDependency dependency) {
        this.standardDependencies.add(dependency);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addStandardDependencies(List<NutsDependency> dependencies) {
        PrivateNutsUtilCollections.addUniqueNonBlankList(this.standardDependencies, dependencies);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addProperty(NutsDescriptorProperty property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.add(property);
        _updateProperties();
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeProperties(NutsDescriptorProperty property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.remove(property);
        _updateProperties();
        return this;
    }

    @Override
    public NutsDescriptorBuilder addProperties(List<NutsDescriptorProperty> properties) {
        if (properties == null || properties.size() == 0) {
            //do nothing
        } else {
            _rebuildPropertiesBuilder();
            _propertiesBuilder.addAll(properties);
            _updateProperties();
        }
        return this;
    }


    @Override
    public NutsDescriptorBuilder replaceProperty(Predicate<NutsDescriptorProperty> filter, Function<NutsDescriptorProperty, NutsDescriptorProperty> converter) {
        if (converter == null) {
            return this;
        }

        PrivateNutsDefaultNutsProperties p = new PrivateNutsDefaultNutsProperties();
        boolean someUpdate = false;
        for (NutsDescriptorProperty entry : getProperties()) {
            if (filter == null || filter.test(entry)) {
                NutsDescriptorProperty v = converter.apply(entry);
                if (v != null) {
                    if (!Objects.equals(v, entry)) {
                        someUpdate = true;
                    }
                    p.add(v);
                } else {
                    someUpdate = true;
                }
            }
        }
        if (someUpdate) {
            setProperties(p.getList());
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder replaceDependency(Predicate<NutsDependency> filter, UnaryOperator<NutsDependency> converter) {
        if (converter == null) {
            return this;
        }
        ArrayList<NutsDependency> dependenciesList = new ArrayList<>();
        for (NutsDependency d : getDependencies()) {
            if (filter == null || filter.test(d)) {
                d = converter.apply(d);
                if (d != null) {
                    dependenciesList.add(d);
                }
            } else {
                dependenciesList.add(d);
            }
        }
        this.dependencies = dependenciesList;
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeDependency(Predicate<NutsDependency> dependency) {
        if (dependency == null) {
            return this;
        }
        for (Iterator<NutsDependency> it = dependencies.iterator(); it.hasNext(); ) {
            NutsDependency d = it.next();
            if (dependency.test(d)) {
                //do not add
                it.remove();
            }
        }
        return this;
    }

    @Override
    public NutsDescriptor build() {
        LinkedHashSet<NutsDescriptorFlag> flags = new LinkedHashSet<>();
        NutsIdType idType = getIdType();
        for (NutsDescriptorProperty property : getProperties()) {
            if (NutsBlankable.isBlank(property.getCondition())) {
                switch (property.getName()) {
                    case "nuts.application": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NutsDescriptorFlag.APP);
                            flags.add(NutsDescriptorFlag.EXEC);
                        }
                        break;
                    }
                    case "nuts.executable": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NutsDescriptorFlag.EXEC);
                        }
                        break;
                    }
                    case "nuts.term": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NutsDescriptorFlag.TERM);
                        }
                        break;
                    }
                    case "nuts.gui": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NutsDescriptorFlag.GUI);
                        }
                        break;
                    }
                    case "nuts.extension": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            idType = NutsIdType.EXTENSION;
                        }
                        break;
                    }
                    case "nuts.runtime": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            idType = NutsIdType.RUNTIME;
                        }
                        break;
                    }
                    case "nuts.companion": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            idType = NutsIdType.COMPANION;
                        }
                        break;
                    }
                    case "nuts.api": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NutsDescriptorFlag.NUTS_API);
                        }
                        break;
                    }
                }
            }
        }
//                session.log().of(DefaultNutsDescriptor.class)
//                        .with().level(Level.FINEST)
//                        .verb(NutsLogVerb.WARNING)
//                        .log(
//                                NutsMessage.jstyle("{0} has nuts.application flag armed but is not an application", getId())
//                        );

        for (NutsDescriptorFlag flag : this.flags) {
            flags.add(flag);
            switch (flag) {
                case APP:
                case TERM:
                case GUI: {
                    flags.add(NutsDescriptorFlag.EXEC);
                    break;
                }
            }
        }
        return new DefaultNutsDescriptor(
                getId(), idType, getParents(), getPackaging(),
                getExecutor(), getInstaller(),
                getName(), getDescription(), getCondition().readOnly(),
                getDependencies(), getStandardDependencies(),
                getLocations(), getProperties(),
                genericName,
                categories,
                icons,
                flags,
                getSolver(),
                getContributors(),
                getLicenses(),
                getMailingLists(),
                getOrganization()
        );
    }

    @Override
    public NutsDescriptorBuilder copy() {
        return new DefaultNutsDescriptorBuilder().setAll(this);
    }

    public Set<NutsDescriptorFlag> getFlags() {
        return flags;
    }

    public NutsDescriptorBuilder setFlags(Set<NutsDescriptorFlag> flags) {
        this.flags = flags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(flags);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addFlag(NutsDescriptorFlag flag) {
        if (flag != null) {
            this.flags.add(flag);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder addFlags(NutsDescriptorFlag... flags) {
        if (flags != null) {
            for (NutsDescriptorFlag flag : flags) {
                if (flag != null) {
                    this.flags.add(flag);
                }
            }
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeFlag(NutsDescriptorFlag flag) {
        if (flag != null) {
            this.flags.remove(flag);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeFlags(NutsDescriptorFlag... flags) {
        if (flags != null) {
            for (NutsDescriptorFlag flag : flags) {
                if (flag != null) {
                    this.flags.remove(flag);
                }
            }
        }
        return this;
    }

    @Override
    public NutsDescriptorProperty getProperty(String name) {
        return Arrays.stream(_propertiesBuilder.getAll()).filter(x -> x.getName().equals(name)).findFirst()
                .orElse(null);
    }

    @Override
    public NutsOptional<NutsValue> getPropertyValue(String name) {
        NutsDescriptorProperty p = getProperty(name);
        return NutsOptional.of(p == null ? null : p.getValue(), session -> NutsMessage.cstyle("property not found : %s", name));
    }

    public NutsIdType getIdType() {
        return idType;
    }

    public NutsDescriptorBuilder setIdType(NutsIdType idType) {
        this.idType = idType == null ? NutsIdType.REGULAR : idType;
        return this;
    }


    private void _rebuildPropertiesBuilder() {
        if (_propertiesBuilder == null) {
            _propertiesBuilder = new PrivateNutsDefaultNutsProperties();
            _propertiesBuilder.addAll(this.properties.toArray(new NutsDescriptorProperty[0]));
        }
    }

    private void _updateProperties() {
        this.properties.clear();
        this.properties.addAll(Arrays.asList(_propertiesBuilder.getAll()));
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, idType, packaging, executor, installer, name, icons, categories,
                genericName, description, condition, locations, dependencies, standardDependencies, properties, flags, solver, parents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsDescriptorBuilder that = (DefaultNutsDescriptorBuilder) o;
        return Objects.equals(id, that.id)
                && Objects.equals(idType, that.idType)
                && Objects.equals(parents, that.parents)
                && Objects.equals(packaging, that.packaging) && Objects.equals(executor, that.executor)
                && Objects.equals(installer, that.installer) && Objects.equals(name, that.name)
                && Objects.equals(icons, that.icons) && Objects.equals(categories, that.categories)
                && Objects.equals(genericName, that.genericName)
                && Objects.equals(description, that.description)
                && Objects.equals(condition, that.condition)
                && Objects.equals(locations, that.locations)
                && Objects.equals(dependencies, that.dependencies)
                && Objects.equals(standardDependencies, that.standardDependencies)
                && Objects.equals(properties, that.properties)
                && Objects.equals(flags, that.flags)
                && Objects.equals(solver, that.solver)
                ;
    }

    @Override
    public NutsDescriptor readOnly() {
        return build();
    }

    @Override
    public NutsDescriptorBuilder builder() {
        return new DefaultNutsDescriptorBuilder(this);
    }

    @Override
    public boolean isBlank() {
        return build().isBlank();
    }

    @Override
    public boolean isExecutable() {
        return getFlags().contains(NutsDescriptorFlag.EXEC);
    }

    @Override
    public boolean isApplication() {
        return getFlags().contains(NutsDescriptorFlag.APP);
    }

    @Override
    public NutsDescriptorFormat formatter(NutsSession session) {
        return build().formatter(session);
    }

    @Override
    public List<NutsDescriptorContributor> getContributors() {
        return contributors;
    }

    @Override
    public NutsDescriptorBuilder setContributors(List<NutsDescriptorContributor> contributors) {
        this.contributors = PrivateNutsUtilCollections.uniqueList(contributors);
        return this;
    }

    @Override
    public List<NutsDescriptorLicense> getLicenses() {
        return licenses;
    }

    @Override
    public NutsDescriptorBuilder setLicenses(List<NutsDescriptorLicense> licenses) {
        this.licenses = PrivateNutsUtilCollections.uniqueList(licenses);
        return this;
    }

    @Override
    public List<NutsDescriptorMailingList> getMailingLists() {
        return mailingLists;
    }

    @Override
    public NutsDescriptorBuilder setMailingLists(List<NutsDescriptorMailingList> mailingLists) {
        this.mailingLists = PrivateNutsUtilCollections.uniqueList(mailingLists);
        return this;
    }

    @Override
    public NutsDescriptorOrganization getOrganization() {
        return organization;
    }

    @Override
    public NutsDescriptorBuilder setOrganization(NutsDescriptorOrganization organization) {
        this.organization = organization;
        return this;
    }


}
