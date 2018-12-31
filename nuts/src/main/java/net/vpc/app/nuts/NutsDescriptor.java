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

import java.io.*;
import java.util.Map;

/**
 * Created by vpc on 2/19/17.
 */
public interface NutsDescriptor extends Serializable {

    NutsId getId();

    NutsId[] getParents();

    boolean isExecutable();

    boolean isNutsApplication();

    String getPackaging();

    String getExt();

    String getFace();

    String[] getArch();

    String[] getOs();

    String[] getOsdist();

    String[] getPlatform();

    String getName();

    String[] getLocations();

    NutsDescriptor setLocations(String[] locations);

    /**
     * The dependencies specified here are not used until they
     *  are referenced in a POM within the group. This allows the
     *  specification of a &quot;standard&quot; version for a particular.
     *  This corresponds to dependencyManagement.dependencies in maven
     * @return "standard" dependencies
     */
    NutsDependency[] getStandardDependencies();

    NutsDependency[] getDependencies();

    NutsDependency[] getDependencies(NutsDependencyFilter dependencyFilter);

    NutsExecutorDescriptor getExecutor();

    NutsExecutorDescriptor getInstaller();

    Map<String, String> getProperties();

    String getDescription();

    String getSHA1();


    boolean matchesEnv(String arch, String os, String dist, String platform);

    boolean matchesPackaging(String packaging);

    NutsDescriptor setExecutable(boolean executable);

    NutsDescriptor setNutsApplication(boolean nutsApp);

    NutsDescriptor setExecutor(NutsExecutorDescriptor executor);


    NutsDescriptor setExt(String ext);

    NutsDescriptor addProperty(String name, String value);

    NutsDescriptor removeProperty(String name);

    NutsDescriptor addOs(String os);

    NutsDescriptor addOsdist(String osdist);

    NutsDescriptor addArch(String arch);

    NutsDescriptor addPlatform(String platform);

    NutsDescriptor removeOs(String os);

    NutsDescriptor removeOsdist(String osdist);

    NutsDescriptor removeArch(String arch);

    NutsDescriptor removePlatform(String platform);

    NutsDescriptor setPackaging(String packaging);

    NutsDescriptor setId(NutsId id);

    NutsDescriptor removeDependency(NutsDependency dependency);

    NutsDescriptor addDependency(NutsDependency dependency);

    NutsDescriptor addDependencies(NutsDependency[] dependencies);

    NutsDescriptor setDependencies(NutsDependency[] dependencies);

    NutsDescriptor setProperties(Map<String, String> map, boolean append);


    NutsDescriptor applyParents(NutsDescriptor[] parentDescriptors);

    NutsDescriptor applyProperties();

    NutsDescriptor applyProperties(Map<String, String> properties);

    boolean matchesPlatform(String platform);

    boolean matchesOs(String os);

    boolean matchesArch(String arch);

    boolean matchesOsdist(String oddist);

    void write(File file) throws NutsIOException;

    void write(PrintStream out) throws NutsIOException;

    void write(OutputStream out) throws NutsIOException;

    void write(Writer out) throws NutsIOException;

    void write(File file, boolean pretty) throws NutsIOException;

    void write(Writer os, boolean pretty) throws NutsIOException;

    String toString(boolean pretty);

    NutsDescriptorBuilder builder();

}
