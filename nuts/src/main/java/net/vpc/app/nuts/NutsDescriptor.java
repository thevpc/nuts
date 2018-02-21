/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by vpc on 2/19/17.
 */
public interface NutsDescriptor {

    boolean matchesEnv(String arch, String os, String dist, String platform);

    boolean matchesPackaging(String packaging);

    NutsExecutorDescriptor getInstaller();

    Map<String, String> getProperties();

    NutsId[] getParents();

    NutsDescriptor setProperties(Map<String, String> map, boolean append);

    String getName();

    String getDescription();

    boolean isExecutable();

    NutsDescriptor setExecutable(boolean executable);

    String getSHA1();

    void write(File file) throws NutsIOException;

    void write(OutputStream file) throws NutsIOException;

    void write(File file, boolean pretty) throws NutsIOException;

    String toString(boolean pretty);

    String toString();

    void write(OutputStream os, boolean pretty) throws NutsIOException;

    NutsExecutorDescriptor getExecutor();

    NutsDescriptor setExecutor(NutsExecutorDescriptor executor);

    String getExt();

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

    String getPackaging();

    NutsDescriptor setPackaging(String packaging);

    NutsId getId();

    NutsDescriptor setId(NutsId id);

    String getFace();

    NutsDescriptor removeDependency(NutsDependency dependency);

    NutsDescriptor addDependency(NutsDependency dependency);

    NutsDescriptor addDependencies(NutsDependency[] dependencies);

    NutsDependency[] getDependencies();

    NutsDescriptor setDependencies(NutsDependency[] dependencies);

    String[] getArch();

    String[] getOs();

    String[] getOsdist();

    String[] getPlatform();

    NutsDescriptor applyParents(NutsDescriptor[] parentDescriptors);

    NutsDescriptor applyProperties();

    NutsDescriptor applyProperties(Map<String, String> properties);

    boolean matchesPlatform(String platform);

    boolean matchesOs(String os);

    boolean matchesArch(String arch);

    boolean matchesOsdist(String oddist);
}
