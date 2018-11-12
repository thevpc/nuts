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

import java.io.Serializable;
import java.util.Map;

/**
 * Created by vpc on 2/19/17.
 */
public interface NutsDescriptorBuilder extends Serializable {

    NutsId getId();

    NutsId[] getParents();

    boolean isExecutable();

    String getPackaging();

    String getExt();

    String getFace();

    String[] getArch();

    String[] getOs();

    String[] getOsdist();

    String[] getPlatform();

    String getName();

    String[] getLocations();

    NutsDescriptorBuilder addLocation(String locations);

    NutsDescriptorBuilder setLocations(String[] locations);

    NutsDependency[] getDependencies();

    NutsExecutorDescriptor getExecutor();

    NutsExecutorDescriptor getInstaller();

    Map<String, String> getProperties();

    String getDescription();

    NutsDescriptorBuilder setInstaller(NutsExecutorDescriptor installer);

    NutsDescriptorBuilder setFace(String face);

    NutsDescriptorBuilder setDescription(String description);

    NutsDescriptorBuilder setExecutable(boolean executable);

    NutsDescriptorBuilder setExecutor(NutsExecutorDescriptor executor);


    NutsDescriptorBuilder setExt(String ext);

    NutsDescriptorBuilder addProperty(String name, String value);

    NutsDescriptorBuilder removeProperty(String name);

    NutsDescriptorBuilder addOs(String os);

    NutsDescriptorBuilder addOsdist(String osdist);

    NutsDescriptorBuilder addArch(String arch);

    NutsDescriptorBuilder addPlatform(String platform);

    NutsDescriptorBuilder removeOs(String os);

    NutsDescriptorBuilder removeOsdist(String osdist);

    NutsDescriptorBuilder removeArch(String arch);

    NutsDescriptorBuilder removePlatform(String platform);

    NutsDescriptorBuilder setPackaging(String packaging);

    NutsDescriptorBuilder setId(NutsId id);

    NutsDescriptorBuilder set(NutsDescriptorBuilder other);

    NutsDescriptorBuilder set(NutsDescriptor other);

    NutsDescriptorBuilder setId(String id);

    NutsDescriptorBuilder removeDependency(NutsDependency dependency);

    NutsDescriptorBuilder addDependency(NutsDependency dependency);

    NutsDescriptorBuilder addDependencies(NutsDependency[] dependencies);

    NutsDescriptorBuilder setDependencies(NutsDependency[] dependencies);

    NutsDescriptorBuilder setProperties(Map<String, String> map);

    NutsDescriptorBuilder setProperties(Map<String, String> map, boolean append);


//    NutsDescriptorBuilder applyParents(NutsDescriptorBuilder[] parentDescriptors);

    NutsDescriptorBuilder applyProperties();

    NutsDescriptorBuilder applyParents(NutsDescriptor[] parentDescriptors);

    NutsDescriptorBuilder applyProperties(Map<String, String> properties);

    NutsDescriptorBuilder setName(String name);

    NutsDescriptorBuilder setParents(NutsId[] parents);

    NutsDescriptor build();

    NutsDescriptorBuilder setArch(String[] archs);

    NutsDescriptorBuilder setOs(String[] os);

    NutsDescriptorBuilder setOsdist(String[] osdist);

    NutsDescriptorBuilder setPlatform(String[] platform);
}
