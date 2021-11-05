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
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.MapToFunction;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DefaultNutsDescriptorBuilder implements NutsDescriptorBuilder {

    private static final long serialVersionUID = 1L;

    private NutsId id;
    //    private String alternative;
    private NutsId[] parents = new NutsId[0]; //defaults to empty;
    private String packaging;
    //    private String ext;
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
    private transient DefaultNutsProperties _propertiesBuilder = new DefaultNutsProperties(); //defaults to empty;
    private transient NutsSession session;

    public DefaultNutsDescriptorBuilder() {
    }

    public DefaultNutsDescriptorBuilder(NutsSession session) {
        this.session = session;
        condition = new DefaultNutsEnvConditionBuilder(session);
    }

    public DefaultNutsDescriptorBuilder(NutsDescriptor other, NutsSession session) {
        this.session = session;
        condition = new DefaultNutsEnvConditionBuilder(session);
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
        this.id = NutsId.of(id,session);
        return this;
    }

    @Override
    public NutsId[] getParents() {
        return parents;
    }

    public NutsDescriptorBuilder setParents(NutsId[] parents) {
        this.parents = parents == null ? new NutsId[0] : new NutsId[parents.length];
        if (parents != null) {
            System.arraycopy(parents, 0, this.parents, 0, this.parents.length);
        }
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
    public String getSolver() {
        return solver;
    }

    @Override
    public NutsEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NutsDescriptorBuilder setCondition(NutsEnvConditionBuilder condition) {
        this.condition.setAll(condition);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setCondition(NutsEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setCategories(List<String> categories) {
        this.categories = categories == null ? new ArrayList<>() : new ArrayList<>(categories);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setSolver(String solver) {
        this.solver=solver;
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
    public NutsIdLocation[] getLocations() {
        return locations == null ? new NutsIdLocation[0] : locations.toArray(new NutsIdLocation[0]);
    }

    @Override
    public NutsDescriptorBuilder setLocations(NutsIdLocation[] locations) {
        this.locations = (locations == null) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(locations));
        return this;
    }

    @Override
    public NutsDependency[] getStandardDependencies() {
        return standardDependencies == null ? new NutsDependency[0] : standardDependencies.toArray(new NutsDependency[0]);
    }

    @Override
    public NutsDescriptorBuilder setStandardDependencies(NutsDependency[] dependencies) {
        this.standardDependencies = new ArrayList<>();
        if (dependencies != null) {
            for (NutsDependency dependency : dependencies) {
                if (dependency == null) {
                    throw new NullPointerException();
                }
                this.standardDependencies.add(dependency);
            }
        }
        return this;
    }

    @Override
    public NutsDependency[] getDependencies() {
        return dependencies == null ? new NutsDependency[0] : dependencies.toArray(new NutsDependency[0]);
    }

    @Override
    public NutsDescriptorBuilder setDependencies(NutsDependency[] dependencies) {
        this.dependencies = new ArrayList<>();
        if (dependencies != null) {
            for (NutsDependency dependency : dependencies) {
                if (dependency == null) {
                    throw new NullPointerException();
                }
                this.dependencies.add(dependency);
            }
        }
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
    public NutsDescriptorProperty[] getProperties() {
        return properties.toArray(new NutsDescriptorProperty[0]);
    }

    @Override
    public NutsDescriptorBuilder setProperties(NutsDescriptorProperty[] properties) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.clear();
        if (properties == null || properties.length == 0) {

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
        NutsDescriptorProperty pp = NutsDescriptorPropertyBuilder.of(session)
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
    public NutsDescriptorBuilder setAll(NutsDescriptorBuilder other) {
        if (other != null) {
            setId(other.getId());
//            setAlternative(other.getAlternative());
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
            setIcons(new ArrayList<>(Arrays.asList(other.getIcons())));
            setGenericName(other.getGenericName());
            setCategories(new ArrayList<>(Arrays.asList(other.getCategories())));
            setSolver(other.getSolver());
            setFlags(other.getFlags());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder clear() {
        setId((NutsId) null);
//            setAlternative(null);
        setPackaging(null);
        setParents(null);
        setDescription(null);
        setFlags(new LinkedHashSet<>());
        setName(null);
        setExecutor(null);
        setInstaller(null);
        setCondition((NutsEnvCondition) null);
        setLocations(null);
        setDependencies((NutsDependency[]) null);
        setStandardDependencies((NutsDependency[]) null);
        setProperties(null);
        setIcons(new ArrayList<>());
        setCategories(null);
        setGenericName(null);
        setSolver(null);
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
    public NutsDescriptorBuilder addDependencies(NutsDependency[] dependencies) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.addAll(Arrays.asList(dependencies));
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
        if (this.standardDependencies == null) {
            this.standardDependencies = new ArrayList<>();
        }
        this.standardDependencies.add(dependency);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addStandardDependencies(NutsDependency[] dependencies) {
        if (this.standardDependencies == null) {
            this.standardDependencies = new ArrayList<>();
        }
        this.standardDependencies.addAll(Arrays.asList(dependencies));
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
    public NutsDescriptorBuilder addProperties(NutsDescriptorProperty[] properties) {
        if (properties == null || properties.length == 0) {
            //do nothing
        } else {
            _rebuildPropertiesBuilder();
            _propertiesBuilder.addAll(properties);
            _updateProperties();
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder applyProperties() {
        return applyProperties(
                CoreNutsUtils.getPropertiesMap(getProperties(),session)

        );
    }

    @Override
    public NutsDescriptorBuilder applyParents(NutsDescriptor[] parentDescriptors) {
        NutsId n_id = getId();
//        String n_alt = getAlternative();
        String n_packaging = getPackaging();
        LinkedHashSet<NutsDescriptorFlag> flags=new LinkedHashSet<>();
//        String n_ext = getExt();
        flags.addAll(getFlags());
        String n_name = getName();
        List<String> n_categories = getCategories();
        if (n_categories == null) {
            n_categories = new ArrayList<>();
        } else {
            n_categories = new ArrayList<>(n_categories);
        }
        List<String> n_icons = getIcons();
        if (n_icons == null) {
            n_icons = new ArrayList<>();
        } else {
            n_icons = new ArrayList<>(n_icons);
        }
        String n_genericName = getGenericName();
        String n_desc = getDescription();
        NutsArtifactCall n_executor = getExecutor();
        NutsArtifactCall n_installer = getInstaller();
        List<NutsDescriptorProperty> n_props = new ArrayList<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_props.addAll(Arrays.asList(parentDescriptor.getProperties()));
        }
        NutsDescriptorProperty[] properties = getProperties();
        if (properties != null) {
            n_props.addAll(Arrays.asList(properties));
        }
        NutsEnvConditionBuilder b = NutsEnvConditionBuilder.of(session);

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        LinkedHashSet<NutsDependency> n_sdeps = new LinkedHashSet<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_id = CoreNutsUtils.applyNutsIdInheritance(n_id, parentDescriptor.getId(), session);
            flags.addAll(parentDescriptor.getFlags());
            if (n_executor == null) {
                n_executor = parentDescriptor.getExecutor();
            }
            if (n_executor == null) {
                n_installer = parentDescriptor.getInstaller();
            }

            //packaging is not inherited!!
            //n_packaging = applyStringInheritance(n_packaging, parentDescriptor.getPackaging());
//            n_ext = CoreNutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = CoreNutsUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_genericName = CoreNutsUtils.applyStringInheritance(n_genericName, parentDescriptor.getGenericName());
            n_desc = CoreNutsUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
            n_deps.addAll(Arrays.asList(parentDescriptor.getDependencies()));
            n_sdeps.addAll(Arrays.asList(parentDescriptor.getStandardDependencies()));
            b.addAll(parentDescriptor.getCondition());
            n_icons.addAll(Arrays.asList(parentDescriptor.getIcons()));
            n_categories.addAll(Arrays.asList(parentDescriptor.getCategories()));
        }
        n_deps.addAll(Arrays.asList(getDependencies()));
        n_sdeps.addAll(Arrays.asList(getStandardDependencies()));
        b.addAll(getCondition());
        NutsId[] n_parents = new NutsId[0];

        setId(n_id);
//        setAlternative(n_alt);
        setParents(n_parents);
        setPackaging(n_packaging);
        setFlags(flags);
        setExecutor(n_executor);
        setInstaller(n_installer);
        setName(n_name);
        setGenericName(n_genericName);
        setCategories(n_categories);
        setIcons(n_icons);
        setDescription(n_desc);
        setCondition(b);
        setDependencies(n_deps.toArray(new NutsDependency[0]));
        setStandardDependencies(n_sdeps.toArray(new NutsDependency[0]));
        setProperties(n_props.toArray(new NutsDescriptorProperty[0]));
        return this;
    }

    @Override
    public NutsDescriptorBuilder applyProperties(Map<String, String> properties) {
        Function<String, String> map = new MapToFunction<>(properties);

        NutsId n_id = getId().builder().apply(map).build();
//        String n_alt = CoreNutsUtils.applyStringProperties(getAlternative(), map);
        String n_packaging = CoreNutsUtils.applyStringProperties(getPackaging(), map);
        String n_name = CoreNutsUtils.applyStringProperties(getName(), map);
        String n_desc = CoreNutsUtils.applyStringProperties(getDescription(), map);
        NutsArtifactCall n_executor = getExecutor();
        NutsArtifactCall n_installer = getInstaller();
        DefaultNutsProperties n_props = new DefaultNutsProperties();
        for (NutsDescriptorProperty property : getProperties()) {
            String v = property.getValue();
            if(CoreStringUtils.containsVars("${")){
                n_props.add(property.builder().setValue(CoreNutsUtils.applyStringProperties(v, map))
                        .build());
            }else{
                n_props.add(property);
            }
        }

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        for (NutsDependency d2 : getDependencies()) {
            n_deps.add(applyNutsDependencyProperties(d2, map));
        }

        LinkedHashSet<NutsDependency> n_sdeps = new LinkedHashSet<>();
        for (NutsDependency d2 : getStandardDependencies()) {
            n_sdeps.add(applyNutsDependencyProperties(d2, map));
        }

        this.setId(n_id);
//        this.setAlternative(n_alt);
        this.setParents(getParents());
        this.setPackaging(n_packaging);
        this.setExecutor(n_executor);
        this.setInstaller(n_installer);
        this.setName(n_name);
        this.setDescription(n_desc);
        this.setGenericName(CoreNutsUtils.applyStringProperties(getGenericName(), map));
        this.setIcons(
                getIcons().stream()
                        .map(
                                x -> CoreNutsUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        this.setCategories(
                getCategories().stream()
                        .map(
                                x -> CoreNutsUtils.applyStringProperties(x, map)
                        ).collect(Collectors.toList())
        );
        this.getCondition().applyProperties(properties);
        this.setDependencies(n_deps.toArray(new NutsDependency[0]));
        this.setStandardDependencies(n_sdeps.toArray(new NutsDependency[0]));
        this.setProperties(n_props.getAll());
        return this;
    }

    @Override
    public NutsDescriptorBuilder replaceProperty(Predicate<NutsDescriptorProperty> filter, Function<NutsDescriptorProperty, NutsDescriptorProperty> converter) {
        if (converter == null) {
            return this;
        }

        DefaultNutsProperties p = new DefaultNutsProperties();
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
            setProperties(p.getAll());
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
    public NutsDescriptorBuilder copy() {
        return new DefaultNutsDescriptorBuilder(session).setAll(this);
    }

    @Override
    public NutsDescriptor build() {
        LinkedHashSet<NutsDescriptorFlag> flags = new LinkedHashSet<>();
        for (NutsDescriptorProperty property : getProperties()) {
            if (property.getCondition() == null) {
                switch (property.getName()) {
                    case "nuts.application": {
                        flags.add(NutsDescriptorFlag.APP);
                        flags.add(NutsDescriptorFlag.EXEC);
                        break;
                    }
                    case "nuts.executable": {
                        flags.add(NutsDescriptorFlag.EXEC);
                        break;
                    }
                    case "nuts.term": {
                        flags.add(NutsDescriptorFlag.TERM);
                        break;
                    }
                    case "nuts.gui": {
                        flags.add(NutsDescriptorFlag.GUI);
                        break;
                    }
                    case "nuts.extension": {
                        flags.add(NutsDescriptorFlag.NUTS_EXTENSION);
                        break;
                    }
                    case "nuts.runtime": {
                        flags.add(NutsDescriptorFlag.NUTS_RUNTIME);
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
                getId(), /*getAlternative(),*/ getParents(), getPackaging(),
                //                getExt(),
                getExecutor(), getInstaller(),
                getName(), getDescription(), getCondition().build(),
                getDependencies(), getStandardDependencies(),
                getLocations(), getProperties(),
                genericName,
                categories == null ? new String[0] : categories.toArray(new String[0]),
                icons == null ? new String[0] : icons.toArray(new String[0]),
                flags.toArray(new NutsDescriptorFlag[0]),
                getSolver(),
                session
        );
    }

    @Override
    public NutsDescriptorProperty getProperty(String name) {
        return Arrays.stream(_propertiesBuilder.getAll()).filter(x -> x.getName().equals(name)).findFirst()
                .orElse(null);
    }

    @Override
    public String getPropertyValue(String name) {
        NutsDescriptorProperty p = getProperty(name);
        return p == null ? null : p.getValue();
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

    private void _rebuildPropertiesBuilder() {
        if (_propertiesBuilder == null) {
            _propertiesBuilder = new DefaultNutsProperties();
            _propertiesBuilder.addAll(this.properties.toArray(new NutsDescriptorProperty[0]));
        }
    }

    private void _updateProperties() {
        this.properties.clear();
        this.properties.addAll(Arrays.asList(_propertiesBuilder.getAll()));
    }

    private NutsEnvCondition applyNutsConditionProperties(NutsEnvCondition child, Function<String, String> properties) {
        return child
                .builder()
                .setOs(CoreNutsUtils.applyStringProperties(child.getOs(), properties))
                .setOsDist(CoreNutsUtils.applyStringProperties(child.getOsDist(), properties))
                .setPlatform(CoreNutsUtils.applyStringProperties(child.getPlatform(), properties))
                .setDesktopEnvironment(CoreNutsUtils.applyStringProperties(child.getDesktopEnvironment(), properties))
                .setArch(CoreNutsUtils.applyStringProperties(child.getArch(), properties))
                .build();
    }

    private NutsId applyNutsIdProperties(NutsId child, Function<String, String> properties) {
        return NutsIdBuilder.of(session)
                .setRepository(CoreNutsUtils.applyStringProperties(child.getRepository(), properties))
                .setGroupId(CoreNutsUtils.applyStringProperties(child.getGroupId(), properties))
                .setArtifactId(CoreNutsUtils.applyStringProperties(child.getArtifactId(), properties))
                .setVersion(CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties))
                .setCondition(applyNutsConditionProperties(child.getCondition(), properties))
                .setClassifier(CoreNutsUtils.applyStringProperties(child.getClassifier(), properties))
                .setPackaging(CoreNutsUtils.applyStringProperties(child.getPackaging(), properties))
                .setProperties(CoreNutsUtils.applyMapProperties(child.getProperties(), properties))
                .build();
    }

    private NutsDependency applyNutsDependencyProperties(NutsDependency child, Function<String, String> properties) {
        NutsId[] exclusions = child.getExclusions();
        for (int i = 0; i < exclusions.length; i++) {
            exclusions[i] = applyNutsIdProperties(exclusions[i], properties);
        }
        return NutsDependencyBuilder.of(session)
                .setRepository(CoreNutsUtils.applyStringProperties(child.getRepository(), properties))
                .setGroupId(CoreNutsUtils.applyStringProperties(child.getGroupId(), properties))
                .setArtifactId(CoreNutsUtils.applyStringProperties(child.getArtifactId(), properties))
                .setVersion(CoreNutsUtils.applyStringProperties(child.getVersion(), properties, session))
                .setClassifier(CoreNutsUtils.applyStringProperties(child.getClassifier(), properties))
                .setScope(CoreNutsUtils.applyStringProperties(child.getScope(), properties))
                .setOptional(CoreNutsUtils.applyStringProperties(child.getOptional(), properties))
                .setCondition(applyNutsConditionProperties(child.getCondition(), properties))
                .setType(CoreNutsUtils.applyStringProperties(child.getType(), properties))
                .setExclusions(exclusions)
                .setProperties(CoreNutsUtils.applyStringProperties(child.getPropertiesQuery(), properties))
                .build();
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, packaging, executor, installer, name, icons, categories,
                genericName, description, condition, locations, dependencies, standardDependencies, properties, flags,solver, session);
        result = 31 * result + Arrays.hashCode(parents);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsDescriptorBuilder that = (DefaultNutsDescriptorBuilder) o;
        return  Objects.equals(id, that.id) && Arrays.equals(parents, that.parents)
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
                && Objects.equals(session, that.session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
