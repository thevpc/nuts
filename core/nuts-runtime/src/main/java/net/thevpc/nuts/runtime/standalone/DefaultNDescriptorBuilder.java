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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootDescriptor;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.NEnvConditionBuilder;
import net.thevpc.nuts.NIdLocation;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DefaultNDescriptorBuilder implements NDescriptorBuilder {

    private static final long serialVersionUID = 1L;

    private NId id;
    private List<NId> parents = new ArrayList<>();
    private NIdType idType = NIdType.REGULAR;
    private String packaging;
    private NArtifactCall executor;
    private NArtifactCall installer;
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
    private NEnvConditionBuilder condition;
    private List<NIdLocation> locations = new ArrayList<>(); //defaults to empty;
    private List<NDependency> dependencies = new ArrayList<>(); //defaults to empty;
    private List<NDependency> standardDependencies = new ArrayList<>(); //defaults to empty;
    private Set<NDescriptorFlag> flags = new LinkedHashSet<>();
    private List<NDescriptorProperty> properties = new ArrayList<>(); //defaults to empty;
    private transient DefaultNProperties _propertiesBuilder = new DefaultNProperties(); //defaults to empty;
    private List<NDescriptorContributor> contributors = new ArrayList<>(); //defaults to empty;
    private List<NDescriptorContributor> developers = new ArrayList<>(); //defaults to empty;
    private List<NDescriptorLicense> licenses = new ArrayList<>(); //defaults to empty;
    private List<NDescriptorMailingList> mailingLists = new ArrayList<>(); //defaults to empty;
    private NDescriptorOrganization organization;


    public DefaultNDescriptorBuilder() {
        condition = new DefaultNEnvConditionBuilder();
    }

    public DefaultNDescriptorBuilder(NDescriptor other) {
        condition = new DefaultNEnvConditionBuilder();
        copyFrom(other);
    }

    public DefaultNDescriptorBuilder(NDescriptorBuilder other) {
        condition = new DefaultNEnvConditionBuilder();
        copyFrom(other);
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public NDescriptorBuilder setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NDescriptorBuilder setId(String id) {
        this.id = NId.get(id).get();
        return this;
    }

    @Override
    public List<NId> getParents() {
        return parents;
    }

    public NDescriptorBuilder setParents(List<NId> parents) {
        this.parents = NReservedLangUtils.uniqueNonBlankList(parents);
        return this;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public NDescriptorBuilder setPackaging(String packaging) {
        this.packaging = NStringUtils.trim(packaging);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NDescriptorBuilder setName(String name) {
        this.name = NStringUtils.trim(name);
        return this;
    }

    @Override
    public String getSolver() {
        return solver;
    }

    @Override
    public NDescriptorBuilder setSolver(String solver) {
        this.solver = solver;
        return this;
    }

    @Override
    public String getGenericName() {
        return genericName;
    }

    @Override
    public NDescriptorBuilder setGenericName(String name) {
        this.genericName = name;
        return this;
    }

    @Override
    public List<String> getIcons() {
        return icons;
    }

    @Override
    public NDescriptorBuilder setIcons(List<String> icons) {
        this.setIcons(icons == null ? null : icons.toArray(new String[0]));
        return this;
    }

    @Override
    public NDescriptorBuilder setIcons(String... icons) {
        this.icons = new ArrayList<>();
        if (icons != null) {
            for (String icon : icons) {
                if (!NBlankable.isBlank(icon)) {
                    this.icons.add(icon);
                }
            }
        }
        return this;
    }

    @Override
    public NDescriptorBuilder setLocations(NIdLocation... locations) {
        this.locations = new ArrayList<>();
        if (locations != null) {
            for (NIdLocation location : locations) {
                if (!NBlankable.isBlank(location)) {
                    this.locations.add(location);
                }
            }
        }
        this.locations = NReservedLangUtils.uniqueList(this.locations);
        return this;
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public NDescriptorBuilder setCategories(List<String> categories) {
        this.setCategories(categories == null ? null : categories.toArray(new String[0]));
        return this;
    }

    @Override
    public NDescriptorBuilder setCategories(String... categories) {
        this.categories = new ArrayList<>();
        if (categories != null) {
            for (String cat : categories) {
                if (!NBlankable.isBlank(cat)) {
                    this.categories.add(cat);
                }
            }
        }
        return this;
    }

    @Override
    public NEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NDescriptorBuilder setCondition(NEnvCondition condition) {
        this.condition.copyFrom(condition);
        return this;
    }

    @Override
    public NDescriptorBuilder setCondition(NEnvConditionBuilder condition) {
        this.condition.copyFrom(condition);
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public NDescriptorBuilder setDescription(String description) {
        this.description = NStringUtils.trim(description);
        return this;
    }

    @Override
    public List<NIdLocation> getLocations() {
        return locations;
    }

    @Override
    public NDescriptorBuilder setLocations(List<NIdLocation> locations) {
        setLocations(locations == null ? null : locations.toArray(new NIdLocation[0]));
        return this;
    }

    @Override
    public List<NDependency> getStandardDependencies() {
        return standardDependencies;
    }

    @Override
    public NDescriptorBuilder setStandardDependencies(List<NDependency> dependencies) {
        this.standardDependencies = NReservedLangUtils.uniqueNonBlankList(dependencies);
        return this;
    }

    @Override
    public List<NDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public NDescriptorBuilder setDependencies(List<NDependency> dependencies) {
        this.dependencies = NReservedLangUtils.uniqueNonBlankList(dependencies);
        return this;
    }

    @Override
    public NArtifactCall getExecutor() {
        return executor;
    }

    @Override
    public NDescriptorBuilder setExecutor(NArtifactCall executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public NArtifactCall getInstaller() {
        return installer;
    }

    @Override
    public NDescriptorBuilder setInstaller(NArtifactCall installer) {
        this.installer = installer;
        return this;
    }

    @Override
    public List<NDescriptorProperty> getProperties() {
        return properties;
    }

    @Override
    public NDescriptorBuilder setProperties(List<NDescriptorProperty> properties) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.clear();
        if (properties == null || properties.isEmpty()) {

        } else {
            _propertiesBuilder.addAll(properties);
        }
        _updateProperties();
        return this;
    }

    @Override
    public NDescriptorBuilder addLocation(NIdLocation location) {
        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }
        this.locations.add(location);
        return this;
    }

    @Override
    public NDescriptorBuilder setProperty(String name, String value) {
        NDescriptorProperty pp = new DefaultNDescriptorPropertyBuilder()
                .setName(name)
                .setValue(value)
                .build();
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
    public NDescriptorBuilder copyFrom(NDescriptorBuilder other) {
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

    //    @Override
    public NDescriptorBuilder copyFrom(NBootDescriptor other) {
        if (other != null) {
            setId(other.getId() == null ? null : NId.get(other.getId().toString()).get());
            setPackaging(other.getPackaging());
            setParents(other.getParents() == null ? null : other.getParents().stream().map(x -> NId.get(x.toString()).get()).collect(Collectors.toList()));
            setDescription(other.getDescription());
            setName(other.getName());
            setCondition(other.getCondition() == null ? null : new DefaultNEnvConditionBuilder().copyFrom(other.getCondition()).build());
            setDependencies(other.getDependencies() == null ? null : other.getDependencies().stream().map(x -> new DefaultNDependencyBuilder().copyFrom(x).build()).collect(Collectors.toList()));
            setStandardDependencies(other.getStandardDependencies() == null ? null : other.getDependencies().stream().map(x -> new DefaultNDependencyBuilder().copyFrom(x).build()).collect(Collectors.toList()));
            setProperties(other.getProperties() == null ? null : other.getProperties().stream().map(x -> new DefaultNDescriptorPropertyBuilder().copyFrom(x).build()).collect(Collectors.toList()));

            setIdType(null);
            setExecutor(null);
            setInstaller(null);
            setLocations();
            setIcons();
            setCategories();
            setGenericName(null);
            setSolver(null);
            setFlags();
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NDescriptorBuilder copyFrom(NDescriptor other) {
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
            setDevelopers(other.getDevelopers());
            setLicenses(other.getLicenses());
            setMailingLists(other.getMailingLists());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NDescriptorBuilder clear() {
        setId((NId) null);
        setIdType(null);
        setPackaging(null);
        setParents(null);
        setDescription(null);
        setFlags(new LinkedHashSet<>());
        setName(null);
        setExecutor(null);
        setInstaller(null);
        setCondition((NEnvCondition) null);
        setLocations();
        setDependencies(null);
        setStandardDependencies(null);
        setProperties(null);
        setIcons();
        setCategories();
        setGenericName(null);
        setSolver(null);
        setOrganization(null);
        setContributors(null);
        setLicenses(null);
        setDevelopers(null);
        setMailingLists(null);
        return this;
    }

    @Override
    public NDescriptorBuilder removeDependency(NDependency dependency) {
        if (this.dependencies != null) {
            this.dependencies.remove(dependency);
        }
        return this;
    }

    @Override
    public NDescriptorBuilder addDependency(NDependency dependency) {
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
    public NDescriptorBuilder addDependencies(List<NDependency> dependencies) {
        NReservedLangUtils.addUniqueNonBlankList(this.dependencies, dependencies);
        return this;
    }

    @Override
    public NDescriptorBuilder removeStandardDependency(NDependency dependency) {
        if (this.standardDependencies != null) {
            this.standardDependencies.remove(dependency);
        }
        return this;
    }

    @Override
    public NDescriptorBuilder addStandardDependency(NDependency dependency) {
        this.standardDependencies.add(dependency);
        return this;
    }

    @Override
    public NDescriptorBuilder addStandardDependencies(List<NDependency> dependencies) {
        NReservedLangUtils.addUniqueNonBlankList(this.standardDependencies, dependencies);
        return this;
    }

    @Override
    public NDescriptorBuilder addProperty(NDescriptorProperty property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.add(property);
        _updateProperties();
        return this;
    }

    @Override
    public NDescriptorBuilder removeProperties(NDescriptorProperty property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.remove(property);
        _updateProperties();
        return this;
    }

    @Override
    public NDescriptorBuilder addProperties(List<NDescriptorProperty> properties) {
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
    public NDescriptorBuilder replaceProperty(Predicate<NDescriptorProperty> filter, Function<NDescriptorProperty, NDescriptorProperty> converter) {
        if (converter == null) {
            return this;
        }

        DefaultNProperties p = new DefaultNProperties();
        boolean someUpdate = false;
        for (NDescriptorProperty entry : getProperties()) {
            if (filter == null || filter.test(entry)) {
                NDescriptorProperty v = converter.apply(entry);
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
            setProperties(p.toList());
        }
        return this;
    }

    @Override
    public NDescriptorBuilder replaceDependency(Predicate<NDependency> filter, UnaryOperator<NDependency> converter) {
        if (converter == null) {
            return this;
        }
        ArrayList<NDependency> dependenciesList = new ArrayList<>();
        for (NDependency d : getDependencies()) {
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
    public NDescriptorBuilder removeDependency(Predicate<NDependency> dependency) {
        if (dependency == null) {
            return this;
        }
        for (Iterator<NDependency> it = dependencies.iterator(); it.hasNext(); ) {
            NDependency d = it.next();
            if (dependency.test(d)) {
                //do not add
                it.remove();
            }
        }
        return this;
    }

    @Override
    public NDescriptor build() {
        LinkedHashSet<NDescriptorFlag> flags = new LinkedHashSet<>();
        NIdType idType = getIdType();
        for (NDescriptorProperty property : getProperties()) {
            if (NBlankable.isBlank(property.getCondition())) {
                switch (property.getName()) {
                    case "nuts.application": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.NUTS_APP);
                            flags.add(NDescriptorFlag.EXEC);
                        }
                        break;
                    }
                    case "nuts.executable": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.EXEC);
                        }
                        break;
                    }
                    case "nuts.term": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.TERM);
                        }
                        break;
                    }
                    case "nuts.gui": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.GUI);
                        }
                        break;
                    }
                    case "nuts.extension": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            idType = NIdType.EXTENSION;
                        }
                        break;
                    }
                    case "nuts.runtime": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            idType = NIdType.RUNTIME;
                        }
                        break;
                    }
                    case "nuts.companion": {
                        if (property.getValue().asBoolean().orElse(false)) {
                            idType = NIdType.COMPANION;
                        }
                        break;
                    }
                }
            }
        }

        if (Objects.equals(getId().getShortName(), NConstants.Ids.NUTS_API)) {
            idType = NIdType.API;
        }

        if (Objects.equals(getId().getShortName(), NConstants.Ids.NUTS_RUNTIME)) {
            idType = NIdType.RUNTIME;
        }

        for (NDescriptorFlag flag : this.flags) {
            flags.add(flag);
            switch (flag) {
                case NUTS_APP:
                case TERM:
                case GUI: {
                    flags.add(NDescriptorFlag.EXEC);
                    break;
                }
            }
        }
        return new DefaultNDescriptor(
                getId(), idType, getParents(), getPackaging(),
                getExecutor(), getInstaller(),
                getName(), getDescription(), getCondition().build(),
                getDependencies(), getStandardDependencies(),
                getLocations(), getProperties(),
                genericName,
                categories,
                icons,
                flags,
                getSolver(),
                getContributors(),
                getDevelopers(),
                getLicenses(),
                getMailingLists(),
                getOrganization()
        );
    }

    @Override
    public NDescriptorBuilder copy() {
        return new DefaultNDescriptorBuilder().copyFrom(this);
    }

    public Set<NDescriptorFlag> getFlags() {
        return flags;
    }

    public NDescriptorBuilder setFlags(Set<NDescriptorFlag> flags) {
        this.flags = flags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(flags);
        return this;
    }

    public NDescriptorBuilder setFlags(NDescriptorFlag... flags) {
        Set<NDescriptorFlag> nv = new LinkedHashSet<>();
        if (flags != null) {
            for (NDescriptorFlag v : flags) {
                if (v != null) {
                    nv.add(v);
                }
            }
        }

        this.flags = nv;
        return this;
    }

    @Override
    public NDescriptorBuilder addFlag(NDescriptorFlag flag) {
        if (flag != null) {
            this.flags.add(flag);
        }
        return this;
    }

    @Override
    public NDescriptorBuilder addFlags(NDescriptorFlag... flags) {
        if (flags != null) {
            for (NDescriptorFlag flag : flags) {
                if (flag != null) {
                    this.flags.add(flag);
                }
            }
        }
        return this;
    }

    @Override
    public NDescriptorBuilder removeFlag(NDescriptorFlag flag) {
        if (flag != null) {
            this.flags.remove(flag);
        }
        return this;
    }

    @Override
    public NDescriptorBuilder removeFlags(NDescriptorFlag... flags) {
        if (flags != null) {
            for (NDescriptorFlag flag : flags) {
                if (flag != null) {
                    this.flags.remove(flag);
                }
            }
        }
        return this;
    }

    @Override
    public NOptional<NDescriptorProperty> getProperty(String name) {
        return NOptional.ofNamed(Arrays.stream(_propertiesBuilder.toArray()).filter(x -> x.getName().equals(name)).findFirst()
                .orElse(null), "property " + name);
    }

    @Override
    public NOptional<NLiteral> getPropertyValue(String name) {
        return getProperty(name).map(NDescriptorProperty::getValue);
    }

    public NIdType getIdType() {
        return idType;
    }

    public NDescriptorBuilder setIdType(NIdType idType) {
        this.idType = idType == null ? NIdType.REGULAR : idType;
        return this;
    }


    private void _rebuildPropertiesBuilder() {
        if (_propertiesBuilder == null) {
            _propertiesBuilder = new DefaultNProperties();
            _propertiesBuilder.addAll(this.properties);
        }
    }

    private void _updateProperties() {
        this.properties.clear();
        this.properties.addAll(Arrays.asList(_propertiesBuilder.toArray()));
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
        DefaultNDescriptorBuilder that = (DefaultNDescriptorBuilder) o;
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
    public NDescriptorBuilder builder() {
        return new DefaultNDescriptorBuilder(this);
    }

    @Override
    public boolean isBlank() {
        return build().isBlank();
    }

    @Override
    public boolean isExecutable() {
        return getFlags().contains(NDescriptorFlag.EXEC);
    }

    @Override
    public boolean isNutsApplication() {
        return getFlags().contains(NDescriptorFlag.NUTS_APP);
    }

    @Override
    public boolean isPlatformApplication() {
        return getFlags().contains(NDescriptorFlag.PLATFORM_APP);
    }

    @Override
    public List<NDescriptorContributor> getContributors() {
        return contributors;
    }

    @Override
    public NDescriptorBuilder setContributors(List<NDescriptorContributor> contributors) {
        this.contributors = NReservedLangUtils.uniqueList(contributors);
        return this;
    }

    @Override
    public NDescriptorBuilder setDevelopers(List<NDescriptorContributor> developers) {
        this.developers = NReservedLangUtils.uniqueList(developers);
        return this;
    }

    @Override
    public List<NDescriptorLicense> getLicenses() {
        return licenses;
    }

    @Override
    public NDescriptorBuilder setLicenses(List<NDescriptorLicense> licenses) {
        this.licenses = NReservedLangUtils.uniqueList(licenses);
        return this;
    }

    @Override
    public List<NDescriptorMailingList> getMailingLists() {
        return mailingLists;
    }

    @Override
    public NDescriptorBuilder setMailingLists(List<NDescriptorMailingList> mailingLists) {
        this.mailingLists = NReservedLangUtils.uniqueList(mailingLists);
        return this;
    }

    @Override
    public NDescriptorOrganization getOrganization() {
        return organization;
    }

    @Override
    public NDescriptorBuilder setOrganization(NDescriptorOrganization organization) {
        this.organization = organization;
        return this;
    }

    @Override
    public List<NDescriptorContributor> getDevelopers() {
        return developers;
    }

    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public boolean isNoContent() {
        return "pom".equals(getPackaging());
    }
}
