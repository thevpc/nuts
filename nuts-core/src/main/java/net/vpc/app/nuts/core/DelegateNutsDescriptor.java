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

import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public abstract class DelegateNutsDescriptor extends AbstractNutsDescriptor {

    public DelegateNutsDescriptor() {
    }

    protected abstract NutsDescriptor getBase();

    @Override
    public String toString() {
        return "DelegateNutsDescriptor{" + getBase() + "}";
    }

    @Override
    public NutsExecutorDescriptor getInstaller() {
        return getBase().getInstaller();
    }

    @Override
    public Map<String, String> getProperties() {
        return getBase().getProperties();
    }

    @Override
    public NutsId[] getParents() {
        return getBase().getParents();
    }

    @Override
    public NutsDescriptor setExecutor(NutsExecutorDescriptor executor) {
        return getBase().setExecutor(executor);
    }

    @Override
    public NutsDescriptor setId(NutsId id) {
        return getBase().setId(id);
    }

    @Override
    public NutsDescriptor setPackaging(String packaging) {
        return getBase().setPackaging(packaging);
    }

    @Override
    public String getName() {
        return getBase().getName();
    }

    @Override
    public String getDescription() {
        return getBase().getDescription();
    }

    @Override
    public boolean isExecutable() {
        return getBase().isExecutable();
    }

    @Override
    public boolean isNutsApplication() {
        return getBase().isNutsApplication();
    }

    @Override
    public NutsExecutorDescriptor getExecutor() {
        return getBase().getExecutor();
    }

    //    @Override
//    public String getExt() {
//        return getBase().getExt();
//    }
    @Override
    public String getPackaging() {
        return getBase().getPackaging();
    }

    @Override
    public NutsId getId() {
        return getBase().getId();
    }

    @Override
    public NutsDependency[] getDependencies() {
        return getBase().getDependencies();
    }

    @Override
    public NutsDependency[] getStandardDependencies() {
        return getBase().getStandardDependencies();
    }

    @Override
    public String[] getArch() {
        return getBase().getArch();
    }

    @Override
    public String[] getOs() {
        return getBase().getOs();
    }

    @Override
    public String[] getOsdist() {
        return getBase().getOsdist();
    }

    @Override
    public String[] getPlatform() {
        return getBase().getPlatform();
    }

//    @Override
//    public String getAlternative() {
//        return getBase().getAlternative();
//    }

    @Override
    public NutsDescriptor setProperties(Map<String, String> map) {
        return getBase().setProperties(map);
    }

    @Override
    public NutsDescriptor addProperties(Map<String, String> map) {
        return getBase().addProperties(map);
    }

    @Override
    public NutsDescriptor setExecutable(boolean executable) {
        return getBase().setExecutable(executable);
    }

    @Override
    public NutsIdLocation[] getLocations() {
        return getBase().getLocations();
    }

    @Override
    public NutsClassifierMapping[] getClassifierMappings() {
        return getBase().getClassifierMappings();
    }

}
