/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.descriptor;

import net.thevpc.nuts.*;

import java.util.List;

/**
 * Created by vpc on 1/5/17.
 */
public abstract class DelegateNDescriptor extends AbstractNDescriptor {

    public DelegateNDescriptor(NSession session) {
        super(session);
    }

    protected abstract NDescriptor getBase();

    @Override
    public String toString() {
        return "DelegateNutsDescriptor{" + getBase() + "}";
    }

    @Override
    public NArtifactCall getInstaller() {
        return getBase().getInstaller();
    }

    @Override
    public List<NDescriptorProperty> getProperties() {
        return getBase().getProperties();
    }

    @Override
    public List<NId> getParents() {
        return getBase().getParents();
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
    public boolean isApplication() {
        return getBase().isApplication();
    }

    @Override
    public NArtifactCall getExecutor() {
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
    public NId getId() {
        return getBase().getId();
    }

    @Override
    public List<NDependency> getDependencies() {
        return getBase().getDependencies();
    }

    @Override
    public List<NDependency> getStandardDependencies() {
        return getBase().getStandardDependencies();
    }

    @Override
    public NEnvCondition getCondition() {
        return getBase().getCondition();
    }

    @Override
    public List<NIdLocation> getLocations() {
        return getBase().getLocations();
    }

    @Override
    public List<String> getIcons() {
        return getBase().getIcons();
    }

    @Override
    public String getGenericName() {
        return getBase().getGenericName();
    }

    @Override
    public List<String> getCategories() {
        return getBase().getCategories();
    }

    @Override
    public String getSolver() {
        return getBase().getSolver();
    }

    @Override
    public NOptional<NDescriptorProperty> getProperty(String name) {
        return getBase().getProperty(name);
    }

    @Override
    public NOptional<NLiteral> getPropertyValue(String name) {
        return getBase().getPropertyValue(name);
    }

    @Override
    public NIdType getIdType() {
        return getBase().getIdType();
    }
}
