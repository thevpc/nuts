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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
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

    @Override
    public void write(OutputStream file) throws NutsIOException {
        write(file, false);
    }

    @Override
    public void write(File file, boolean pretty) throws NutsIOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        FileOutputStream os = null;
        try {
            try {
                os = new FileOutputStream(file);
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
        write(b, pretty);
        return new String(b.toByteArray());
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public void write(OutputStream os, boolean pretty) throws NutsIOException {
        NutsDescriptor desc = this;
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("nuts-version", "1.0");
        objectBuilder.add("id", desc.getId().toString());
        objectBuilder.add("face", CoreStringUtils.isEmpty(desc.getFace()) ? NutsConstants.QUERY_FACE_DEFAULT_VALUE : desc.getFace());
        objectBuilder.add("executable", desc.isExecutable());
        if (desc.getParents().length > 0) {
            JsonArrayBuilder p = Json.createArrayBuilder();
            for (NutsId nutsId : getParents()) {
                p.add(nutsId.toString());
            }
            objectBuilder.add("parents", p);
        }
        if (CoreStringUtils.isEmpty(desc.getPackaging())) {
            //objectBuilder.add("packaging", JsonValue.NULL);
        } else {
            objectBuilder.add("packaging", desc.getPackaging());
        }
        if (CoreStringUtils.isEmpty(desc.getExt())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("ext", desc.getExt());
        }
        if (CoreStringUtils.isEmpty(desc.getName())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("name", desc.getName());
        }

        if (CoreStringUtils.isEmpty(desc.getDescription())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("description", desc.getDescription());
        }
        Map<String, String> properties = getProperties();
        if (properties != null && !properties.isEmpty()) {
            objectBuilder.add("properties", CoreJsonUtils.get().serializeStringsMap(desc.getProperties(), CoreJsonUtils.IGNORE_EMPTY_OPTIONS));
        }
        if (desc.getExecutor() != null) {
            JsonObjectBuilder objectBuilder2 = nutsExecutorDescriptorToJsonObjectBuilder(desc.getExecutor());
            if (objectBuilder2 != null) {
                objectBuilder.add("executor", objectBuilder2);
            }
        }
        if (desc.getInstaller() != null) {
            JsonObjectBuilder objectBuilder2 = nutsExecutorDescriptorToJsonObjectBuilder(desc.getInstaller());
            if (objectBuilder2 != null) {
                objectBuilder.add("installer", objectBuilder2);
            }
        }

        String[] architectures = desc.getArch();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("arch", arch);
        }

        architectures = desc.getOs();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("os", arch);
        }

        architectures = desc.getOsdist();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("osdist", arch);
        }

        architectures = desc.getPlatform();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("platform", arch);
        }

        JsonArrayBuilder dep = Json.createArrayBuilder();
        NutsDependency[] dependencies = desc.getDependencies();
        if (dependencies != null && dependencies.length > 0) {
            for (NutsDependency nutsDependency : dependencies) {
                dep.add(nutsDependency.toString());
            }
            objectBuilder.add("dependencies", dep);
        }
        CoreJsonUtils.get().storeJson(objectBuilder.build(), new OutputStreamWriter(os), pretty);
    }

    private JsonObjectBuilder nutsExecutorDescriptorToJsonObjectBuilder(NutsExecutorDescriptor e) {
        JsonObjectBuilder objectBuilder2 = Json.createObjectBuilder();
        if (e != null) {
            if (e.getId() != null) {
                objectBuilder2.add("id", e.getId().toString());
            }
            if (e.getArgs() != null && e.getArgs().length > 0) {
                objectBuilder2.add("args", CoreJsonUtils.get().serializeArr(e.getArgs(), CoreJsonUtils.IGNORE_EMPTY_OPTIONS));
            }
            if (e.getProperties() != null && !e.getProperties().isEmpty()) {
                objectBuilder2.add("properties", CoreJsonUtils.get().serializeObj(e.getProperties(), CoreJsonUtils.IGNORE_EMPTY_OPTIONS));
            }
        }
        return objectBuilder2;
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
        return createInstance(
                n_id, n_alt, n_parents, n_packaging, n_executable, n_ext, n_executor, n_installer, n_name, n_desc,
                n_archs.toArray(new String[n_archs.size()]),
                n_os.toArray(new String[n_os.size()]),
                n_osdist.toArray(new String[n_osdist.size()]),
                n_platform.toArray(new String[n_platform.size()]),
                n_deps.toArray(new NutsDependency[n_deps.size()]),
                n_props
        );
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
        return createInstance(
                n_id, n_alt, getParents(), n_packaging, isExecutable(), n_ext, n_executor, n_installer, n_name, n_desc,
                CoreNutsUtils.applyStringProperties(getArch(), map),
                CoreNutsUtils.applyStringProperties(getOs(), map),
                CoreNutsUtils.applyStringProperties(getOsdist(), map),
                CoreNutsUtils.applyStringProperties(getPlatform(), map),
                n_deps.toArray(new NutsDependency[n_deps.size()]),
                n_props
        );
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
    public NutsDescriptor addOs(String os) {
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
                CoreCollectionUtils.toArraySet(getOs(), new String[]{os}),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removeOs(String os) {
        Set<String> vals = CoreCollectionUtils.toSet(getOs());
        vals.remove(os);
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
                vals.toArray(new String[vals.size()]),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addOsdist(String osdist) {
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
                CoreCollectionUtils.toArraySet(getOsdist(), new String[]{osdist}),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removeOsdist(String os) {
        Set<String> vals = CoreCollectionUtils.toSet(getOs());
        vals.remove(os);
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
                vals.toArray(new String[vals.size()]),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addPlatform(String platform) {
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
                CoreCollectionUtils.toArraySet(getPlatform(), new String[]{platform}),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removePlatform(String os) {
        Set<String> vals = CoreCollectionUtils.toSet(getPlatform());
        vals.remove(os);
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
                vals.toArray(new String[vals.size()]),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addArch(String arch) {
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
                CoreCollectionUtils.toArraySet(getArch(), new String[]{arch}),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removeArch(String arch) {
        Set<String> vals = CoreCollectionUtils.toSet(getArch());
        vals.remove(arch);
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
                vals.toArray(new String[vals.size()]),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addProperty(String name, String value) {
        Map<String, String> properties = new HashMap<>(getProperties());
        properties.put(name, value);
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
                getDependencies(),
                properties
        );
    }

    @Override
    public NutsDescriptor removeProperty(String name) {
        Map<String, String> properties = new HashMap<>(getProperties());
        properties.remove(name);
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
                getDependencies(),
                properties
        );
    }

    @Override
    public NutsDescriptor setExt(String ext) {
        if (CoreStringUtils.trim(ext).equals(getExt())) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                ext,
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setPackaging(String packaging) {
        if (CoreStringUtils.trim(packaging).equals(getPackaging())) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                packaging,
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
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setExecutable(boolean executable) {
        if (executable == isExecutable()) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                executable,
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setExecutor(NutsExecutorDescriptor executor) {
        if (Objects.equals(executor, getExecutor())) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                executor,
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setId(NutsId id) {
        if (Objects.equals(id, getId())) {
            return this;
        }
        return createInstance(
                id,
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
                getDependencies(),
                getProperties()
        );
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
                getDependencies(),
                l_properties
        );
    }

    protected NutsDescriptor createInstance(NutsId id, String face, NutsId[] parents, String packaging, boolean executable, String ext, NutsExecutorDescriptor executor, NutsExecutorDescriptor installer, String name, String description,
            String[] arch, String[] os, String[] osdist, String[] platform,
            NutsDependency[] dependencies, Map<String, String> properties) {
        throw new NutsIllegalArgumentsException("Unmodifiable instance");
    }
}
