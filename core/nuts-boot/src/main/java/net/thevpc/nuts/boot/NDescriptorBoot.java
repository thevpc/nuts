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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NReservedLangUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class NDescriptorBoot {

    private static final long serialVersionUID = 1L;

    private NIdBoot id;
    private List<NIdBoot> parents = new ArrayList<>();
    private String packaging;
    /**
     * short description
     */
    private String name;
    /**
     * some longer (but not too long) description
     */
    private String description;
    private NEnvConditionBoot condition;
    private List<NDependencyBoot> dependencies = new ArrayList<>(); //defaults to empty;
    private List<NDependencyBoot> standardDependencies = new ArrayList<>(); //defaults to empty;
    private List<NDescriptorPropertyBoot> properties = new ArrayList<>(); //defaults to empty;
    private transient NPropertiesBoot _propertiesBuilder = new NPropertiesBoot(); //defaults to empty;


    public NDescriptorBoot() {
        condition = new NEnvConditionBoot();
    }

    public NDescriptorBoot(NDescriptorBoot other) {
        condition = new NEnvConditionBoot();
        setAll(other);
    }

    public NIdBoot getId() {
        return id;
    }


    public NDescriptorBoot setId(NIdBoot id) {
        this.id = id;
        return this;
    }


    public NDescriptorBoot setId(String id) {
        this.id = NIdBoot.of(id);
        return this;
    }


    public List<NIdBoot> getParents() {
        return parents;
    }

    public NDescriptorBoot setParents(List<NIdBoot> parents) {
        this.parents = NReservedLangUtilsBoot.uniqueNonBlankList(parents,x->x.isBlank());
        return this;
    }


    public String getPackaging() {
        return packaging;
    }


    public NDescriptorBoot setPackaging(String packaging) {
        this.packaging = NStringUtilsBoot.trim(packaging);
        return this;
    }


    public String getName() {
        return name;
    }


    public NDescriptorBoot setName(String name) {
        this.name = NStringUtilsBoot.trim(name);
        return this;
    }


    public NEnvConditionBoot getCondition() {
        return condition;
    }


    public NDescriptorBoot setCondition(NEnvConditionBoot condition) {
        this.condition.setAll(condition);
        return this;
    }


    public String getDescription() {
        return description;
    }


    public NDescriptorBoot setDescription(String description) {
        this.description = NStringUtilsBoot.trim(description);
        return this;
    }


    public List<NDependencyBoot> getStandardDependencies() {
        return standardDependencies;
    }


    public NDescriptorBoot setStandardDependencies(List<NDependencyBoot> dependencies) {
        this.standardDependencies = NReservedLangUtilsBoot.uniqueNonBlankList(dependencies,x->x.isBlank());
        return this;
    }


    public List<NDependencyBoot> getDependencies() {
        return dependencies;
    }


    public NDescriptorBoot setDependencies(List<NDependencyBoot> dependencies) {
        this.dependencies = NReservedLangUtilsBoot.uniqueNonBlankList(dependencies,x->x.isBlank());
        return this;
    }


    public List<NDescriptorPropertyBoot> getProperties() {
        return properties;
    }


    public NDescriptorBoot setProperties(List<NDescriptorPropertyBoot> properties) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.clear();
        if (properties == null || properties.size() == 0) {

        } else {
            _propertiesBuilder.addAll(properties);
        }
        _updateProperties();
        return this;
    }


    public NDescriptorBoot setProperty(String name, String value) {
        NDescriptorPropertyBoot pp = new NDescriptorPropertyBoot()
                .setName(name)
                .setValue(value);
        _rebuildPropertiesBuilder();
        if (value == null) {
            _propertiesBuilder.remove(pp);
        } else {
            properties.add(pp);
        }
        _updateProperties();
        return this;
    }

    public NDescriptorBoot setAll(NDescriptorBoot other) {
        if (other != null) {
            setId(other.getId());
            setPackaging(other.getPackaging());
            setParents(other.getParents());
            setDescription(other.getDescription());
            setName(other.getName());
            setCondition(other.getCondition());
            setDependencies(other.getDependencies());
            setStandardDependencies(other.getStandardDependencies());
            setProperties(other.getProperties());
        } else {
            clear();
        }
        return this;
    }


    public NDescriptorBoot clear() {
        setId((NIdBoot) null);
        setPackaging(null);
        setParents(null);
        setDescription(null);
        setName(null);
        setCondition((NEnvConditionBoot) null);
        setDependencies(null);
        setStandardDependencies(null);
        setProperties(null);
        return this;
    }


    public NDescriptorBoot removeDependency(NDependencyBoot dependency) {
        if (this.dependencies != null) {
            this.dependencies.remove(dependency);
        }
        return this;
    }


    public NDescriptorBoot addDependency(NDependencyBoot dependency) {
        if (dependency == null) {
            throw new NullPointerException();
        }
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
        return this;
    }


    public NDescriptorBoot addDependencies(List<NDependencyBoot> dependencies) {
        NReservedLangUtilsBoot.addUniqueNonBlankList(this.dependencies, dependencies,x->x.isBlank());
        return this;
    }


    public NDescriptorBoot removeStandardDependency(NDependencyBoot dependency) {
        if (this.standardDependencies != null) {
            this.standardDependencies.remove(dependency);
        }
        return this;
    }


    public NDescriptorBoot addStandardDependency(NDependencyBoot dependency) {
        this.standardDependencies.add(dependency);
        return this;
    }


    public NDescriptorBoot addStandardDependencies(List<NDependencyBoot> dependencies) {
        NReservedLangUtilsBoot.addUniqueNonBlankList(this.standardDependencies, dependencies,x->x.isBlank());
        return this;
    }


    public NDescriptorBoot addProperty(NDescriptorPropertyBoot property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.add(property);
        _updateProperties();
        return this;
    }


    public NDescriptorBoot removeProperties(NDescriptorPropertyBoot property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.remove(property);
        _updateProperties();
        return this;
    }


    public NDescriptorBoot addProperties(List<NDescriptorPropertyBoot> properties) {
        if (properties == null || properties.size() == 0) {
            //do nothing
        } else {
            _rebuildPropertiesBuilder();
            _propertiesBuilder.addAll(properties);
            _updateProperties();
        }
        return this;
    }


    public NDescriptorBoot replaceProperty(Predicate<NDescriptorPropertyBoot> filter, Function<NDescriptorPropertyBoot, NDescriptorPropertyBoot> converter) {
        if (converter == null) {
            return this;
        }

        NPropertiesBoot p = new NPropertiesBoot();
        boolean someUpdate = false;
        for (NDescriptorPropertyBoot entry : getProperties()) {
            if (filter == null || filter.test(entry)) {
                NDescriptorPropertyBoot v = converter.apply(entry);
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


    public NDescriptorBoot replaceDependency(Predicate<NDependencyBoot> filter, UnaryOperator<NDependencyBoot> converter) {
        if (converter == null) {
            return this;
        }
        ArrayList<NDependencyBoot> dependenciesList = new ArrayList<>();
        for (NDependencyBoot d : getDependencies()) {
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


    public NDescriptorBoot removeDependency(Predicate<NDependencyBoot> dependency) {
        if (dependency == null) {
            return this;
        }
        for (Iterator<NDependencyBoot> it = dependencies.iterator(); it.hasNext(); ) {
            NDependencyBoot d = it.next();
            if (dependency.test(d)) {
                //do not add
                it.remove();
            }
        }
        return this;
    }


    public NDescriptorPropertyBoot getProperty(String name) {
        return Arrays.stream(_propertiesBuilder.toArray()).filter(x -> x.getName().equals(name)).findFirst()
                .orElse(null);
    }

    private void _rebuildPropertiesBuilder() {
        if (_propertiesBuilder == null) {
            _propertiesBuilder = new NPropertiesBoot();
            _propertiesBuilder.addAll(this.properties);
        }
    }

    private void _updateProperties() {
        this.properties.clear();
        this.properties.addAll(Arrays.asList(_propertiesBuilder.toArray()));
    }


    public int hashCode() {
        return Objects.hash(id, packaging, name,
                description, condition, dependencies, standardDependencies, properties, parents);
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDescriptorBoot that = (NDescriptorBoot) o;
        return Objects.equals(id, that.id)
                && Objects.equals(parents, that.parents)
                && Objects.equals(packaging, that.packaging)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(condition, that.condition)
                && Objects.equals(dependencies, that.dependencies)
                && Objects.equals(standardDependencies, that.standardDependencies)
                && Objects.equals(properties, that.properties)
                ;
    }


    public NDescriptorBoot copy() {
        return new NDescriptorBoot(this);
    }


    public boolean isBlank() {
        if (id!=null && !id.isBlank()) {
            return false;
        }
        if (!NStringUtilsBoot.isBlank(packaging)) {
            return false;
        }
        if (parents != null) {
            for (NIdBoot parent : parents) {
                if (parent!=null && !parent.isBlank()) {
                    return false;
                }
            }
        }

        if (!NStringUtilsBoot.isBlank(description)) {
            return false;
        }

        if (!NStringUtilsBoot.isBlank(name)) {
            return false;
        }

        if (condition!=null && !condition.isBlank()) {
            return false;
        }

        if (this.dependencies != null) {
            for (NDependencyBoot d : this.dependencies) {
                if (d!=null && !d.isBlank()) {
                    return false;
                }
            }
        }
        if (this.standardDependencies != null) {
            for (NDependencyBoot d : this.standardDependencies) {
                if (d!=null && !d.isBlank()) {
                    return false;
                }
            }
        }
        if (properties != null && properties.size() > 0) {
            for (NDescriptorPropertyBoot property : properties) {
                if (property!=null && !property.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }


}
