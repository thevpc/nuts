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
package net.thevpc.nuts.runtime.standalone.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreArrayUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDescriptor extends AbstractNutsDescriptor {

    private static final long serialVersionUID = 1L;
    private final String solver;
    private final NutsArtifactCall executor;
    private final NutsArtifactCall installer;
    private final NutsEnvCondition condition;
    private final NutsIdLocation[] locations;
    private final NutsDependency[] dependencies;
    private final NutsDependency[] standardDependencies;
    private final NutsDescriptorProperty[] properties;
    private final Set<NutsDescriptorFlag> flags;
    private NutsId id;
    private NutsIdType idType;
    //    private String alternative;
    private NutsId[] parents;
    private String packaging;
    /**
     * short description
     */
    private String name;
    /**
     * some longer (but not too long) description
     */
    private String description;
    private String[] icons;
    private String[] categories;
    private String genericName;

    public DefaultNutsDescriptor(NutsDescriptor d, NutsSession session) {
        this(
                d.getId(),
                d.getIdType(),
                d.getParents(),
                d.getPackaging(),
                d.getExecutor(),
                d.getInstaller(),
                d.getName(),
                d.getDescription(),
                d.getCondition(),
                d.getDependencies(),
                d.getStandardDependencies(),
                d.getLocations(),
                d.getProperties(),
                d.getGenericName(),
                d.getCategories(),
                d.getIcons(),
                d.getFlags().toArray(new NutsDescriptorFlag[0]),
                d.getSolver(),
                session
        );
    }

    public DefaultNutsDescriptor(NutsId id, NutsIdType idType, NutsId[] parents, String packaging,
                                 //                                 String ext,
                                 NutsArtifactCall executor, NutsArtifactCall installer, String name, String description,
                                 NutsEnvCondition condition,
                                 NutsDependency[] dependencies,
                                 NutsDependency[] standardDependencies,
                                 NutsIdLocation[] locations, NutsDescriptorProperty[] properties,
                                 String genericName, String[] categories, String[] icons,
                                 NutsDescriptorFlag[] flags,
                                 String solver,
                                 NutsSession session) {
        super(session);
        //id can have empty groupId (namely for executors like 'java')
        this.id = id == null ? NutsId.of("", session) : id;
        this.idType = idType == null ? NutsIdType.REGULAR : idType;
//        this.alternative = NutsUtilStrings.trimToNull(alternative);
        this.packaging = NutsUtilStrings.trimToNull(packaging);
        this.parents = parents == null ? new NutsId[0] : new NutsId[parents.length];
        if (parents != null) {
            System.arraycopy(parents, 0, this.parents, 0, this.parents.length);
        }
        this.description = NutsUtilStrings.trimToNull(description);
        this.name = NutsUtilStrings.trimToNull(name);
        this.genericName = NutsUtilStrings.trimToNull(genericName);
        this.icons = icons == null ? new String[0] :
                Arrays.stream(icons).map(x -> x == null ? "" : x.trim()).filter(x -> x.length() > 0)
                        .toArray(String[]::new);
        this.categories = categories == null ? new String[0] :
                Arrays.stream(categories).map(x -> x == null ? "" : x.trim()).filter(x -> x.length() > 0)
                        .toArray(String[]::new);
        this.executor = executor;
        this.installer = installer;
        this.condition = CoreFilterUtils.trimToBlank(condition, session);
        this.locations = CoreArrayUtils.toArraySet(locations);
        this.dependencies = dependencies == null ? new NutsDependency[0] : new NutsDependency[dependencies.length];
        for (int i = 0; i < this.dependencies.length; i++) {
            if (dependencies[i] == null) {
                throw new NullPointerException();
            }
            this.dependencies[i] = dependencies[i];
        }
        this.standardDependencies = standardDependencies == null ? new NutsDependency[0] : new NutsDependency[standardDependencies.length];
        for (int i = 0; i < this.standardDependencies.length; i++) {
            if (standardDependencies[i] == null) {
                throw new NullPointerException();
            }
            this.standardDependencies[i] = standardDependencies[i];
        }
        if (properties == null || properties.length == 0) {
            this.properties = null;
        } else {
            DefaultNutsProperties p = new DefaultNutsProperties();
            p.addAll(properties);
            this.properties = p.getAll();
        }
        this.flags = Collections.unmodifiableSet(new LinkedHashSet<>(
                Arrays.stream(flags == null ? new NutsDescriptorFlag[0] : flags)
                        .filter(Objects::nonNull).collect(Collectors.toList())
        ));
        this.solver = NutsUtilStrings.trimToNull(solver);
    }

    @Override
    public NutsIdType getIdType() {
        return idType;
    }

    public boolean isBlank() {
        if (!NutsBlankable.isBlank(id)) {
            return false;
        }
        if (!NutsBlankable.isBlank(packaging)) {
            return false;
        }
        if (idType!=NutsIdType.REGULAR) {
            return false;
        }
        if (parents != null) {
            for (NutsId parent : parents) {
                if (!NutsBlankable.isBlank(parent)) {
                    return false;
                }
            }
        }

        if (!NutsBlankable.isBlank(description)) {
            return false;
        }
        if (!NutsBlankable.isBlank(name)) {
            return false;
        }
        if (!NutsBlankable.isBlank(genericName)) {
            return false;
        }
        if (this.icons != null) {
            for (String d : this.icons) {
                if (!NutsBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (this.categories != null) {
            for (String d : this.categories) {
                if (!NutsBlankable.isBlank(d)) {
                    return false;
                }
            }
        }

        if (!NutsBlankable.isBlank(executor)) {
            return false;
        }
        if (!NutsBlankable.isBlank(installer)) {
            return false;
        }
        if (!NutsBlankable.isBlank(condition)) {
            return false;
        }

        for (NutsIdLocation d : this.locations) {
            if (!NutsBlankable.isBlank(d)) {
                return false;
            }
        }
        if (this.dependencies != null) {
            for (NutsDependency d : this.dependencies) {
                if (!NutsBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (this.standardDependencies != null) {
            for (NutsDependency d : this.standardDependencies) {
                if (!NutsBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (properties != null && properties.length > 0) {
            for (NutsDescriptorProperty property : properties) {
                if (!NutsBlankable.isBlank(property)) {
                    return false;
                }
            }
        }
        if (!flags.isEmpty()) {
            return false;
        }
        return NutsBlankable.isBlank(this.solver);
    }

    public boolean isValid() {
        if (NutsBlankable.isBlank(id)) {
            return false;
        }
        if (NutsBlankable.isBlank(id.getGroupId())) {
            return false;
        }
        if (NutsBlankable.isBlank(id.getArtifactId())) {
            return false;
        }
        if (NutsBlankable.isBlank(id.getVersion())) {
            return false;
        }
        return id.isLongId();
    }

    public void check() {
        if (NutsBlankable.isBlank(id)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing id"));
        }
        if (NutsBlankable.isBlank(id.getGroupId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing groupId"));
        }
        if (NutsBlankable.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing artifactId"));
        }
        if (NutsBlankable.isBlank(id.getVersion())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing version"));
        }
        if (NutsBlankable.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing artifactId for %s", id));
        }
        //NutsWorkspaceUtils.of(session).checkSimpleNameNutsId(id);
        if (!id.isLongId()) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("id should not have query defined in descriptors : %s", id));
        }
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsId[] getParents() {
        return parents;
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
    public Set<NutsDescriptorFlag> getFlags() {
        return flags;
    }

    //    @Override
//    public String getExt() {
//        return ext;
//    }
    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public String getSolver() {
        return solver;
    }

    @Override
    public NutsEnvCondition getCondition() {
        return condition;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getIcons() {
        return icons;
    }

    @Override
    public String getGenericName() {
        return genericName;
    }

    @Override
    public String[] getCategories() {
        return categories;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public NutsIdLocation[] getLocations() {
        return locations;
    }

    @Override
    public NutsDependency[] getStandardDependencies() {
        return standardDependencies;
    }

    @Override
    public NutsDependency[] getDependencies() {
        return dependencies;
    }

    @Override
    public NutsArtifactCall getExecutor() {
        return executor;
    }

    @Override
    public NutsArtifactCall getInstaller() {
        return installer;
    }

    @Override
    public NutsDescriptorProperty[] getProperties() {
        return properties == null ? new NutsDescriptorProperty[0] : properties;
    }

    @Override
    public NutsDescriptorProperty getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return Arrays.stream(properties).filter(x -> x.getName().equals(name)).findFirst()
                .orElse(null);
    }

    @Override
    public String getPropertyValue(String name) {
        NutsDescriptorProperty p = getProperty(name);
        return p == null ? null : p.getValue();
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id, idType, packaging,
                //                ext,
                executor, installer, name, description, genericName, condition, flags,
                solver
        );
        result = 31 * result + Arrays.hashCode(categories);
        result = 31 * result + Arrays.hashCode(properties);
        result = 31 * result + Arrays.hashCode(icons);
        result = 31 * result + Arrays.hashCode(parents);
        result = 31 * result + Arrays.hashCode(locations);
        result = 31 * result + Arrays.hashCode(dependencies);
        result = 31 * result + Arrays.hashCode(standardDependencies);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultNutsDescriptor that = (DefaultNutsDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(idType, that.idType)
                && Arrays.equals(parents, that.parents)
                && Objects.equals(solver, that.solver)
                && Objects.equals(packaging, that.packaging)
                && //                        Objects.equals(ext, that.ext) &&
                Objects.equals(executor, that.executor)
                && Objects.equals(installer, that.installer)
                && Objects.equals(name, that.name)
                && Arrays.equals(icons, that.icons)
                && Arrays.equals(categories, that.categories)
                && Objects.equals(genericName, that.genericName)
                && Objects.equals(description, that.description)
                && Objects.equals(condition, that.condition)
                && Arrays.equals(locations, that.locations)
                && Arrays.equals(dependencies, that.dependencies)
                && Arrays.equals(standardDependencies, that.standardDependencies)
                && Objects.equals(flags, that.flags)
                && Arrays.equals(properties, that.properties);
    }

    @Override
    public String toString() {
        return "DefaultNutsDescriptor{"
                + "id=" + id
                + ", idType=" + idType.id()
                + ", parents=" + Arrays.toString(parents)
                + ", packaging='" + packaging + '\''
                + ", executor=" + executor
                + ", flags=" + flags
                + ", installer=" + installer
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", condition=" + condition
                + ", locations=" + Arrays.toString(locations)
                + ", dependencies=" + Arrays.toString(dependencies)
                + ", standardDependencies=" + Arrays.toString(standardDependencies)
                + ", icon=" + Arrays.toString(icons)
                + ", category=" + Arrays.toString(categories)
                + ", genericName=" + genericName
                + ", properties=" + Arrays.toString(properties)
                + ", solver=" + solver
                + '}';
    }
}
