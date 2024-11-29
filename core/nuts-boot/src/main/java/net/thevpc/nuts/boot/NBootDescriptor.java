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

import net.thevpc.nuts.boot.reserved.util.NBootUtils;
import net.thevpc.nuts.boot.reserved.util.NBootStringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class NBootDescriptor {

    private static final long serialVersionUID = 1L;

    private NBootId id;
    private List<NBootId> parents = new ArrayList<>();
    private String packaging;
    /**
     * short description
     */
    private String name;
    /**
     * some longer (but not too long) description
     */
    private String description;
    private NBootEnvCondition condition;
    private List<NBootDependency> dependencies = new ArrayList<>(); //defaults to empty;
    private List<NBootDependency> standardDependencies = new ArrayList<>(); //defaults to empty;
    private List<NBootDescriptorProperty> properties = new ArrayList<>(); //defaults to empty;
    private transient NBootProperties _propertiesBuilder = new NBootProperties(); //defaults to empty;


    public NBootDescriptor() {
        condition = new NBootEnvCondition();
    }

    public NBootDescriptor(NBootDescriptor other) {
        condition = new NBootEnvCondition();
        setAll(other);
    }

    public NBootId getId() {
        return id;
    }


    public NBootDescriptor setId(NBootId id) {
        this.id = id;
        return this;
    }


    public NBootDescriptor setId(String id) {
        this.id = NBootId.of(id);
        return this;
    }


    public List<NBootId> getParents() {
        return parents;
    }

    public NBootDescriptor setParents(List<NBootId> parents) {
        this.parents = NBootUtils.uniqueNonBlankList(parents, x->x.isBlank());
        return this;
    }


    public String getPackaging() {
        return packaging;
    }


    public NBootDescriptor setPackaging(String packaging) {
        this.packaging = NBootStringUtils.trim(packaging);
        return this;
    }


    public String getName() {
        return name;
    }


    public NBootDescriptor setName(String name) {
        this.name = NBootStringUtils.trim(name);
        return this;
    }


    public NBootEnvCondition getCondition() {
        return condition;
    }


    public NBootDescriptor setCondition(NBootEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }


    public String getDescription() {
        return description;
    }


    public NBootDescriptor setDescription(String description) {
        this.description = NBootStringUtils.trim(description);
        return this;
    }


    public List<NBootDependency> getStandardDependencies() {
        return standardDependencies;
    }


    public NBootDescriptor setStandardDependencies(List<NBootDependency> dependencies) {
        this.standardDependencies = NBootUtils.uniqueNonBlankList(dependencies, x->x.isBlank());
        return this;
    }


    public List<NBootDependency> getDependencies() {
        return dependencies;
    }


    public NBootDescriptor setDependencies(List<NBootDependency> dependencies) {
        this.dependencies = NBootUtils.uniqueNonBlankList(dependencies, x->x.isBlank());
        return this;
    }


    public List<NBootDescriptorProperty> getProperties() {
        return properties;
    }


    public NBootDescriptor setProperties(List<NBootDescriptorProperty> properties) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.clear();
        if (properties == null || properties.size() == 0) {

        } else {
            _propertiesBuilder.addAll(properties);
        }
        _updateProperties();
        return this;
    }


    public NBootDescriptor setProperty(String name, String value) {
        NBootDescriptorProperty pp = new NBootDescriptorProperty()
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

    public NBootDescriptor setAll(NBootDescriptor other) {
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


    public NBootDescriptor clear() {
        setId((NBootId) null);
        setPackaging(null);
        setParents(null);
        setDescription(null);
        setName(null);
        setCondition((NBootEnvCondition) null);
        setDependencies(null);
        setStandardDependencies(null);
        setProperties(null);
        return this;
    }


    public NBootDescriptor removeDependency(NBootDependency dependency) {
        if (this.dependencies != null) {
            this.dependencies.remove(dependency);
        }
        return this;
    }


    public NBootDescriptor addDependency(NBootDependency dependency) {
        if (dependency == null) {
            throw new NullPointerException();
        }
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
        return this;
    }


    public NBootDescriptor addDependencies(List<NBootDependency> dependencies) {
        NBootUtils.addUniqueNonBlankList(this.dependencies, dependencies, x->x.isBlank());
        return this;
    }


    public NBootDescriptor removeStandardDependency(NBootDependency dependency) {
        if (this.standardDependencies != null) {
            this.standardDependencies.remove(dependency);
        }
        return this;
    }


    public NBootDescriptor addStandardDependency(NBootDependency dependency) {
        this.standardDependencies.add(dependency);
        return this;
    }


    public NBootDescriptor addStandardDependencies(List<NBootDependency> dependencies) {
        NBootUtils.addUniqueNonBlankList(this.standardDependencies, dependencies, x->x.isBlank());
        return this;
    }


    public NBootDescriptor addProperty(NBootDescriptorProperty property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.add(property);
        _updateProperties();
        return this;
    }


    public NBootDescriptor removeProperties(NBootDescriptorProperty property) {
        _rebuildPropertiesBuilder();
        _propertiesBuilder.remove(property);
        _updateProperties();
        return this;
    }


    public NBootDescriptor addProperties(List<NBootDescriptorProperty> properties) {
        if (properties == null || properties.size() == 0) {
            //do nothing
        } else {
            _rebuildPropertiesBuilder();
            _propertiesBuilder.addAll(properties);
            _updateProperties();
        }
        return this;
    }


    public NBootDescriptor replaceProperty(Predicate<NBootDescriptorProperty> filter, Function<NBootDescriptorProperty, NBootDescriptorProperty> converter) {
        if (converter == null) {
            return this;
        }

        NBootProperties p = new NBootProperties();
        boolean someUpdate = false;
        for (NBootDescriptorProperty entry : getProperties()) {
            if (filter == null || filter.test(entry)) {
                NBootDescriptorProperty v = converter.apply(entry);
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


    public NBootDescriptor replaceDependency(Predicate<NBootDependency> filter, UnaryOperator<NBootDependency> converter) {
        if (converter == null) {
            return this;
        }
        ArrayList<NBootDependency> dependenciesList = new ArrayList<>();
        for (NBootDependency d : getDependencies()) {
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


    public NBootDescriptor removeDependency(Predicate<NBootDependency> dependency) {
        if (dependency == null) {
            return this;
        }
        for (Iterator<NBootDependency> it = dependencies.iterator(); it.hasNext(); ) {
            NBootDependency d = it.next();
            if (dependency.test(d)) {
                //do not add
                it.remove();
            }
        }
        return this;
    }


    public NBootDescriptorProperty getProperty(String name) {
        return Arrays.stream(_propertiesBuilder.toArray()).filter(x -> x.getName().equals(name)).findFirst()
                .orElse(null);
    }

    private void _rebuildPropertiesBuilder() {
        if (_propertiesBuilder == null) {
            _propertiesBuilder = new NBootProperties();
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
        NBootDescriptor that = (NBootDescriptor) o;
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


    public NBootDescriptor copy() {
        return new NBootDescriptor(this);
    }


    public boolean isBlank() {
        if (id!=null && !id.isBlank()) {
            return false;
        }
        if (!NBootStringUtils.isBlank(packaging)) {
            return false;
        }
        if (parents != null) {
            for (NBootId parent : parents) {
                if (parent!=null && !parent.isBlank()) {
                    return false;
                }
            }
        }

        if (!NBootStringUtils.isBlank(description)) {
            return false;
        }

        if (!NBootStringUtils.isBlank(name)) {
            return false;
        }

        if (condition!=null && !condition.isBlank()) {
            return false;
        }

        if (this.dependencies != null) {
            for (NBootDependency d : this.dependencies) {
                if (d!=null && !d.isBlank()) {
                    return false;
                }
            }
        }
        if (this.standardDependencies != null) {
            for (NBootDependency d : this.standardDependencies) {
                if (d!=null && !d.isBlank()) {
                    return false;
                }
            }
        }
        if (properties != null && properties.size() > 0) {
            for (NBootDescriptorProperty property : properties) {
                if (property!=null && !property.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }


}
