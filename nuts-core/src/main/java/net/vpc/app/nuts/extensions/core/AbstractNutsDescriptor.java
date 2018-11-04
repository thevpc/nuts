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
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.io.FileUtils;

import java.io.*;
import java.util.*;

/**
 * Created by vpc on 2/19/17.
 */
public abstract class AbstractNutsDescriptor implements NutsDescriptor {

    @Override
    public boolean matchesEnv(String arch, String os, String dist, String platform) {
        if (!matchesArch(arch)) {
            return false;
        }
        if (!matchesOs(os)) {
            return false;
        }
        if (!matchesOsdist(dist)) {
            return false;
        }
        if (!matchesPlatform(platform)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matchesPackaging(String packaging) {
        if (CoreStringUtils.isEmpty(packaging)) {
            return true;
        }
        if (CoreStringUtils.isEmpty(getPackaging())) {
            return true;
        }
        NutsId _v = CoreNutsUtils.parseNutsId(packaging);
        NutsId _v2 = CoreNutsUtils.parseNutsId(getPackaging());
        if (_v == null || _v2 == null) {
            return _v == _v2;
        }
        if (_v.isSameFullName(_v2)) {
            if (_v.getVersion().toFilter().accept(_v2.getVersion())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matchesArch(String arch) {
        if (CoreStringUtils.isEmpty(arch)) {
            return true;
        }
        NutsId _v = CoreNutsUtils.parseNutsId(arch);
        String[] all = getArch();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isEmpty(v)) {
                    return true;
                }
                NutsId y = CoreNutsUtils.parseOrErrorNutsId(v);
                if (y.isSameFullName(_v)) {
                    if (y.getVersion().toFilter().accept(_v.getVersion())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean matchesOs(String os) {
        if (CoreStringUtils.isEmpty(os)) {
            return true;
        }
        NutsId _v = CoreNutsUtils.parseNutsId(os);
        String[] all = getOs();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isEmpty(v)) {
                    return true;
                }
                NutsId y = CoreNutsUtils.parseOrErrorNutsId(v);
                if (y.isSameFullName(_v)) {
                    if (y.getVersion().toFilter().accept(_v.getVersion())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean matchesOsdist(String osdist) {
        if (CoreStringUtils.isEmpty(osdist)) {
            return true;
        }
        NutsId _v = CoreNutsUtils.parseNutsId(osdist);
        String[] all = getOsdist();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isEmpty(v)) {
                    return true;
                }
                NutsId y = CoreNutsUtils.parseOrErrorNutsId(v);
                if (y.isSameFullName(_v)) {
                    if (y.getVersion().toFilter().accept(_v.getVersion())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }

    }

    @Override
    public boolean matchesPlatform(String platform) {
        if (CoreStringUtils.isEmpty(platform)) {
            return true;
        }
        NutsId _v = CoreNutsUtils.parseNutsId(platform);
        String[] all = getPlatform();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isEmpty(v)) {
                    return true;
                }
                NutsId y = CoreNutsUtils.parseOrErrorNutsId(v);
                if (y.getFullName().equals("java")) {
                    //should accept any platform !!!
                    return true;
                }
                if (y.isSameFullName(_v)) {
                    if (y.getVersion().toFilter().accept(_v.getVersion())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getSHA1() throws NutsIOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        write(o, false);
        return CoreSecurityUtils.evalSHA1(new ByteArrayInputStream(o.toByteArray()), true);
    }

    @Override
    public void write(File file) throws NutsIOException {
        write(file, false);
    }

    //    @Override
    public void write(OutputStream os, boolean pretty) throws NutsIOException {
        OutputStreamWriter o = new OutputStreamWriter(os);
        write(o, pretty);
        try {
            o.flush();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void write(Writer out) throws NutsIOException {
        write(out, false);
    }

    @Override
    public void write(PrintStream out) throws NutsIOException {
        PrintWriter out1 = new PrintWriter(out);
        write(out1);
        out1.flush();
    }

    @Override
    public void write(OutputStream out) throws NutsIOException {
        PrintWriter out1 = new PrintWriter(out);
        write(out1);
        out1.flush();
    }

    @Override
    public void write(File file, boolean pretty) throws NutsIOException {
        FileUtils.createParents(file);
        FileWriter os = null;
        try {
            try {
                os = new FileWriter(file);
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
            write(os, pretty);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new NutsIOException(e);
                }
            }
        }
    }

    public String toString(boolean pretty) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b);
        write(w, pretty);
        try {
            w.flush();
        } catch (IOException e) {
            //
        }
        return new String(b.toByteArray());
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public void write(Writer os, boolean pretty) throws NutsIOException {
        CoreJsonUtils.get().write(this, os, true);
    }


    @Override
    public NutsDescriptor applyProperties() {
        return applyProperties(getProperties());
    }

    @Override
    public NutsDescriptor applyParents(NutsDescriptor[] parentDescriptors) {
        NutsId n_id = getId();
        String n_alt = getFace();
        String n_packaging = getPackaging();
        String n_ext = getExt();
        boolean n_executable = isExecutable();
        String n_name = getName();
        String n_desc = getDescription();
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_props.putAll(parentDescriptor.getProperties());
        }
        Map<String, String> properties = getProperties();
        if (properties != null) {
            n_props.putAll(properties);
        }
        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        LinkedHashSet<String> n_archs = new LinkedHashSet<>();
        LinkedHashSet<String> n_os = new LinkedHashSet<>();
        LinkedHashSet<String> n_osdist = new LinkedHashSet<>();
        LinkedHashSet<String> n_platform = new LinkedHashSet<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_id = applyNutsIdInheritance(n_id, parentDescriptor.getId());
            if (!n_executable && parentDescriptor.isExecutable()) {
                n_executable = true;
            }
            if (n_executor == null) {
                n_executor = parentDescriptor.getExecutor();
            }
            if (n_executor == null) {
                n_installer = parentDescriptor.getInstaller();
            }

            //packaging is not inherited!!
            //n_packaging = applyStringInheritance(n_packaging, parentDescriptor.getPackaging());
            n_ext = CoreNutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = CoreNutsUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_desc = CoreNutsUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
            n_deps.addAll(Arrays.asList(parentDescriptor.getDependencies()));
            n_archs.addAll(Arrays.asList(parentDescriptor.getArch()));
            n_os.addAll(Arrays.asList(parentDescriptor.getOs()));
            n_osdist.addAll(Arrays.asList(parentDescriptor.getOsdist()));
            n_platform.addAll(Arrays.asList(parentDescriptor.getPlatform()));
        }
        n_deps.addAll(Arrays.asList(getDependencies()));
        n_archs.addAll(Arrays.asList(getArch()));
        n_os.addAll(Arrays.asList(getOs()));
        n_osdist.addAll(Arrays.asList(getOsdist()));
        n_platform.addAll(Arrays.asList(getPlatform()));
        NutsId[] n_parents = new NutsId[0];
        if (n_packaging.isEmpty() && n_ext.isEmpty()) {
            n_packaging = "jar";
            n_ext = "jar";
        } else if (n_packaging.isEmpty()) {
            n_packaging = n_ext;
        } else {
            n_ext = n_packaging;
        }
        return new DefaultNutsDescriptorBuilder()
                .setId(n_id)
                .setFace(n_alt)
                .setParents(n_parents)
                .setPackaging(n_packaging)
                .setExecutable(n_executable)
                .setExt(n_ext)
                .setExecutor(n_executor)
                .setInstaller(n_installer)
                .setName(n_name)
                .setDescription(n_desc)
                .setArch(n_archs.toArray(new String[n_archs.size()]))
                .setOs(n_os.toArray(new String[n_os.size()]))
                .setOsdist(n_osdist.toArray(new String[n_osdist.size()]))
                .setPlatform(n_platform.toArray(new String[n_platform.size()]))
                .setDependencies(n_deps.toArray(new NutsDependency[n_deps.size()]))
                .setProperties(n_props)
                .build()
                ;
    }

    @Override
    public NutsDescriptor applyProperties(Map<String, String> properties) {
        MapStringMapper map = new MapStringMapper(properties);

        NutsId n_id = getId().apply(map);
        String n_alt = CoreNutsUtils.applyStringProperties(getFace(), map);
        String n_packaging = CoreNutsUtils.applyStringProperties(getPackaging(), map);
        String n_ext = CoreNutsUtils.applyStringProperties(getExt(), map);
        String n_name = CoreNutsUtils.applyStringProperties(getName(), map);
        String n_desc = CoreNutsUtils.applyStringProperties(getDescription(), map);
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        Map<String, String> properties1 = getProperties();
        if (properties1 != null) {
            for (Map.Entry<String, String> ee : properties1.entrySet()) {
                n_props.put(CoreNutsUtils.applyStringProperties(ee.getKey(), map), CoreNutsUtils.applyStringProperties(ee.getValue(), map));
            }
        }

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        for (NutsDependency d2 : getDependencies()) {
            n_deps.add(applyNutsDependencyProperties(d2, map));
        }

        return new DefaultNutsDescriptorBuilder()
                .setId(n_id)
                .setFace(n_alt)
                .setParents(getParents())
                .setPackaging(n_packaging)
                .setExecutable(isExecutable())
                .setExt(n_ext)
                .setExecutor(n_executor)
                .setInstaller(n_installer)
                .setName(n_name)
                .setDescription(n_desc)
                .setArch(CoreNutsUtils.applyStringProperties(getArch(), map))
                .setOs(CoreNutsUtils.applyStringProperties(getOs(), map))
                .setOsdist(CoreNutsUtils.applyStringProperties(getOsdist(), map))
                .setPlatform(CoreNutsUtils.applyStringProperties(getPlatform(), map))
                .setDependencies(n_deps.toArray(new NutsDependency[n_deps.size()]))
                .setProperties(n_props)
                .build()
                ;
    }

    private NutsId applyNutsIdProperties(NutsId child, StringMapper properties) {
        return new NutsIdImpl(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                CoreNutsUtils.applyMapProperties(child.getQueryMap(), properties)
        );
    }

    private NutsDependency applyNutsDependencyProperties(NutsDependency child, StringMapper properties) {
        NutsId[] exclusions = child.getExclusions();
        for (int i = 0; i < exclusions.length; i++) {
            exclusions[i] = applyNutsIdProperties(exclusions[i], properties);
        }
        return new NutsDependencyImpl(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                CoreNutsUtils.applyStringProperties(child.getScope(), properties),
                CoreNutsUtils.applyStringProperties(child.getOptional(), properties),
                exclusions
        );
    }

    private NutsId applyNutsIdInheritance(NutsId child, NutsId parent) {
        if (parent != null) {
            boolean modified = false;
            String namespace = child.getNamespace();
            String group = child.getGroup();
            String name = child.getName();
            String version = child.getVersion().getValue();
            Map<String, String> face = child.getQueryMap();
            if (CoreStringUtils.isEmpty(namespace)) {
                modified = true;
                namespace = parent.getNamespace();
            }
            if (CoreStringUtils.isEmpty(group)) {
                modified = true;
                group = parent.getGroup();
            }
            if (CoreStringUtils.isEmpty(name)) {
                modified = true;
                name = parent.getName();
            }
            if (CoreStringUtils.isEmpty(version)) {
                modified = true;
                version = parent.getVersion().getValue();
            }
            Map<String, String> parentFaceMap = parent.getQueryMap();
            if (!parentFaceMap.isEmpty()) {
                modified = true;
                face.putAll(parentFaceMap);
            }
            if (modified) {
                return new NutsIdImpl(
                        namespace,
                        group,
                        name,
                        version,
                        face
                );
            }
        }
        return child;
    }

    @Override
    public NutsDescriptor addDependency(NutsDependency dependency) {
        if (dependency == null) {
            return this;
        }
        ArrayList<NutsDependency> dependencies = new ArrayList<>(Arrays.asList(getDependencies()));
        dependencies.add(dependency);
        return setDependencies(dependencies.toArray(new NutsDependency[dependencies.size()]));
    }

    @Override
    public NutsDescriptor removeDependency(NutsDependency dependency) {
        if (dependency == null) {
            return this;
        }
        NutsDependency[] dependencies = getDependencies();
        ArrayList<NutsDependency> dependenciesList = new ArrayList<>();
        for (NutsDependency d : dependencies) {
            if (d.getFullName().equals(dependency.getFullName())
                    && Objects.equals(d.getScope(), dependency.getScope())) {
                //do not add
            } else {
                dependenciesList.add(d);
            }
        }
        return setDependencies(dependenciesList.toArray(new NutsDependency[dependenciesList.size()]));
    }

    @Override
    public NutsDescriptor addDependencies(NutsDependency[] dependencies) {
        if (dependencies == null || dependencies.length == 0) {
            return this;
        }
        ArrayList<NutsDependency> dependenciesList = new ArrayList<>(Arrays.asList(getDependencies()));
        dependenciesList.addAll(Arrays.asList(dependencies));
        return setDependencies(dependenciesList.toArray(new NutsDependency[dependenciesList.size()]));
    }

    @Override
    public NutsDescriptor setDependencies(NutsDependency[] dependencies) {
        return builder().setDependencies(dependencies).build();
    }


    @Override
    public NutsDescriptor setLocations(String[] locations) {
        return builder().setLocations(locations).build();
    }


    @Override
    public NutsDescriptor addOs(String os) {
        return builder().addOs(os).build();
    }

    @Override
    public NutsDescriptor removeOs(String os) {
        return builder().removeOs(os).build();
    }

    @Override
    public NutsDescriptor addOsdist(String osdist) {
        return builder().addOsdist(osdist).build();
    }

    @Override
    public NutsDescriptor removeOsdist(String os) {
        return builder().removeOsdist(os).build();
    }

    @Override
    public NutsDescriptor addPlatform(String platform) {
        return builder().addPlatform(platform).build();
    }

    @Override
    public NutsDescriptor removePlatform(String platform) {
        return builder().removePlatform(platform).build();
    }

    @Override
    public NutsDescriptor addArch(String arch) {
        return builder().addArch(arch).build();
    }

    @Override
    public NutsDescriptor removeArch(String arch) {
        return builder().removeArch(arch).build();
    }

    @Override
    public NutsDescriptor addProperty(String name, String value) {
        return builder().addProperty(name,value).build();
    }

    @Override
    public NutsDescriptor removeProperty(String name) {
        return builder().removeProperty(name).build();
    }

    @Override
    public NutsDescriptor setExt(String ext) {
        if (CoreStringUtils.trim(ext).equals(getExt())) {
            return this;
        }
        return builder().setExt(ext).build();
    }

    @Override
    public NutsDescriptor setPackaging(String packaging) {
        if (CoreStringUtils.trim(packaging).equals(getPackaging())) {
            return this;
        }
        return builder().setPackaging(packaging).build();
    }

    @Override
    public NutsDescriptor setExecutable(boolean executable) {
        if (executable == isExecutable()) {
            return this;
        }
        return builder().setExecutable(executable).build();
    }

    @Override
    public NutsDescriptor setExecutor(NutsExecutorDescriptor executor) {
        if (Objects.equals(executor, getExecutor())) {
            return this;
        }
        return builder().setExecutor(executor).build();
    }

    @Override
    public NutsDescriptor setId(NutsId id) {
        if (Objects.equals(id, getId())) {
            return this;
        }
        return builder().setId(id).build();
    }

    @Override
    public NutsDescriptor setProperties(Map<String, String> map, boolean append) {
        Map<String, String> l_properties = new HashMap<>();
        if (append) {
            l_properties.putAll(getProperties());
        }
        if (map != null) {
            l_properties.putAll(map);
        }
        if (Objects.equals(l_properties, getProperties())) {
            return this;
        }
        return builder().setProperties(map).build();
    }

    @Override
    public NutsDescriptorBuilder builder() {
        return new DefaultNutsDescriptorBuilder(this);
    }

}
