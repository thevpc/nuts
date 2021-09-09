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
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreArrayUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.util.*;
import java.util.logging.Level;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDescriptor extends AbstractNutsDescriptor {

    private static final long serialVersionUID = 1L;

    private NutsId id;
    //    private String alternative;
    private NutsId[] parents;
    private String packaging;
    //    private String ext;
    private boolean executable;
    private boolean application;
    private NutsArtifactCall executor;
    private NutsArtifactCall installer;
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
    private NutsEnvCondition condition;
    private NutsIdLocation[] locations;
    private NutsDependency[] dependencies;
    private NutsDependency[] standardDependencies;
    private NutsDescriptorProperty[] properties;

    public DefaultNutsDescriptor(NutsDescriptor d, NutsSession session) {
        this(
                d.getId(),
//                d.getAlternative(),
                d.getParents(),
                d.getPackaging(),
                d.isExecutable(),
                d.isApplication(),
                //                d.getExt(),
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
                session
        );
    }

    public DefaultNutsDescriptor(NutsId id, /*String alternative, */NutsId[] parents, String packaging, boolean executable, boolean application,
                                 //                                 String ext,
                                 NutsArtifactCall executor, NutsArtifactCall installer, String name, String description,
                                 NutsEnvCondition condition,
                                 NutsDependency[] dependencies,
                                 NutsDependency[] standardDependencies,
                                 NutsIdLocation[] locations, NutsDescriptorProperty[] properties,
                                 String genericName, String[] categories, String[] icons,
                                 NutsSession session) {
        super(session);
        //id can have empty groupId (namely for executors like 'java')
        if (id == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing id"));
        }
        if (NutsUtilStrings.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing artifactId for %s", id));
        }
        //NutsWorkspaceUtils.of(session).checkSimpleNameNutsId(id);
        if (!id.getProperties().isEmpty()) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("id should not have query defined in descriptors"));
        }
        this.id = id;
//        this.alternative = NutsUtilStrings.trimToNull(alternative);
        this.packaging = NutsUtilStrings.trimToNull(packaging);
        this.parents = parents == null ? new NutsId[0] : new NutsId[parents.length];
        if (parents != null) {
            System.arraycopy(parents, 0, this.parents, 0, this.parents.length);
        }
        this.executable = executable;
        this.application = application;
        this.description = NutsUtilStrings.trimToNull(description);
        this.name = NutsUtilStrings.trimToNull(name);
        this.genericName = NutsUtilStrings.trimToNull(genericName);
        this.icons =icons==null?new String[0] :
                Arrays.stream(icons).map(x->x==null?"":x.trim()).filter(x->x.length()>0)
                        .toArray(String[]::new);
        this.categories = categories==null?new String[0] :
                Arrays.stream(categories).map(x->x==null?"":x.trim()).filter(x->x.length()>0)
                        .toArray(String[]::new);
        this.executor = executor;
        this.installer = installer;
        this.condition = CoreNutsUtils.trimToBlank(condition,session);
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
        if (properties == null || properties.length==0) {
            this.properties = null;
        } else {
            DefaultNutsProperties p = new DefaultNutsProperties();
            p.addAll(properties);
            this.properties = p.getAll();
        }
        if (this.properties != null
                && !application
        ) {
            String p = getPropertyValue("nuts.application");
            if("true".equals(p)){
                session.getWorkspace().log().of(DefaultNutsDescriptor.class)
                        .with().level(Level.FINEST)
                        .verb(NutsLogVerb.WARNING)
                        .log("{0} has nuts.application flag armed but is not an application", getId());
            }
        }
    }

//    @Override
//    public String getAlternative() {
//        return alternative;
//    }

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
        return executable;
    }

    @Override
    public boolean isApplication() {
        return application;
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
    public NutsEnvCondition getCondition() {
        return condition;
    }

    @Override
    public String getName() {
        return name;
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
    public NutsDescriptorProperty getProperty(String name) {
        if(properties==null){
            return null;
        }
        return Arrays.stream(properties).filter(x->x.getName().equals(name)).findFirst()
                .orElse(null);
    }

    @Override
    public String getPropertyValue(String name) {
        NutsDescriptorProperty p=getProperty(name);
        return p==null?null:p.getValue();
    }

    @Override
    public NutsDescriptorProperty[] getProperties() {
        return properties == null ? new NutsDescriptorProperty[0] : properties;
    }

    @Override
    public String[] getIcons() {
        return icons;
    }

    @Override
    public String[] getCategories() {
        return categories;
    }

    @Override
    public String getGenericName() {
        return genericName;
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id, /*alternative,*/ packaging,
                //                ext,
                executable, application, executor, installer, name, description,genericName,condition
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
        return executable == that.executable
                && application == that.application
                && Objects.equals(id, that.id)
//                && Objects.equals(alternative, that.alternative)
                && Arrays.equals(parents, that.parents)
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
                && Arrays.equals(properties, that.properties);
    }

    @Override
    public String toString() {
        return "DefaultNutsDescriptor{"
                + "id=" + id
//                + ", alternative='" + alternative + '\''
                + ", parents=" + Arrays.toString(parents)
                + ", packaging='" + packaging + '\''
                + //                ", ext='" + ext + '\'' +
                ", executable=" + executable
                + ", application=" + application
                + ", executor=" + executor
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
                + '}';
    }
}
