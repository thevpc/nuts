/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.AbstractNutsDescriptor;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;

import java.util.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

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
    private String[] arch;
    private String[] os;
    private String[] osdist;
    private String[] platform;
    private NutsIdLocation[] locations;
    private NutsClassifierMapping[] classifierMappings;
    private NutsDependency[] dependencies;
    private NutsDependency[] standardDependencies;
    private Map<String, String> properties;

    public DefaultNutsDescriptor(NutsDescriptor d) {
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
                d.getArch(),
                d.getOs(),
                d.getOsdist(),
                d.getPlatform(),
                d.getDependencies(),
                d.getStandardDependencies(),
                d.getLocations(),
                d.getProperties(),
                d.getClassifierMappings()
        );
    }

    public DefaultNutsDescriptor(NutsId id, /*String alternative, */NutsId[] parents, String packaging, boolean executable, boolean application,
                                 //                                 String ext,
                                 NutsArtifactCall executor, NutsArtifactCall installer, String name, String description,
                                 String[] arch, String[] os, String[] osdist, String[] platform,
                                 NutsDependency[] dependencies,
                                 NutsDependency[] standardDependencies,
                                 NutsIdLocation[] locations, Map<String, String> properties, NutsClassifierMapping[] classifierMappings) {
        if (id == null) {
            throw new NutsIllegalArgumentException(null, "Missing id");
        }
        if (!id.getProperties().isEmpty()) {
            throw new NutsIllegalArgumentException(null, "id should not have query defined in descriptors");
        }
        this.id = id;
//        this.alternative = CoreStringUtils.trimToNull(alternative);
        this.packaging = CoreStringUtils.trimToNull(packaging);
        this.parents = parents == null ? new NutsId[0] : new NutsId[parents.length];
        if (parents != null) {
            System.arraycopy(parents, 0, this.parents, 0, this.parents.length);
        }
        this.executable = executable;
        this.application = application;
        this.description = CoreStringUtils.trimToNull(description);
        this.name = CoreStringUtils.trimToNull(name);
        this.executor = executor;
        this.installer = installer;
//        this.ext = CoreStringUtils.trimToNull(ext);
        this.arch = CoreCommonUtils.toArraySet(arch);
        this.os = CoreCommonUtils.toArraySet(os);
        this.osdist = CoreCommonUtils.toArraySet(osdist);
        this.platform = CoreCommonUtils.toArraySet(platform);
        this.locations = CoreCommonUtils.toArraySet(locations);
        this.classifierMappings = CoreCommonUtils.toArraySet(classifierMappings);
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
        if (properties == null || properties.isEmpty()) {
            this.properties = null;
        } else {
            HashMap<String, String> p = new HashMap<>(properties);
            this.properties = Collections.unmodifiableMap(p);
        }
    }

//    @Override
//    public String getAlternative() {
//        return alternative;
//    }

    @Override
    public NutsArtifactCall getInstaller() {
        return installer;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties == null ? Collections.EMPTY_MAP : Collections.unmodifiableMap(properties);
    }

    @Override
    public NutsId[] getParents() {
        return parents;
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
    public boolean isExecutable() {
        return executable;
    }

    @Override
    public boolean isApplication() {
        return application;
    }

    @Override
    public NutsArtifactCall getExecutor() {
        return executor;
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
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDependency[] getDependencies() {
        return dependencies;
    }

    @Override
    public NutsDependency[] getStandardDependencies() {
        return standardDependencies;
    }

    @Override
    public String[] getArch() {
        return arch;
    }

    @Override
    public String[] getOs() {
        return os;
    }

    @Override
    public String[] getOsdist() {
        return osdist;
    }

    @Override
    public String[] getPlatform() {
        return platform;
    }

    @Override
    public NutsIdLocation[] getLocations() {
        return locations;
    }

    @Override
    public NutsClassifierMapping[] getClassifierMappings() {
        return classifierMappings;
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
                + ", arch=" + Arrays.toString(arch)
                + ", os=" + Arrays.toString(os)
                + ", osdist=" + Arrays.toString(osdist)
                + ", platform=" + Arrays.toString(platform)
                + ", locations=" + Arrays.toString(locations)
                + ", dependencies=" + Arrays.toString(dependencies)
                + ", standardDependencies=" + Arrays.toString(standardDependencies)
                + ", properties=" + properties
                + '}';
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
                && Objects.equals(description, that.description)
                && Arrays.equals(arch, that.arch)
                && Arrays.equals(os, that.os)
                && Arrays.equals(osdist, that.osdist)
                && Arrays.equals(platform, that.platform)
                && Arrays.equals(locations, that.locations)
                && Arrays.equals(classifierMappings, that.classifierMappings)
                && Arrays.equals(dependencies, that.dependencies)
                && Arrays.equals(standardDependencies, that.standardDependencies)
                && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id, /*alternative,*/ packaging,
                //                ext,
                executable, application, executor, installer, name, description, properties);
        result = 31 * result + Arrays.hashCode(parents);
        result = 31 * result + Arrays.hashCode(arch);
        result = 31 * result + Arrays.hashCode(os);
        result = 31 * result + Arrays.hashCode(osdist);
        result = 31 * result + Arrays.hashCode(platform);
        result = 31 * result + Arrays.hashCode(locations);
        result = 31 * result + Arrays.hashCode(classifierMappings);
        result = 31 * result + Arrays.hashCode(dependencies);
        result = 31 * result + Arrays.hashCode(standardDependencies);
        return result;
    }
}
