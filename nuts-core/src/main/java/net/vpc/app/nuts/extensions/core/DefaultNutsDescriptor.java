/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreCollectionUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDescriptor extends AbstractNutsDescriptor {

    private NutsId id;
    private String face;
    private NutsId[] parents;
    private String packaging;
    private String ext;
    private boolean executable;
    private NutsExecutorDescriptor executor;
    private NutsExecutorDescriptor installer;
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
    private NutsDependency[] dependencies;
    private Map<String, String> properties;

    public DefaultNutsDescriptor(NutsId id, String face, NutsId[] parents, String packaging, boolean executable, String ext, NutsExecutorDescriptor executor, NutsExecutorDescriptor installer, String name, String description,
                                 String[] arch, String[] os, String[] osdist, String[] platform,
                                 NutsDependency[] dependencies, Map<String, String> properties) {
        if (id == null) {
            throw new NutsIllegalArgumentsException("Missing id");
        }
        if (!id.getQueryMap().isEmpty()) {
            throw new NutsIllegalArgumentsException("id should not have query defined in descriptors");
        }
        this.id = id;
        this.face = face;
        this.packaging = CoreStringUtils.trim(packaging);
        this.parents = parents == null ? new NutsId[0] : new NutsId[parents.length];
        if (parents != null) {
            System.arraycopy(parents, 0, this.parents, 0, this.parents.length);
        }
        this.executable = executable;
        this.description = CoreStringUtils.trim(description);
        this.name = CoreStringUtils.trim(name);
        this.executor = executor;
        this.installer = installer;
        this.ext = CoreStringUtils.trim(ext);
        this.arch = CoreCollectionUtils.toArraySet(arch);
        this.os = CoreCollectionUtils.toArraySet(os);
        this.osdist = CoreCollectionUtils.toArraySet(osdist);
        this.platform = CoreCollectionUtils.toArraySet(platform);
        this.dependencies = dependencies == null ? new NutsDependency[0] : new NutsDependency[dependencies.length];
        for (int i = 0; i < this.dependencies.length; i++) {
            if (dependencies[i] == null) {
                throw new NullPointerException();
            }
            this.dependencies[i] = dependencies[i];
        }
        if (properties == null || properties.isEmpty()) {
            this.properties = null;
        } else {
            HashMap<String, String> p = new HashMap<>(properties);
            this.properties = Collections.unmodifiableMap(p);
        }
    }

    public String getFace() {
        return face;
    }

    @Override
    public NutsExecutorDescriptor getInstaller() {
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
    public NutsExecutorDescriptor getExecutor() {
        return executor;
    }

    @Override
    public String getExt() {
        return ext;
    }

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
    public String[] getArch() {
        return arch;
    }

    public String[] getOs() {
        return os;
    }

    public String[] getOsdist() {
        return osdist;
    }

    public String[] getPlatform() {
        return platform;
    }

    @Override
    public NutsDescriptor setDependencies(NutsDependency[] dependencies) {
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                dependencies,
                getProperties()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultNutsDescriptor that = (DefaultNutsDescriptor) o;

        if (executable != that.executable) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (face != null ? !face.equals(that.face) : that.face != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(parents, that.parents)) return false;
        if (packaging != null ? !packaging.equals(that.packaging) : that.packaging != null) return false;
        if (ext != null ? !ext.equals(that.ext) : that.ext != null) return false;
        if (executor != null ? !executor.equals(that.executor) : that.executor != null) return false;
        if (installer != null ? !installer.equals(that.installer) : that.installer != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(arch, that.arch)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(os, that.os)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(osdist, that.osdist)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(platform, that.platform)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(dependencies, that.dependencies)) return false;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (face != null ? face.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(parents);
        result = 31 * result + (packaging != null ? packaging.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        result = 31 * result + (executable ? 1 : 0);
        result = 31 * result + (executor != null ? executor.hashCode() : 0);
        result = 31 * result + (installer != null ? installer.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(arch);
        result = 31 * result + Arrays.hashCode(os);
        result = 31 * result + Arrays.hashCode(osdist);
        result = 31 * result + Arrays.hashCode(platform);
        result = 31 * result + Arrays.hashCode(dependencies);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    protected NutsDescriptor createInstance(NutsId id, String face, NutsId[] parents, String packaging, boolean executable, String ext, NutsExecutorDescriptor executor, NutsExecutorDescriptor installer, String name, String description, String[] arch, String[] os, String[] osdist, String[] platform, NutsDependency[] dependencies, Map<String, String> properties) {
        return new DefaultNutsDescriptor(
                id, face, parents, packaging, executable, ext, executor, installer, name, description, arch, os, osdist, platform, dependencies, properties
        );
    }
}
