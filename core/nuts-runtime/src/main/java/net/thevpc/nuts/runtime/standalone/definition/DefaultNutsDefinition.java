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
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.definition;

import java.nio.file.Path;
import java.util.Objects;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElementNotFoundException;
import net.thevpc.nuts.io.NutsPath;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsDefinition implements NutsDefinition {

    private NutsId id;
    private NutsDescriptor descriptor;
    private String repositoryUuid;
    private String repositoryName;

    private NutsPath content;
    private NutsInstallInformation installInformation;
    private NutsDependencies dependencies;
    private NutsDescriptor effectiveDescriptor;
    private NutsId apiId = null;
    private transient NutsSession session;

    public DefaultNutsDefinition() {
//        System.out.println("");
    }

    public DefaultNutsDefinition(String repoUuid, String repoName, NutsId id, NutsDescriptor descriptor, NutsPath content, NutsInstallInformation install, NutsId apiId, NutsSession session) {
        this.descriptor = descriptor;
        this.content = content;
        this.id = id;
        if (!id.isLongId()) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("id should not have query defined in descriptors : %s",id));
        }
        this.installInformation = install;
        this.repositoryUuid = repoUuid;
        this.repositoryName = repoName;
        this.apiId = apiId;
        this.session = session;
    }

    public DefaultNutsDefinition(NutsDefinition other, NutsSession session) {
        if (other != null) {
            this.descriptor = other.getDescriptor();
            this.id = other.getId();
            this.repositoryUuid = other.getRepositoryUuid();
            this.repositoryName = other.getRepositoryName();

            this.content = other.getContent().orNull();
            this.installInformation = other.getInstallInformation().orNull();
            this.effectiveDescriptor = other.getEffectiveDescriptor().orNull();
            this.dependencies = other.getDependencies().orNull();
            this.apiId = other.getApiId();
        }
        this.session = session;
    }

    @Override
    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    public DefaultNutsDefinition setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    public boolean isTemporary() {
        return content != null && content.isUserTemporary();
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "Definition{"
                + " id=" + id
                + ", content=" + content
                + '}';
    }

    public DefaultNutsDefinition copy() {
        return new DefaultNutsDefinition(this, session);
    }

    @Override
    public NutsOptional<NutsPath> getContent() {
        return NutsOptional.of(content,s->NutsMessage.cstyle("content not found for id %s",getId()));
    }

    @Override
    public NutsOptional<NutsDescriptor> getEffectiveDescriptor() {
        return NutsOptional.of(effectiveDescriptor,s->NutsMessage.cstyle("unable to get effectiveDescriptor for id %s. You need to call search.setEffective(...) first.",getId()));
    }

    @Override
    public NutsOptional<NutsInstallInformation> getInstallInformation() {
        return NutsOptional.of(installInformation,s->NutsMessage.cstyle("unable to get install information for id %s.",getId()));
    }

    @Override
    public NutsOptional<NutsDependencies> getDependencies() {
        return NutsOptional.of(dependencies,s->NutsMessage.cstyle("unable to get dependencies for id %s. You need to call search.setDependencies(...) first.",getId()));
    }

    public DefaultNutsDefinition setContent(NutsPath content) {
        this.content = content;
        return this;
    }

    public DefaultNutsDefinition setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public int compareTo(NutsDefinition n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof DefaultNutsDefinition)) {
            return -1;
        }
        NutsId o1 = getId();
        NutsId o2 = n2.getId();
        if (o1 == null || o2 == null) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            return 1;
        }
        return o1.compareTo(o2);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsDefinition other = (DefaultNutsDefinition) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    public DefaultNutsDefinition setEffectiveDescriptor(NutsDescriptor effectiveDescriptor) {
        this.effectiveDescriptor = effectiveDescriptor;
        return this;
    }

    public DefaultNutsDefinition setInstallInformation(NutsInstallInformation install) {
        this.installInformation = install;
        return this;
    }

    public DefaultNutsDefinition setDependencies(NutsDependencies dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    @Override
    public NutsId getApiId() {
        return apiId;
    }

    public DefaultNutsDefinition setRepositoryUuid(String repositoryUuid) {
        this.repositoryUuid = repositoryUuid;
        return this;
    }

    public DefaultNutsDefinition setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }
}
