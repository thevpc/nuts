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
package net.vpc.app.nuts;

import net.vpc.app.nuts.util.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.util.*;

/**
 * Created by vpc on 2/19/17.
 */
public abstract class AbstractNutsDescriptor implements NutsDescriptor {

    public boolean matchesEnv(String arch, String os, String dist, String platform) {
        NutsId _arch = NutsId.parseNullableOrError(arch);
        NutsId _os = NutsId.parseNullableOrError(os);
        NutsId _dist = NutsId.parseNullableOrError(dist);
        NutsId _platform = NutsId.parseNullableOrError(platform);
        boolean ok = false;
        if(_arch!=null && getArch().length>0) {
            ok = false;
            for (String x : getArch()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_arch)) {
                    if (y.getVersion().toFilter().accept(_arch.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            if(!ok){
                return false;
            }
        }
        if(_os!=null && getOs().length>0) {
            ok = false;
            for (String x : getOs()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_os)) {
                    if (y.getVersion().toFilter().accept(_os.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            if(!ok){
                return false;
            }
        }

        if(_dist!=null && getOsdist().length>0) {
            ok = false;
            for (String x : getOsdist()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_dist)) {
                    if (y.getVersion().toFilter().accept(_dist.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            if(!ok){
                return false;
            }
        }

        if(_platform!=null && getPlatform().length>0) {
            ok = false;
            for (String x : getPlatform()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_platform)) {
                    if (y.getVersion().toFilter().accept(_platform.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            if(!ok){
                return false;
            }
        }
        return true;
    }

    @Override
    public String getSHA1() throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        write(o, false);
        return SecurityUtils.evalSHA1(new ByteArrayInputStream(o.toByteArray()));
    }

    @Override
    public void write(File file) throws IOException {
        write(file, false);
    }

    @Override
    public void write(OutputStream file) throws IOException {
        write(file, false);
    }

    @Override
    public void write(File file, boolean pretty) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            write(os, pretty);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    @Override
    public String toString() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            write(b, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(b.toByteArray());
    }

    @Override
    public void write(OutputStream os, boolean pretty) throws IOException {
        NutsDescriptor desc = this;
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("nuts-version", "1.0");
        objectBuilder.add("id", desc.getId().toString());
        objectBuilder.add("face", StringUtils.isEmpty(desc.getFace())?NutsConstants.QUERY_FACE_DEFAULT_VALUE :desc.getFace());
        objectBuilder.add("executable", desc.isExecutable());
        if (desc.getParents().length > 0) {
            JsonArrayBuilder p = Json.createArrayBuilder();
            for (NutsId nutsId : getParents()) {
                p.add(nutsId.toString());
            }
            objectBuilder.add("parents", p);
        }
        if (StringUtils.isEmpty(desc.getPackaging())) {
            //objectBuilder.add("packaging", JsonValue.NULL);
        } else {
            objectBuilder.add("packaging", desc.getPackaging());
        }
        if (StringUtils.isEmpty(desc.getExt())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("ext", desc.getExt());
        }
        if (StringUtils.isEmpty(desc.getName())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("name", desc.getName());
        }

        if (StringUtils.isEmpty(desc.getDescription())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("description", desc.getDescription());
        }
        Map<String, String> properties = getProperties();
        if (properties != null && !properties.isEmpty()) {
            objectBuilder.add("properties", JsonUtils.serializeStringsMap(desc.getProperties(), JsonUtils.IGNORE_EMPTY_OPTIONS));
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
        JsonUtils.storeJson(objectBuilder.build(), new OutputStreamWriter(os), pretty);
    }

    private JsonObjectBuilder nutsExecutorDescriptorToJsonObjectBuilder(NutsExecutorDescriptor e) {
        JsonObjectBuilder objectBuilder2 = Json.createObjectBuilder();
        if (e != null) {
            if (e.getId() != null) {
                objectBuilder2.add("id", e.getId().toString());
            }
            if (e.getArgs() != null && e.getArgs().length > 0) {
                objectBuilder2.add("args", JsonUtils.serializeArr(e.getArgs(), JsonUtils.IGNORE_EMPTY_OPTIONS));
            }
            if (e.getProperties() != null && !e.getProperties().isEmpty()) {
                objectBuilder2.add("properties", JsonUtils.serializeObj(e.getProperties(), JsonUtils.IGNORE_EMPTY_OPTIONS));
            }
        }
        return objectBuilder2;
    }

    @Override
    public NutsDescriptor applyProperties() throws IOException {
        return applyProperties(getProperties());
    }

    @Override
    public NutsDescriptor applyParents(NutsDescriptor[] parentDescriptors) throws IOException {
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
            n_ext = applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = applyStringInheritance(n_name, parentDescriptor.getName());
            n_desc = applyStringInheritance(n_desc, parentDescriptor.getDescription());
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
        return new DefaultNutsDescriptor(
                n_id, n_alt,n_parents, n_packaging, n_executable, n_ext, n_executor, n_installer, n_name, n_desc,
                n_archs.toArray(new String[n_archs.size()]),
                n_os.toArray(new String[n_os.size()]),
                n_osdist.toArray(new String[n_osdist.size()]),
                n_platform.toArray(new String[n_platform.size()]),
                n_deps.toArray(new NutsDependency[n_deps.size()]),
                n_props
        );
    }

    @Override
    public NutsDescriptor applyProperties(Map<String, String> properties) throws IOException {
        MapStringMapper map = new MapStringMapper(properties);

        NutsId n_id = applyNutsIdProperties(getId(), map);
        String n_alt = applyStringProperties(getFace(), map);
        String n_packaging = applyStringProperties(getPackaging(), map);
        String n_ext = applyStringProperties(getExt(), map);
        String n_name = applyStringProperties(getName(), map);
        String n_desc = applyStringProperties(getDescription(), map);
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        Map<String, String> properties1 = getProperties();
        if (properties1 != null) {
            for (Map.Entry<String, String> ee : properties1.entrySet()) {
                n_props.put(applyStringProperties(ee.getKey(), map), applyStringProperties(ee.getValue(), map));
            }
        }

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        for (NutsDependency d2 : getDependencies()) {
            n_deps.add(applyNutsDependencyProperties(d2, map));
        }
        return new DefaultNutsDescriptor(
                n_id, n_alt,getParents(), n_packaging, isExecutable(), n_ext, n_executor, n_installer, n_name, n_desc,
                applyStringProperties(getArch(), map),
                applyStringProperties(getOs(), map),
                applyStringProperties(getOsdist(), map),
                applyStringProperties(getPlatform(), map),
                n_deps.toArray(new NutsDependency[n_deps.size()]),
                n_props
        );
    }

    private NutsId applyNutsIdProperties(NutsId child, StringMapper properties) {
        return new NutsId(
                applyStringProperties(child.getNamespace(), properties),
                applyStringProperties(child.getGroup(), properties),
                applyStringProperties(child.getName(), properties),
                applyStringProperties(child.getVersion().getValue(), properties),
                applyMapProperties(child.getQueryMap(), properties)
        );
    }

    private NutsDependency applyNutsDependencyProperties(NutsDependency child, StringMapper properties) {
        return new NutsDependency(
                applyStringProperties(child.getNamespace(), properties),
                applyStringProperties(child.getGroup(), properties),
                applyStringProperties(child.getName(), properties),
                applyStringProperties(child.getVersion().getValue(), properties),
                applyStringProperties(child.getScope(), properties),
                applyStringProperties(child.getOptional(), properties)
        );
    }

    private String[] applyStringProperties(String[] child, StringMapper properties) {
        String[] vals = new String[child.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = applyStringProperties(child[i], properties);
        }
        return vals;
    }

    private Map<String,String> applyMapProperties(Map<String,String> child, StringMapper properties) {
        Map<String,String> m2=new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : child.entrySet()) {
            m2.put(applyStringProperties(entry.getKey(),properties),applyStringProperties(entry.getValue(),properties));
        }
        return m2;
    }

    private String applyStringProperties(String child, StringMapper properties) {
        if (StringUtils.isEmpty(child)) {
            return null;
        }
        return StringUtils.replaceVars(child, properties);
    }

    private String applyStringInheritance(String child, String parent) {
        child = StringUtils.trimToNull(child);
        parent = StringUtils.trimToNull(parent);
        if (child == null) {
            return parent;
        }
        return child;
    }

    private NutsId applyNutsIdInheritance(NutsId child, NutsId parent) {
        if (parent != null) {
            boolean modified = false;
            String namespace = child.getNamespace();
            String group = child.getGroup();
            String name = child.getName();
            String version = child.getVersion().getValue();
            Map<String,String> face = child.getQueryMap();
            if (StringUtils.isEmpty(namespace)) {
                modified = true;
                namespace = parent.getNamespace();
            }
            if (StringUtils.isEmpty(group)) {
                modified = true;
                group = parent.getGroup();
            }
            if (StringUtils.isEmpty(name)) {
                modified = true;
                name = parent.getName();
            }
            if (StringUtils.isEmpty(version)) {
                modified = true;
                version = parent.getVersion().getValue();
            }
            Map<String, String> parentFaceMap = parent.getQueryMap();
            if (!parentFaceMap.isEmpty()) {
                modified = true;
                face.putAll(parentFaceMap);
            }
            if (modified) {
                return new NutsId(
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

}
