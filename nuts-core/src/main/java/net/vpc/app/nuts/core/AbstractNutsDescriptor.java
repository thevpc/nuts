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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils.MapToFunction;

/**
 * Created by vpc on 2/19/17.
 */
public abstract class AbstractNutsDescriptor implements NutsDescriptor {


    
    @Override
    public NutsDescriptor applyProperties() {
        return applyProperties(getProperties());
    }

    @Override
    public NutsDescriptor applyParents(NutsDescriptor[] parentDescriptors) {
        if (parentDescriptors.length == 0) {
            return this;
        }
        NutsId n_id = getId();
        String n_alt = getAlternative();
        String n_packaging = getPackaging();
//        String n_ext = getExt();
        boolean n_executable = isExecutable();
        boolean n_app = isNutsApplication();
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
        LinkedHashSet<NutsDependency> n_sdeps = new LinkedHashSet<>();
        LinkedHashSet<String> n_archs = new LinkedHashSet<>();
        LinkedHashSet<String> n_os = new LinkedHashSet<>();
        LinkedHashSet<String> n_osdist = new LinkedHashSet<>();
        LinkedHashSet<String> n_platform = new LinkedHashSet<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_id = CoreNutsUtils.applyNutsIdInheritance(n_id, parentDescriptor.getId());
            if (!n_executable && parentDescriptor.isExecutable()) {
                n_executable = true;
            }
            if (!n_app && parentDescriptor.isNutsApplication()) {
                n_app = true;
            }
            if (n_executor == null) {
                n_executor = parentDescriptor.getExecutor();
            }
            if (n_executor == null) {
                n_installer = parentDescriptor.getInstaller();
            }

            //packaging is not inherited!!
            //n_packaging = applyStringInheritance(n_packaging, parentDescriptor.getPackaging());
//            n_ext = CoreNutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = CoreNutsUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_desc = CoreNutsUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
            n_deps.addAll(Arrays.asList(parentDescriptor.getDependencies()));
            n_sdeps.addAll(Arrays.asList(parentDescriptor.getStandardDependencies()));
            n_archs.addAll(Arrays.asList(parentDescriptor.getArch()));
            n_os.addAll(Arrays.asList(parentDescriptor.getOs()));
            n_osdist.addAll(Arrays.asList(parentDescriptor.getOsdist()));
            n_platform.addAll(Arrays.asList(parentDescriptor.getPlatform()));
        }
        n_deps.addAll(Arrays.asList(getDependencies()));
        n_sdeps.addAll(Arrays.asList(getStandardDependencies()));
        n_archs.addAll(Arrays.asList(getArch()));
        n_os.addAll(Arrays.asList(getOs()));
        n_osdist.addAll(Arrays.asList(getOsdist()));
        n_platform.addAll(Arrays.asList(getPlatform()));
        NutsId[] n_parents = new NutsId[0];
//        if (n_packaging.isEmpty() && n_ext.isEmpty()) {
//            n_packaging = "jar";
//            n_ext = "jar";
//        } else if (n_packaging.isEmpty()) {
//            n_packaging = n_ext;
//        } else {
//            n_ext = n_packaging;
//        }
        return new DefaultNutsDescriptorBuilder()
                .setId(n_id)
                .setAlternative(n_alt)
                .setParents(n_parents)
                .setPackaging(n_packaging)
                .setExecutable(n_executable)
                .setNutsApplication(n_app)
                //                .setExt(n_ext)
                .setExecutor(n_executor)
                .setInstaller(n_installer)
                .setName(n_name)
                .setDescription(n_desc)
                .setArch(n_archs.toArray(new String[0]))
                .setOs(n_os.toArray(new String[0]))
                .setOsdist(n_osdist.toArray(new String[0]))
                .setPlatform(n_platform.toArray(new String[0]))
                .setDependencies(n_deps.toArray(new NutsDependency[0]))
                .setStandardDependencies(n_sdeps.toArray(new NutsDependency[0]))
                .setProperties(n_props)
                .build();
    }

    @Override
    public NutsDescriptor applyProperties(Map<String, String> properties) {
        Function<String, String> map = new MapToFunction<String,String>(properties);

        NutsId n_id = getId().apply(map);
        String n_alt = CoreNutsUtils.applyStringProperties(getAlternative(), map);
        String n_packaging = CoreNutsUtils.applyStringProperties(getPackaging(), map);
//        String n_ext = CoreNutsUtils.applyStringProperties(getExt(), map);
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

        LinkedHashSet<NutsDependency> n_sdeps = new LinkedHashSet<>();
        for (NutsDependency d2 : getStandardDependencies()) {
            n_sdeps.add(applyNutsDependencyProperties(d2, map));
        }

        return new DefaultNutsDescriptorBuilder()
                .setId(n_id)
                .setAlternative(n_alt)
                .setParents(getParents())
                .setPackaging(n_packaging)
                .setExecutable(isExecutable())
                .setNutsApplication(isNutsApplication())
                //                .setExt(n_ext)
                .setExecutor(n_executor)
                .setInstaller(n_installer)
                .setName(n_name)
                .setDescription(n_desc)
                .setArch(CoreNutsUtils.applyStringProperties(getArch(), map))
                .setOs(CoreNutsUtils.applyStringProperties(getOs(), map))
                .setOsdist(CoreNutsUtils.applyStringProperties(getOsdist(), map))
                .setPlatform(CoreNutsUtils.applyStringProperties(getPlatform(), map))
                .setDependencies(n_deps.toArray(new NutsDependency[0]))
                .setStandardDependencies(n_sdeps.toArray(new NutsDependency[0]))
                .setProperties(n_props)
                .build();
    }

    private NutsId applyNutsIdProperties(NutsId child, Function<String,String> properties) {
        return new DefaultNutsId(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                CoreNutsUtils.applyMapProperties(child.getQueryMap(), properties)
        );
    }

    private NutsDependency applyNutsDependencyProperties(NutsDependency child, Function<String,String> properties) {
        NutsId[] exclusions = child.getExclusions();
        for (int i = 0; i < exclusions.length; i++) {
            exclusions[i] = applyNutsIdProperties(exclusions[i], properties);
        }
        return new DefaultNutsDependency(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getClassifier(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion(), properties),
                CoreNutsUtils.applyStringProperties(child.getScope(), properties),
                CoreNutsUtils.applyStringProperties(child.getOptional(), properties),
                exclusions
        );
    }

    @Override
    public NutsDescriptor addDependency(NutsDependency dependency) {
        if (dependency == null) {
            return this;
        }
        return builder().addDependency(dependency).build();
    }

    @Override
    public NutsDescriptor removeDependency(NutsDependency dependency) {
        if (dependency == null) {
            return this;
        }
        return builder().removeDependency(dependency).build();
    }

    @Override
    public NutsDescriptor replaceDependency(Predicate<NutsDependency> filter, UnaryOperator<NutsDependency> converter) {
        if (converter == null) {
            return this;
        }
        return builder().replaceDependency(filter, converter).build();
    }

    @Override
    public NutsDescriptor removeDependency(Predicate<NutsDependency> dependency) {
        if (dependency == null) {
            return this;
        }
        return builder().removeDependency(dependency).build();
    }

    @Override
    public NutsDescriptor addDependencies(NutsDependency[] dependencies) {
        if (dependencies == null || dependencies.length == 0) {
            return this;
        }
        ArrayList<NutsDependency> dependenciesList = new ArrayList<>(Arrays.asList(getDependencies()));
        dependenciesList.addAll(Arrays.asList(dependencies));
        return setDependencies(dependenciesList.toArray(new NutsDependency[0]));
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
        return builder().addProperty(name, value).build();
    }

    @Override
    public NutsDescriptor removeProperty(String name) {
        return builder().removeProperty(name).build();
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
    public NutsDescriptor setAlternative(String alternative) {
        if (CoreStringUtils.trim(alternative).equals(getAlternative())) {
            return this;
        }
        return builder().setAlternative(alternative).build();
    }

    @Override
    public NutsDescriptor setNutsApplication(boolean nutsApp) {
        if (nutsApp == isNutsApplication()) {
            return this;
        }
        return builder().setNutsApplication(nutsApp).build();
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
        return builder().setProperties(l_properties).build();
    }

    @Override
    public NutsDescriptorBuilder builder() {
        return new DefaultNutsDescriptorBuilder(this);
    }

    @Override
    public NutsDescriptor replaceProperty(Predicate<Map.Entry<String, String>> filter, Function<Map.Entry<String, String>, String> converter) {
        return builder().replaceProperty(filter, converter).build();
    }

}
