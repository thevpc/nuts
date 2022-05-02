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

import net.thevpc.nuts.reserved.NutsReservedDefaultNutsProperties;
import net.thevpc.nuts.reserved.NutsReservedCollectionUtils;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDescriptor implements NutsDescriptor {

    private static final long serialVersionUID = 1L;
    private final String solver;
    private final NutsArtifactCall executor;
    private final NutsArtifactCall installer;
    private final NutsEnvCondition condition;
    private final List<NutsIdLocation> locations;
    private final List<NutsDependency> dependencies;
    private final List<NutsDependency> standardDependencies;
    private final List<NutsDescriptorProperty> properties;
    private final Set<NutsDescriptorFlag> flags;
    private NutsId id;
    private NutsIdType idType;
    private List<NutsId> parents;
    private String packaging;
    /**
     * short description
     */
    private String name;
    /**
     * some longer (but not too long) description
     */
    private String description;
    private List<String> icons;
    private List<String> categories;
    private String genericName;
    private List<NutsDescriptorContributor> contributors;
    private List<NutsDescriptorLicense> licenses;
    private List<NutsDescriptorMailingList> mailingLists;
    private NutsDescriptorOrganization organization;

    public DefaultNutsDescriptor(NutsDescriptor d) {
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
                d.getFlags(),
                d.getSolver(),
                d.getContributors(),
                d.getLicenses(),
                d.getMailingLists(),
                d.getOrganization()
        );
    }

    public DefaultNutsDescriptor(NutsId id, NutsIdType idType, List<NutsId> parents, String packaging,
                                 //                                 String ext,
                                 NutsArtifactCall executor, NutsArtifactCall installer, String name, String description,
                                 NutsEnvCondition condition,
                                 List<NutsDependency> dependencies,
                                 List<NutsDependency> standardDependencies,
                                 List<NutsIdLocation> locations, List<NutsDescriptorProperty> properties,
                                 String genericName, List<String> categories, List<String> icons,
                                 Set<NutsDescriptorFlag> flags,
                                 String solver,
                                 List<NutsDescriptorContributor> contributors,
                                 List<NutsDescriptorLicense> licenses,
                                 List<NutsDescriptorMailingList> mailingLists,
                                 NutsDescriptorOrganization organization
    ) {
        this.id = id;
        this.idType = idType == null ? NutsIdType.REGULAR : idType;
        this.packaging = NutsStringUtils.trimToNull(packaging);
        this.parents = NutsReservedCollectionUtils.uniqueList(parents);
        this.description = NutsStringUtils.trimToNull(description);
        this.name = NutsStringUtils.trimToNull(name);
        this.genericName = NutsStringUtils.trimToNull(genericName);
        this.icons = NutsReservedCollectionUtils.uniqueNonBlankList(icons);
        this.categories = NutsReservedCollectionUtils.uniqueNonBlankList(categories);
        this.executor = executor;
        this.installer = installer;
        this.condition = condition == null ? NutsEnvCondition.BLANK : condition;
        this.locations = NutsReservedCollectionUtils.uniqueNonBlankList(locations);
        this.dependencies = NutsReservedCollectionUtils.uniqueNonBlankList(dependencies);
        this.standardDependencies = NutsReservedCollectionUtils.uniqueNonBlankList(standardDependencies);
        if (properties == null || properties.size() == 0) {
            this.properties = null;
        } else {
            NutsReservedDefaultNutsProperties p = new NutsReservedDefaultNutsProperties();
            p.addAll(properties);
            this.properties = p.getList();
        }
        this.flags = NutsReservedCollectionUtils.nonBlankSet(flags);
        this.solver = NutsStringUtils.trimToNull(solver);
        this.contributors = NutsReservedCollectionUtils.unmodifiableUniqueList(contributors);
        this.licenses = NutsReservedCollectionUtils.unmodifiableUniqueList(licenses);
        this.mailingLists = NutsReservedCollectionUtils.unmodifiableUniqueList(mailingLists);
        this.organization = organization;
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
        if (idType != NutsIdType.REGULAR) {
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
        if (properties != null && properties.size() > 0) {
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

//    public void check() {
//        if (NutsBlankable.isBlank(id)) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing id"));
//        }
//        if (NutsBlankable.isBlank(id.getGroupId())) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing groupId"));
//        }
//        if (NutsBlankable.isBlank(id.getArtifactId())) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing artifactId"));
//        }
//        if (NutsBlankable.isBlank(id.getVersion())) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing version"));
//        }
//        if (NutsBlankable.isBlank(id.getArtifactId())) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing artifactId for %s", id));
//        }
//        //NutsWorkspaceUtils.of(session).checkSimpleNameNutsId(id);
//        if (!id.isLongId()) {
//            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("id should not have query defined in descriptors : %s", id));
//        }
//    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public List<NutsId> getParents() {
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
    public List<String> getIcons() {
        return icons;
    }

    @Override
    public String getGenericName() {
        return genericName;
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<NutsIdLocation> getLocations() {
        return locations;
    }

    @Override
    public List<NutsDependency> getStandardDependencies() {
        return standardDependencies;
    }

    @Override
    public List<NutsDependency> getDependencies() {
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
    public List<NutsDescriptorProperty> getProperties() {
        return properties;
    }

    @Override
    public NutsDescriptorProperty getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return properties.stream().filter(x -> x.getName().equals(name)).findFirst()
                .orElse(null);
    }

    @Override
    public NutsOptional<NutsValue> getPropertyValue(String name) {
        NutsDescriptorProperty p = getProperty(name);
        return NutsOptional.of(p == null ? null : p.getValue(), session -> NutsMessage.cstyle("property not found : %s", name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idType, packaging,
                executor, installer, name, description, genericName, condition, flags,
                solver, categories, properties, icons, parents, locations, dependencies, standardDependencies
        );
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
                && Objects.equals(parents, that.parents)
                && Objects.equals(solver, that.solver)
                && Objects.equals(packaging, that.packaging)
                && //                        Objects.equals(ext, that.ext) &&
                Objects.equals(executor, that.executor)
                && Objects.equals(installer, that.installer)
                && Objects.equals(name, that.name)
                && Objects.equals(icons, that.icons)
                && Objects.equals(categories, that.categories)
                && Objects.equals(genericName, that.genericName)
                && Objects.equals(description, that.description)
                && Objects.equals(condition, that.condition)
                && Objects.equals(locations, that.locations)
                && Objects.equals(dependencies, that.dependencies)
                && Objects.equals(standardDependencies, that.standardDependencies)
                && Objects.equals(flags, that.flags)
                && Objects.equals(properties, that.properties);
    }

    @Override
    public String toString() {
        return "DefaultNutsDescriptor{"
                + "id=" + id
                + ", idType=" + idType.id()
                + ", parents=" + parents
                + ", packaging='" + packaging + '\''
                + ", executor=" + executor
                + ", flags=" + flags
                + ", installer=" + installer
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", condition=" + condition
                + ", locations=" + locations
                + ", dependencies=" + dependencies
                + ", standardDependencies=" + standardDependencies
                + ", icon=" + icons
                + ", category=" + categories
                + ", genericName=" + genericName
                + ", properties=" + properties
                + ", solver=" + solver
                + '}';
    }

    @Override
    public NutsDescriptorBuilder builder() {
        return new DefaultNutsDescriptorBuilder().setAll(this);
    }

    @Override
    public NutsDescriptorFormat formatter(NutsSession session) {
        return NutsDescriptorFormat.of(session).setValue(this);
    }

    @Override
    public NutsDescriptor readOnly() {
        return this;
    }


    @Override
    public List<NutsDescriptorContributor> getContributors() {
        return contributors;
    }

    @Override
    public List<NutsDescriptorLicense> getLicenses() {
        return licenses;
    }

    @Override
    public List<NutsDescriptorMailingList> getMailingLists() {
        return mailingLists;
    }

    @Override
    public NutsDescriptorOrganization getOrganization() {
        return organization;
    }
}
