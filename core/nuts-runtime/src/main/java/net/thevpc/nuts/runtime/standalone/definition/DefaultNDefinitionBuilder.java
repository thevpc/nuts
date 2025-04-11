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
import java.util.Set;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNDefinitionBuilder implements NDefinitionBuilder {

    private NId id;
    private NDescriptor descriptor;
    private String repositoryUuid;
    private String repositoryName;

    private NPath content;
    private NInstallInformation installInformation;
    private NDependencies dependencies;
    private NDescriptor effectiveDescriptor;
    private NDependency dependency;
    private NId apiId = null;
    private Set<NDescriptorFlag> effectiveFlags;

    public DefaultNDefinitionBuilder() {
    }

    public DefaultNDefinitionBuilder(String repoUuid, String repoName, NId id, NDescriptor descriptor, NPath content, NInstallInformation install, NId apiId, NDependency dependency,Set<NDescriptorFlag> effectiveFlags) {
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
        this.dependency = dependency;
        this.effectiveFlags = effectiveFlags;
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
            this.dependency = other.getDependency();
            this.effectiveFlags = other.getEffectiveFlags().orNull();
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
            this.dependency = other.getDependency();
            this.effectiveFlags = other.getEffectiveFlags().orNull();
        }
    }

    @Override
    public NDependency getDependency() {
        return dependency;
    }

    @Override
    public NDefinitionBuilder setDependency(NDependency dependency) {
        this.dependency = dependency;
        return this;
    }

    @Override
    public NDefinition build() {
        return new DefaultNDefinition(repositoryUuid, repositoryName, id, descriptor, content, installInformation, apiId, effectiveDescriptor, dependencies,
                dependency != null ? dependency : id != null ? id.toDependency() : null,effectiveFlags
        );
    }

    @Override
    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public NDefinitionBuilder setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public boolean isTemporary() {
        return content != null && content.isUserTemporary();
    }

    @Override
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

    @Override
    public NDefinitionBuilder copy() {
        return new DefaultNDefinitionBuilder(this);
    }

    @Override
    public NOptional<NPath> getContent() {
        return NOptional.of(content, () -> NMsg.ofC("content not found for id %s", getId()));
    }

    @Override
    public NOptional<NDescriptor> getEffectiveDescriptor() {
        return NOptional.of(effectiveDescriptor, () -> NMsg.ofC("unable to get effectiveDescriptor for id %s. You need to call search.setEffective(...) first.", getId()));
    }

    @Override
    public NOptional<NInstallInformation> getInstallInformation() {
        return NOptional.of(installInformation, () -> NMsg.ofC("unable to get install information for id %s.", getId()));
    }

    @Override
    public NOptional<NDependencies> getDependencies() {
        return NOptional.of(dependencies, () -> NMsg.ofC("unable to get dependencies for id %s. You need to call search.setDependencies(...) first.", getId()));
    }

    @Override
    public NDefinitionBuilder setContent(NPath content) {
        this.content = content;
        return this;
    }

    @Override
    public NDefinitionBuilder setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public int compareTo(NDefinitionBuilder n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof NDefinitionBuilder)) {
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
        final NDefinitionBuilder other = (NDefinitionBuilder) obj;
        if (!Objects.equals(this.id, other.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public NDefinitionBuilder setEffectiveDescriptor(NDescriptor effectiveDescriptor) {
        this.effectiveDescriptor = effectiveDescriptor;
        return this;
    }

    @Override
    public NDefinitionBuilder setInstallInformation(NInstallInformation install) {
        this.installInformation = install;
        return this;
    }

    @Override
    public NDefinitionBuilder setDependencies(NDependencies dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    @Override
    public NDefinitionBuilder setApiId(NId apiId) {
        this.apiId = apiId;
        return this;
    }

    @Override
    public NId getApiId() {
        return apiId;
    }

    @Override
    public NDefinitionBuilder setRepositoryUuid(String repositoryUuid) {
        this.repositoryUuid = repositoryUuid;
        return this;
    }

    @Override
    public NDefinitionBuilder setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    @Override
    public NDefinitionBuilder setEffectiveFlags(Set<NDescriptorFlag> effectiveFlags) {
        this.effectiveFlags = effectiveFlags;
        return this;
    }

    @Override
    public NOptional<Set<NDescriptorFlag>> getEffectiveFlags() {
        return NOptional.of(effectiveFlags, () -> NMsg.ofC("unable to get effectiveFlags for id %s.", getId()));
    }
}
