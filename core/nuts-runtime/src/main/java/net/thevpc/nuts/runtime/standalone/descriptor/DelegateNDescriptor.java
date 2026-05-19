/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.descriptor;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.artifact.NIdLocation;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/5/17.
 */
public abstract class DelegateNDescriptor extends AbstractNDescriptor {

    public DelegateNDescriptor() {
        super();
    }

    protected abstract NDescriptor getBase();

    @Override
    public String toString() {
        return "DelegateNutsDescriptor{" + getBase() + "}";
    }

    @Override
    public NArtifactCall installer() {
        return getBase().installer();
    }

    @Override
    public List<NDescriptorProperty> properties() {
        return getBase().properties();
    }

    @Override
    public List<NId> parents() {
        return getBase().parents();
    }

    @Override
    public String name() {
        return getBase().name();
    }

    @Override
    public String description() {
        return getBase().description();
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
    public boolean isPlatformApplication() {
        return getBase().isPlatformApplication();
    }

    @Override
    public NArtifactCall executor() {
        return getBase().executor();
    }

    //    @Override
//    public String getExt() {
//        return getBase().getExt();
//    }
    @Override
    public String packaging() {
        return getBase().packaging();
    }

    @Override
    public NId id() {
        return getBase().id();
    }

    @Override
    public List<NDependency> dependencies() {
        return getBase().dependencies();
    }

    @Override
    public List<NDependency> standardDependencies() {
        return getBase().standardDependencies();
    }

    @Override
    public NEnvCondition condition() {
        return getBase().condition();
    }

    @Override
    public List<NIdLocation> locations() {
        return getBase().locations();
    }

    @Override
    public List<String> icons() {
        return getBase().icons();
    }

    @Override
    public String genericName() {
        return getBase().genericName();
    }

    @Override
    public List<String> categories() {
        return getBase().categories();
    }

    @Override
    public String solver() {
        return getBase().solver();
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
    public NIdType idType() {
        return getBase().idType();
    }

    @Override
    public boolean isBlank() {
        return getBase().isBlank();
    }

    @Override
    public Set<NDescriptorFlag> flags() {
        return getBase().flags();
    }

    @Override
    public List<NDescriptorContributor> contributors() {
        return getBase().contributors();
    }

    @Override
    public List<NDescriptorContributor> developers() {
        return getBase().developers();
    }

    @Override
    public List<NDescriptorLicense> licenses() {
        return getBase().licenses();
    }

    @Override
    public List<NDescriptorMailingList> mailingLists() {
        return getBase().mailingLists();
    }

    @Override
    public NDescriptorOrganization organization() {
        return getBase().organization();
    }

    @Override
    public boolean isNoContent() {
        return getBase().isNoContent();
    }
}
