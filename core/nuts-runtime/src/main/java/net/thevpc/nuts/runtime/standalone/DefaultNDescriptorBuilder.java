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

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.boot.NBootDescriptor;
import net.thevpc.nuts.artifact.NIdLocation;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@NScore(fixed = NScorable.DEFAULT_SCORE)
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
    private final NEnvConditionBuilder condition;
    private List<NIdLocation> locations = new ArrayList<>(); //defaults to empty;
    private List<NDependency> dependencies = new ArrayList<>(); //defaults to empty;
    private List<NDependency> standardDependencies = new ArrayList<>(); //defaults to empty;
    private Set<NDescriptorFlag> flags = new LinkedHashSet<>();
    private final List<NDescriptorProperty> properties = new ArrayList<>(); //defaults to empty;
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
    public NId id() {
        return id;
    }

    @Override
    public NDescriptorBuilder id(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NDescriptorBuilder id(String id) {
        this.id = NId.get(id).get();
        return this;
    }

    @Override
    public List<NId> parents() {
        return parents;
    }

    public NDescriptorBuilder parents(List<NId> parents) {
        this.parents = NReservedLangUtils.uniqueNonBlankList(parents);
        return this;
    }

    @Override
    public String packaging() {
        return packaging;
    }

    @Override
    public NDescriptorBuilder packaging(String packaging) {
        this.packaging = NStringUtils.strip(packaging);
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NDescriptorBuilder name(String name) {
        this.name = NStringUtils.strip(name);
        return this;
    }

    @Override
    public String solver() {
        return solver;
    }

    @Override
    public NDescriptorBuilder solver(String solver) {
        this.solver = solver;
        return this;
    }

    @Override
    public String genericName() {
        return genericName;
    }

    @Override
    public NDescriptorBuilder genericName(String name) {
        this.genericName = name;
        return this;
    }

    @Override
    public List<String> icons() {
        return icons;
    }

    @Override
    public NDescriptorBuilder icons(List<String> icons) {
        this.icons(icons == null ? null : icons.toArray(new String[0]));
        return this;
    }

    @Override
    public NDescriptorBuilder icons(String... icons) {
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
    public NDescriptorBuilder locations(NIdLocation... locations) {
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
    public List<String> categories() {
        return categories;
    }

    @Override
    public NDescriptorBuilder categories(List<String> categories) {
        this.categories(categories == null ? null : categories.toArray(new String[0]));
        return this;
    }

    @Override
    public NDescriptorBuilder categories(String... categories) {
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
    public NEnvConditionBuilder condition() {
        return condition;
    }

    @Override
    public NDescriptorBuilder condition(NEnvCondition condition) {
        this.condition.copyFrom(condition);
        return this;
    }

    @Override
    public NDescriptorBuilder condition(NEnvConditionBuilder condition) {
        this.condition.copyFrom(condition);
        return this;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public NDescriptorBuilder description(String description) {
        this.description = NStringUtils.strip(description);
        return this;
    }

    @Override
    public List<NIdLocation> locations() {
        return locations;
    }

    @Override
    public NDescriptorBuilder locations(List<NIdLocation> locations) {
        locations(locations == null ? null : locations.toArray(new NIdLocation[0]));
        return this;
    }

    @Override
    public List<NDependency> standardDependencies() {
        return standardDependencies;
    }

    @Override
    public NDescriptorBuilder standardDependencies(List<NDependency> dependencies) {
        this.standardDependencies = NReservedLangUtils.uniqueNonBlankList(dependencies);
        return this;
    }

    @Override
    public List<NDependency> dependencies() {
        return dependencies;
    }

    @Override
    public NDescriptorBuilder dependencies(List<NDependency> dependencies) {
        this.dependencies = NReservedLangUtils.uniqueNonBlankList(dependencies);
        return this;
    }

    @Override
    public NArtifactCall executor() {
        return executor;
    }

    @Override
    public NDescriptorBuilder executor(NArtifactCall executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public NArtifactCall installer() {
        return installer;
    }

    @Override
    public NDescriptorBuilder installer(NArtifactCall installer) {
        this.installer = installer;
        return this;
    }

    @Override
    public List<NDescriptorProperty> properties() {
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
                .name(name)
                .value(value)
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
            id(other.id());
            idType(other.idType());
            packaging(other.packaging());
            parents(other.parents());
            description(other.description());
            name(other.name());
            executor(other.executor());
            installer(other.installer());
//            setExt(other.getExt());
            condition(other.condition());
            locations(other.locations());
            dependencies(other.dependencies());
            standardDependencies(other.standardDependencies());
            setProperties(other.properties());
            icons(new ArrayList<>(other.icons()));
            categories(other.categories());
            genericName(other.genericName());
            solver(other.solver());
            flags(other.flags());
        } else {
            clear();
        }
        return this;
    }

    //    @Override
    public NDescriptorBuilder copyFrom(NBootDescriptor other) {
        if (other != null) {
            id(other.getId() == null ? null : NId.get(other.getId().toString()).get());
            packaging(other.getPackaging());
            parents(other.getParents() == null ? null : other.getParents().stream().map(x -> NId.get(x.toString()).get()).collect(Collectors.toList()));
            description(other.getDescription());
            name(other.getName());
            condition(other.getCondition() == null ? null : new DefaultNEnvConditionBuilder().copyFrom(other.getCondition()).build());
            dependencies(other.getDependencies() == null ? null : other.getDependencies().stream().map(x -> new DefaultNDependencyBuilder().copyFrom(x).build()).collect(Collectors.toList()));
            standardDependencies(other.getStandardDependencies() == null ? null : other.getDependencies().stream().map(x -> new DefaultNDependencyBuilder().copyFrom(x).build()).collect(Collectors.toList()));
            setProperties(other.getProperties() == null ? null : other.getProperties().stream().map(x -> new DefaultNDescriptorPropertyBuilder().copyFrom(x).build()).collect(Collectors.toList()));

            idType(null);
            executor(null);
            installer(null);
            this.locations();
            this.icons();
            this.categories();
            genericName(null);
            solver(null);
            this.flags();
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NDescriptorBuilder copyFrom(NDescriptor other) {
        if (other != null) {
            id(other.id());
            idType(other.idType());
            packaging(other.packaging());
            parents(other.parents());
            description(other.description());
            name(other.name());
            executor(other.executor());
            installer(other.installer());
            condition(other.condition());
            locations(other.locations());
            dependencies(other.dependencies());
            standardDependencies(other.standardDependencies());
            setProperties(other.properties());
            icons(other.icons());
            genericName(other.genericName());
            categories(other.categories());
            solver(other.solver());
            flags(other.flags());
            organization(other.organization());
            contributors(other.contributors());
            developers(other.developers());
            licenses(other.licenses());
            mailingLists(other.mailingLists());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NDescriptorBuilder clear() {
        id((NId) null);
        idType(null);
        packaging(null);
        parents(null);
        description(null);
        flags(new LinkedHashSet<>());
        name(null);
        executor(null);
        installer(null);
        condition((NEnvCondition) null);
        this.locations();
        dependencies(null);
        standardDependencies(null);
        setProperties(null);
        this.icons();
        this.categories();
        genericName(null);
        solver(null);
        organization(null);
        contributors(null);
        licenses(null);
        developers(null);
        mailingLists(null);
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
        if (!NBlankable.isBlank(dependency)) {
            if (this.dependencies == null) {
                this.dependencies = new ArrayList<>();
            }
            this.dependencies.add(dependency);
        }
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
        for (NDescriptorProperty entry : properties()) {
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
        for (NDependency d : dependencies()) {
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
        NIdType idType = idType();
        for (NDescriptorProperty property : properties()) {
            if (NBlankable.isBlank(property.condition())) {
                switch (property.name()) {
                    case "nuts.application": {
                        if (property.value().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.NUTS_APP);
                            flags.add(NDescriptorFlag.EXEC);
                        }
                        break;
                    }
                    case "nuts.executable": {
                        if (property.value().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.EXEC);
                        }
                        break;
                    }
                    case "nuts.term": {
                        if (property.value().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.TERM);
                        }
                        break;
                    }
                    case "nuts.gui": {
                        if (property.value().asBoolean().orElse(false)) {
                            flags.add(NDescriptorFlag.GUI);
                        }
                        break;
                    }
                    case "nuts.extension": {
                        if (property.value().asBoolean().orElse(false)) {
                            idType = NIdType.EXTENSION;
                        }
                        break;
                    }
                    case "nuts.runtime": {
                        if (property.value().asBoolean().orElse(false)) {
                            idType = NIdType.RUNTIME;
                        }
                        break;
                    }
                    case "nuts.companion": {
                        if (property.value().asBoolean().orElse(false)) {
                            idType = NIdType.COMPANION;
                        }
                        break;
                    }
                }
            }
        }

        NId id1 = id();
        if (!NBlankable.isBlank(id1)) {
            if (Objects.equals(id1.shortName(), NConstants.Ids.NUTS_API)) {
                idType = NIdType.API;
            }

            if (Objects.equals(id1.shortName(), NConstants.Ids.NUTS_RUNTIME)) {
                idType = NIdType.RUNTIME;
            }
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
                id1, idType, parents(), packaging(),
                executor(), installer(),
                name(), description(), condition().build(),
                dependencies(), standardDependencies(),
                locations(), properties(),
                genericName,
                categories,
                icons,
                flags,
                solver(),
                contributors(),
                developers(),
                licenses(),
                mailingLists(),
                organization()
        );
    }

    @Override
    public NDescriptorBuilder copy() {
        return new DefaultNDescriptorBuilder().copyFrom(this);
    }

    public Set<NDescriptorFlag> flags() {
        return flags;
    }

    public NDescriptorBuilder flags(Set<NDescriptorFlag> flags) {
        this.flags = flags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(flags);
        return this;
    }

    public NDescriptorBuilder flags(NDescriptorFlag... flags) {
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
        return NOptional.ofNamed(Arrays.stream(_propertiesBuilder.toArray()).filter(x -> x.name().equals(name)).findFirst()
                .orElse(null), "property " + name);
    }

    @Override
    public NOptional<NLiteral> getPropertyValue(String name) {
        return getProperty(name).map(NDescriptorProperty::value);
    }

    public NIdType idType() {
        return idType;
    }

    public NDescriptorBuilder idType(NIdType idType) {
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
        return flags().contains(NDescriptorFlag.EXEC);
    }

    @Override
    public boolean isNutsApplication() {
        return flags().contains(NDescriptorFlag.NUTS_APP);
    }

    @Override
    public boolean isPlatformApplication() {
        return flags().contains(NDescriptorFlag.PLATFORM_APP);
    }

    @Override
    public List<NDescriptorContributor> contributors() {
        return contributors;
    }

    @Override
    public NDescriptorBuilder contributors(List<NDescriptorContributor> contributors) {
        this.contributors = NReservedLangUtils.uniqueList(contributors);
        return this;
    }

    @Override
    public NDescriptorBuilder developers(List<NDescriptorContributor> developers) {
        this.developers = NReservedLangUtils.uniqueList(developers);
        return this;
    }

    @Override
    public List<NDescriptorLicense> licenses() {
        return licenses;
    }

    @Override
    public NDescriptorBuilder licenses(List<NDescriptorLicense> licenses) {
        this.licenses = NReservedLangUtils.uniqueList(licenses);
        return this;
    }

    @Override
    public List<NDescriptorMailingList> mailingLists() {
        return mailingLists;
    }

    @Override
    public NDescriptorBuilder mailingLists(List<NDescriptorMailingList> mailingLists) {
        this.mailingLists = NReservedLangUtils.uniqueList(mailingLists);
        return this;
    }

    @Override
    public NDescriptorOrganization organization() {
        return organization;
    }

    @Override
    public NDescriptorBuilder organization(NDescriptorOrganization organization) {
        this.organization = organization;
        return this;
    }

    @Override
    public List<NDescriptorContributor> developers() {
        return developers;
    }

    public boolean isNoContent() {
        return "pom".equals(packaging());
    }
}
