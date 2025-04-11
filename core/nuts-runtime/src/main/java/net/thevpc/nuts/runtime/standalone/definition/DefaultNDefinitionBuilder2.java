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
import net.thevpc.nuts.runtime.standalone.util.ValueSupplier;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNDefinitionBuilder2 {

    private Supplier<NId> id;
    private Supplier<NDescriptor> descriptor;
    private Supplier<String> repositoryUuid;
    private Supplier<String> repositoryName;

    private Supplier<NPath> content;
    private Supplier<NInstallInformation> installInformation;
    private Supplier<NDependencies> dependencies;
    private Supplier<NDescriptor> effectiveDescriptor;
    private Supplier<NDependency> dependency;
    private Supplier<NId> apiId = null;
    private Supplier<Set<NDescriptorFlag>> effectiveFlags;

    public DefaultNDefinitionBuilder2() {
    }

    public DefaultNDefinitionBuilder2(NDefinition other) {
        if (other != null) {
            if(other instanceof DefaultNDefinitionWithSuppliers) {
                DefaultNDefinitionWithSuppliers ds=(DefaultNDefinitionWithSuppliers) other;
                this.descriptor = ds.descriptor;
                this.id = ds.id;
                this.repositoryUuid = ds.repositoryUuid;
                this.repositoryName = ds.repositoryName;
                this.content = ds.content;
                this.installInformation = ds.installInformation;
                this.effectiveDescriptor = ds.effectiveDescriptor;
                this.dependencies = ds.dependencies;
                this.apiId = ds.apiId;
                this.dependency = ds.dependency;
                this.effectiveFlags = ds.effectiveFlags;
            }else {
                this.descriptor = new ValueSupplier<>(other.getDescriptor());
                this.id = new ValueSupplier<>(other.getId());
                this.repositoryUuid = new ValueSupplier<>(other.getRepositoryUuid());
                this.repositoryName = new ValueSupplier<>(other.getRepositoryName());

                this.content = () -> other.getContent().orNull();
                this.installInformation = () -> other.getInstallInformation().orNull();
                this.effectiveDescriptor = () -> other.getEffectiveDescriptor().orNull();
                this.dependencies = () -> other.getDependencies().orNull();
                this.apiId = () -> other.getApiId();
                this.dependency = () -> other.getDependency();
                this.effectiveFlags = () -> other.getEffectiveFlags().orNull();
            }
        }
    }

    public DefaultNDefinitionBuilder2(DefaultNDefinitionBuilder2 other) {
        if (other != null) {
            this.descriptor = other.descriptor;
            this.id = other.id;
            this.repositoryUuid = other.repositoryUuid;
            this.repositoryName = other.repositoryName;

            this.content = other.content;
            this.installInformation = other.installInformation;
            this.effectiveDescriptor = other.effectiveDescriptor;
            this.dependencies = other.dependencies;
            this.apiId = other.apiId;
            this.dependency = other.dependency;
            this.effectiveFlags = other.effectiveFlags;
        }
    }


    public Supplier<NDependency> getDependency() {
        return dependency;
    }


    public DefaultNDefinitionBuilder2 setDependency(Supplier<NDependency> dependency) {
        this.dependency = dependency;
        return this;
    }


    public NDefinition build() {
        return new DefaultNDefinitionWithSuppliers(repositoryUuid, repositoryName, id, descriptor, content, installInformation, apiId, effectiveDescriptor, dependencies,
                dependency, effectiveFlags
        );
    }


    public Supplier<String> getRepositoryUuid() {
        return repositoryUuid;
    }


    public Supplier<String> getRepositoryName() {
        return repositoryName;
    }


    public DefaultNDefinitionBuilder2 setId(Supplier<NId> id) {
        this.id = id;
        return this;
    }


    public Supplier<NId> getId() {
        return id;
    }


    public Supplier<NDescriptor> getDescriptor() {
        return descriptor;
    }


    public String toString() {
        return "Definition{"
                + " id=" + id
                + ", content=" + content
                + '}';
    }


    public DefaultNDefinitionBuilder2 copy() {
        return new DefaultNDefinitionBuilder2(this);
    }


    public Supplier<NPath> getContent() {
        return content;
    }


    public Supplier<NDescriptor> getEffectiveDescriptor() {
        return effectiveDescriptor;
    }


    public Supplier<NInstallInformation> getInstallInformation() {
        return installInformation;
    }


    public Supplier<NDependencies> getDependencies() {
        return dependencies;
    }


    public DefaultNDefinitionBuilder2 setContent(Supplier<NPath> content) {
        this.content = content;
        return this;
    }


    public DefaultNDefinitionBuilder2 setDescriptor(Supplier<NDescriptor> descriptor) {
        this.descriptor = descriptor;
        return this;
    }


    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }


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


    public DefaultNDefinitionBuilder2 setEffectiveDescriptor(Supplier<NDescriptor> effectiveDescriptor) {
        this.effectiveDescriptor = effectiveDescriptor;
        return this;
    }


    public DefaultNDefinitionBuilder2 setInstallInformation(Supplier<NInstallInformation> install) {
        this.installInformation = install;
        return this;
    }


    public DefaultNDefinitionBuilder2 setDependencies(Supplier<NDependencies> dependencies) {
        this.dependencies = dependencies;
        return this;
    }


    public DefaultNDefinitionBuilder2 setApiId(Supplier<NId> apiId) {
        this.apiId = apiId;
        return this;
    }


    public Supplier<NId> getApiId() {
        return apiId;
    }


    public DefaultNDefinitionBuilder2 setRepositoryUuid(Supplier<String> repositoryUuid) {
        this.repositoryUuid = repositoryUuid;
        return this;
    }


    public DefaultNDefinitionBuilder2 setRepositoryName(Supplier<String> repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    public DefaultNDefinitionBuilder2 setEffectiveFlags(Supplier<Set<NDescriptorFlag>> effectiveFlags) {
        this.effectiveFlags = effectiveFlags;
        return this;
    }
}
