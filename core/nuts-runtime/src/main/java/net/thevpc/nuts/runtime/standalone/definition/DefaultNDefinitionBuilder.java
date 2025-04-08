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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNDefinitionBuilder /*implements NDefinition*/ {

    private NId id;
    private NDescriptor descriptor;
    private String repositoryUuid;
    private String repositoryName;

    private NPath content;
    private NInstallInformation installInformation;
    private NDependencies dependencies;
    private NDescriptor effectiveDescriptor;
    private NId apiId = null;

    public DefaultNDefinitionBuilder() {
    }

    public DefaultNDefinitionBuilder(String repoUuid, String repoName, NId id, NDescriptor descriptor, NPath content, NInstallInformation install, NId apiId) {
        this.descriptor = descriptor;
        this.content = content;
        this.id = id;
        if (!id.isLongId()) {
            throw new NIllegalArgumentException(NMsg.ofC("id should not have query defined in descriptors : %s", id));
        }
        this.installInformation = install;
        this.repositoryUuid = repoUuid;
        this.repositoryName = repoName;
        this.apiId = apiId;
    }

    public DefaultNDefinitionBuilder(NDefinition other) {
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
    }

    public DefaultNDefinitionBuilder(DefaultNDefinitionBuilder other) {
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
    }

    public NDefinition build() {
        return new DefaultNDefinition(repositoryUuid, repositoryName, id, descriptor, content, installInformation, apiId,effectiveDescriptor, dependencies);
    }

    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public DefaultNDefinitionBuilder setId(NId id) {
        this.id = id;
        return this;
    }

    public NId getId() {
        return id;
    }

    public boolean isTemporary() {
        return content != null && content.isUserTemporary();
    }

    public NDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "Definition{"
                + " id=" + id
                + ", content=" + content
                + '}';
    }

    public DefaultNDefinitionBuilder copy() {
        return new DefaultNDefinitionBuilder(this);
    }

    public NOptional<NPath> getContent() {
        return NOptional.of(content, () -> NMsg.ofC("content not found for id %s", getId()));
    }

    public NOptional<NDescriptor> getEffectiveDescriptor() {
        return NOptional.of(effectiveDescriptor, () -> NMsg.ofC("unable to get effectiveDescriptor for id %s. You need to call search.setEffective(...) first.", getId()));
    }

    public NOptional<NInstallInformation> getInstallInformation() {
        return NOptional.of(installInformation, () -> NMsg.ofC("unable to get install information for id %s.", getId()));
    }

    public NOptional<NDependencies> getDependencies() {
        return NOptional.of(dependencies, () -> NMsg.ofC("unable to get dependencies for id %s. You need to call search.setDependencies(...) first.", getId()));
    }

    public DefaultNDefinitionBuilder setContent(NPath content) {
        this.content = content;
        return this;
    }

    public DefaultNDefinitionBuilder setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public int compareTo(DefaultNDefinitionBuilder n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof DefaultNDefinitionBuilder)) {
            return -1;
        }
        NId o1 = getId();
        NId o2 = n2.getId();
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
        final DefaultNDefinitionBuilder other = (DefaultNDefinitionBuilder) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    public DefaultNDefinitionBuilder setEffectiveDescriptor(NDescriptor effectiveDescriptor) {
        this.effectiveDescriptor = effectiveDescriptor;
        return this;
    }

    public DefaultNDefinitionBuilder setInstallInformation(NInstallInformation install) {
        this.installInformation = install;
        return this;
    }

    public DefaultNDefinitionBuilder setDependencies(NDependencies dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public DefaultNDefinitionBuilder setApiId(NId apiId) {
        this.apiId = apiId;
        return this;
    }

    public NId getApiId() {
        return apiId;
    }

    public DefaultNDefinitionBuilder setRepositoryUuid(String repositoryUuid) {
        this.repositoryUuid = repositoryUuid;
        return this;
    }

    public DefaultNDefinitionBuilder setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }
}
