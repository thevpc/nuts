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
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.util.*;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNDescriptor implements NDescriptor {

    private static final long serialVersionUID = 1L;
    private final String solver;
    private final NArtifactCall executor;
    private final NArtifactCall installer;
    private final NEnvCondition condition;
    private final List<NIdLocation> locations;
    private final List<NDependency> dependencies;
    private final List<NDependency> standardDependencies;
    private final List<NDescriptorProperty> properties;
    private final Set<NDescriptorFlag> flags;
    private NId id;
    private NIdType idType;
    private List<NId> parents;
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
    private List<NDescriptorContributor> contributors;
    private List<NDescriptorContributor> developers;
    private List<NDescriptorLicense> licenses;
    private List<NDescriptorMailingList> mailingLists;
    private NDescriptorOrganization organization;

    public DefaultNDescriptor(NDescriptor d) {
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
                d.getDevelopers(),
                d.getLicenses(),
                d.getMailingLists(),
                d.getOrganization()
        );
    }

    public DefaultNDescriptor(NId id, NIdType idType, List<NId> parents, String packaging,
                              //                                 String ext,
                              NArtifactCall executor, NArtifactCall installer, String name, String description,
                              NEnvCondition condition,
                              List<NDependency> dependencies,
                              List<NDependency> standardDependencies,
                              List<NIdLocation> locations, List<NDescriptorProperty> properties,
                              String genericName, List<String> categories, List<String> icons,
                              Set<NDescriptorFlag> flags,
                              String solver,
                              List<NDescriptorContributor> contributors,
                              List<NDescriptorContributor> developers,
                              List<NDescriptorLicense> licenses,
                              List<NDescriptorMailingList> mailingLists,
                              NDescriptorOrganization organization
    ) {
        this.id = id;
        this.idType = idType == null ? NIdType.REGULAR : idType;
        this.packaging = NStringUtils.trimToNull(packaging);
        this.parents = NReservedLangUtils.uniqueList(parents);
        this.description = NStringUtils.trimToNull(description);
        this.name = NStringUtils.trimToNull(name);
        this.genericName = NStringUtils.trimToNull(genericName);
        this.icons = NReservedLangUtils.uniqueNonBlankList(icons);
        this.categories = NReservedLangUtils.uniqueNonBlankList(categories);
        this.executor = executor;
        this.installer = installer;
        this.condition = condition == null ? NEnvCondition.BLANK : condition;
        this.locations = NReservedLangUtils.uniqueNonBlankList(locations);
        this.dependencies = NReservedLangUtils.uniqueNonBlankList(dependencies);
        this.standardDependencies = NReservedLangUtils.uniqueNonBlankList(standardDependencies);
        if (properties == null || properties.size() == 0) {
            this.properties = null;
        } else {
            DefaultNProperties p = new DefaultNProperties();
            p.addAll(properties);
            this.properties = p.toList();
        }
        this.flags = NReservedLangUtils.nonBlankSet(flags);
        this.solver = NStringUtils.trimToNull(solver);
        this.contributors = NReservedLangUtils.unmodifiableUniqueList(contributors);
        this.developers = NReservedLangUtils.unmodifiableUniqueList(developers);
        this.licenses = NReservedLangUtils.unmodifiableUniqueList(licenses);
        this.mailingLists = NReservedLangUtils.unmodifiableUniqueList(mailingLists);
        this.organization = organization;
    }

    @Override
    public NIdType getIdType() {
        return idType;
    }

    public boolean isBlank() {
        if (!NBlankable.isBlank(id)) {
            return false;
        }
        if (!NBlankable.isBlank(packaging)) {
            return false;
        }
        if (idType != NIdType.REGULAR) {
            return false;
        }
        if (parents != null) {
            for (NId parent : parents) {
                if (!NBlankable.isBlank(parent)) {
                    return false;
                }
            }
        }

        if (!NBlankable.isBlank(description)) {
            return false;
        }
        if (!NBlankable.isBlank(name)) {
            return false;
        }
        if (!NBlankable.isBlank(genericName)) {
            return false;
        }
        if (this.icons != null) {
            for (String d : this.icons) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (this.developers != null) {
            for (NDescriptorContributor d : this.developers) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (this.contributors != null) {
            for (NDescriptorContributor d : this.contributors) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (this.licenses != null) {
            for (NDescriptorLicense d : this.licenses) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (this.categories != null) {
            for (String d : this.categories) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }

        if (!NBlankable.isBlank(executor)) {
            return false;
        }
        if (!NBlankable.isBlank(installer)) {
            return false;
        }
        if (!NBlankable.isBlank(condition)) {
            return false;
        }

        for (NIdLocation d : this.locations) {
            if (!NBlankable.isBlank(d)) {
                return false;
            }
        }
        if (this.dependencies != null) {
            for (NDependency d : this.dependencies) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (this.standardDependencies != null) {
            for (NDependency d : this.standardDependencies) {
                if (!NBlankable.isBlank(d)) {
                    return false;
                }
            }
        }
        if (properties != null && properties.size() > 0) {
            for (NDescriptorProperty property : properties) {
                if (!NBlankable.isBlank(property)) {
                    return false;
                }
            }
        }
        if (!flags.isEmpty()) {
            return false;
        }
        return NBlankable.isBlank(this.solver);
    }

    public boolean isValid() {
        if (NBlankable.isBlank(id)) {
            return false;
        }
        if (NBlankable.isBlank(id.getGroupId())) {
            return false;
        }
        if (NBlankable.isBlank(id.getArtifactId())) {
            return false;
        }
        if (NBlankable.isBlank(id.getVersion())) {
            return false;
        }
        return id.isLongId();
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public List<NId> getParents() {
        return parents;
    }

    @Override
    public boolean isExecutable() {
        return getFlags().contains(NDescriptorFlag.EXEC);
    }

    @Override
    public boolean isApplication() {
        return getFlags().contains(NDescriptorFlag.APP);
    }

    @Override
    public Set<NDescriptorFlag> getFlags() {
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
    public NEnvCondition getCondition() {
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
    public List<NIdLocation> getLocations() {
        return locations;
    }

    @Override
    public List<NDependency> getStandardDependencies() {
        return standardDependencies;
    }

    @Override
    public List<NDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public NArtifactCall getExecutor() {
        return executor;
    }

    @Override
    public NArtifactCall getInstaller() {
        return installer;
    }

    @Override
    public List<NDescriptorProperty> getProperties() {
        return properties;
    }

    @Override
    public NOptional<NDescriptorProperty> getProperty(String name) {
        if (properties == null) {
            return NOptional.ofNamedEmpty("property " + name);
        }
        return NOptional.ofNamed(
                properties.stream().filter(x -> x.getName().equals(name)).findFirst()
                        .orElse(null),
                "property " + name
        );
    }

    @Override
    public NOptional<NLiteral> getPropertyValue(String name) {
        return getProperty(name).map(NDescriptorProperty::getValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idType, packaging,
                executor, installer, name, description, genericName, condition, flags,
                solver, categories, properties, icons, parents, locations, dependencies, standardDependencies,
                contributors,developers,licenses
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
        DefaultNDescriptor that = (DefaultNDescriptor) o;
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
                && Objects.equals(properties, that.properties)
                && Objects.equals(contributors, that.contributors)
                && Objects.equals(developers, that.developers)
                && Objects.equals(licenses, that.licenses)
                ;
    }

    @Override
    public String toString() {
        return "NDescriptor{"
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
                + ", contributors=" + contributors
                + ", developers=" + developers
                + ", licenses=" + licenses
                + '}';
    }

    @Override
    public NDescriptorBuilder builder() {
        return new DefaultNDescriptorBuilder().setAll(this);
    }

    @Override
    public NDescriptorFormat formatter(NSession session) {
        return NDescriptorFormat.of(session).setValue(this);
    }

    @Override
    public NDescriptor readOnly() {
        return this;
    }


    @Override
    public List<NDescriptorContributor> getContributors() {
        return contributors;
    }

    @Override
    public List<NDescriptorContributor> getDevelopers() {
        return developers;
    }

    @Override
    public List<NDescriptorLicense> getLicenses() {
        return licenses;
    }

    @Override
    public List<NDescriptorMailingList> getMailingLists() {
        return mailingLists;
    }

    @Override
    public NDescriptorOrganization getOrganization() {
        return organization;
    }
}
